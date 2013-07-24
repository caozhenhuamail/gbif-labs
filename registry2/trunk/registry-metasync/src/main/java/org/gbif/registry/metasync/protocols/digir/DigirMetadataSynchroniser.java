/*
 * Copyright 2013 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry.metasync.protocols.digir;

import org.gbif.api.model.registry2.Citation;
import org.gbif.api.model.registry2.Contact;
import org.gbif.api.model.registry2.Dataset;
import org.gbif.api.model.registry2.Endpoint;
import org.gbif.api.model.registry2.Identifier;
import org.gbif.api.model.registry2.Installation;
import org.gbif.api.model.registry2.MachineTag;
import org.gbif.api.vocabulary.registry2.ContactType;
import org.gbif.api.vocabulary.registry2.EndpointType;
import org.gbif.api.vocabulary.registry2.IdentifierType;
import org.gbif.api.vocabulary.registry2.InstallationType;
import org.gbif.registry.metasync.api.SyncResult;
import org.gbif.registry.metasync.api.ErrorCode;
import org.gbif.registry.metasync.api.MetadataException;
import org.gbif.registry.metasync.protocols.BaseProtocolHandler;
import org.gbif.registry.metasync.protocols.digir.model.DigirContact;
import org.gbif.registry.metasync.protocols.digir.model.DigirMetadata;
import org.gbif.registry.metasync.protocols.digir.model.DigirResource;
import org.gbif.registry.metasync.util.Constants;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.registry.metasync.util.Constants.METADATA_NAMESPACE;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * DiGIR synchronisation happens by issuing a metadata request to the single endpoint that the installation should
 * have. The response contains a list of resources which we parse to GBIF {@link Dataset} objects, each having a single
 * Endpoint.
 */
public class DigirMetadataSynchroniser extends BaseProtocolHandler {

  // Source: http://stackoverflow.com/questions/27910/finding-a-doi-in-a-document-or-page#comment24134610_10324802
  private static final Pattern DOI_PATTERN =
    Pattern.compile("\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'])\\S)+)\\b");

  public DigirMetadataSynchroniser(HttpClient httpClient) {
    super(httpClient);
  }

  @Override
  public boolean canHandle(Installation installation) {
    return installation.getType() == InstallationType.DIGIR_INSTALLATION;
  }

  @Override
  public SyncResult syncInstallation(Installation installation, List<Dataset> datasets) throws MetadataException {
    checkArgument(installation.getType() == InstallationType.DIGIR_INSTALLATION, "Only supports DiGIR Installations");

    if (installation.getEndpoints().size() != 1) {
      throw new MetadataException("A DiGIR Installation should only ever have one Endpoint, this one has ["
                                  + installation.getEndpoints().size()
                                  + "]", ErrorCode.OTHER_ERROR);
    }
    Endpoint endpoint = installation.getEndpoints().get(0);

    DigirMetadata metadata = getDigirMetadata(endpoint);
    updateInstallation(metadata, installation);
    updateInstallationEndpoint(metadata, endpoint);
    return mapToDatasets(metadata, datasets, endpoint.getUrl(), installation);
  }

  private DigirMetadata getDigirMetadata(Endpoint endpoint) throws MetadataException {
    return doHttpRequest(URI.create(endpoint.getUrl()), newDigester(DigirMetadata.class));
  }

  /**
   * Updates the Installation in in place with all the data gathered from the Endpoint.
   */
  private void updateInstallation(DigirMetadata metadata, Installation installation) {
    installation.setContacts(matchContacts(installation.getContacts(),
                                           convertToRegistryContacts(metadata.getHost().getContacts())));

    installation.setDescription(metadata.getHost().getDescription());

    installation.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                      Constants.DIGIR_CODE,
                                                      metadata.getHost().getCode()));
    installation.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                      Constants.INSTALLATION_VERSION,
                                                      metadata.getImplementation()));
  }

  /**
   * Updates the single Endpoint that a DiGIR Installation has.
   */
  private void updateInstallationEndpoint(DigirMetadata metadata, Endpoint endpoint) {
    endpoint.setDescription(metadata.getHost().getDescription());
  }

  /**
   * Maps the resources we got from the metadata response to Datasets that are currently hosted by this Installation.
   * We identify Datasets by the {@code code} attribute that we're getting from the metadata response. We're saving
   * this code on the Dataset itself as a machine tag.
   */
  private SyncResult mapToDatasets(
    DigirMetadata metadata, Iterable<Dataset> datasets, String url, Installation installation
  ) {
    List<Dataset> added = Lists.newArrayList();
    List<Dataset> deleted = Lists.newArrayList();
    Map<Dataset, Dataset> updated = Maps.newHashMap();

    // Maps currently existing DiGIR codes to the Datasets from our Registry that use those codes
    Map<String, Dataset> codeMap = Maps.newHashMap();
    for (Dataset dataset : datasets) {

      // Find the "code" machine tag
      for (MachineTag tag : dataset.getMachineTags()) {
        if (tag.getNamespace().equals(METADATA_NAMESPACE) && tag.getName().equals(Constants.DIGIR_CODE)) {
          codeMap.put(tag.getValue(), dataset);
        }
      }
    }

    // Sort in either updated or added Datasets using the just built Map
    for (DigirResource resource : metadata.getResources()) {
      Dataset newDataset = convertToDataset(resource, url);
      if (codeMap.containsKey(resource.getCode())) {
        updated.put(codeMap.get(resource.getCode()), newDataset);
      } else {
        added.add(newDataset);
      }
    }

    // All Datasets that weren't updated must have been deleted
    for (Dataset dataset : datasets) {
      if (!updated.containsKey(dataset)) {
        deleted.add(dataset);
      }
    }

    return new SyncResult(updated, added, deleted, installation);
  }

  /**
   * Converts a DiGIR resource to a GBIF Dataset.
   */
  private Dataset convertToDataset(DigirResource resource, String url) {
    Dataset dataset = new Dataset();
    dataset.setTitle(resource.getName());
    dataset.setDescription(resource.getDescription());

    // We're only using the very first related URI even though there might be more
    if (!resource.getRelatedInformation().isEmpty()) {
      dataset.setHomepage(resource.getRelatedInformation().iterator().next());
    }
    dataset.setCitation(new Citation(resource.getCitation(), null));
    dataset.setRights(resource.getUseRestrictions());
    dataset.setContacts(convertToRegistryContacts(resource.getContacts()));

    dataset.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE, Constants.DIGIR_CODE, resource.getCode()));

    if (resource.getNumberOfRecords() != 0) {
      dataset.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                   Constants.DECLARED_COUNT,
                                                   String.valueOf(resource.getNumberOfRecords())));
    }

    if (resource.getMaxSearchResponseRecords() != 0) {
      dataset.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                   Constants.DIGIR_MAX_SEARCH_RESPONSE_RECORDS,
                                                   String.valueOf(resource.getMaxSearchResponseRecords())));
    }

    if (resource.getDateLastUpdated() != null) {
      dataset.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                   Constants.DATE_LAST_UPDATED,
                                                   resource.getDateLastUpdated().toString()));
    }

    for (Map.Entry<String, URI> entry : resource.getConceptualSchemas().entrySet()) {
      dataset.addMachineTag(MachineTag.newInstance(METADATA_NAMESPACE,
                                                   Constants.CONCEPTUAL_SCHEMA,
                                                   entry.getValue().toASCIIString()));
    }

    // See if the code contains a DOI and set it as an Identifier
    Matcher matcher = DOI_PATTERN.matcher(resource.getCode());
    if (matcher.find()) {
      Identifier identifier = new Identifier();
      identifier.setType(IdentifierType.DOI);
      identifier.setIdentifier(matcher.group());
      dataset.getIdentifiers().add(identifier);
    }

    // Each DiGIR Dataset has exactly one Endpoint, we create and populate it here
    Endpoint endpoint = new Endpoint();
    endpoint.setDescription(resource.getName());
    endpoint.setUrl(url);
    // TODO: Set to MANIS if appropriate
    endpoint.setType(EndpointType.DIGIR);
    dataset.addEndpoint(endpoint);

    return dataset;
  }

  /**
   * Converts a list of DiGIR contacts to GBIF {@link Contact} objects.
   */
  private List<Contact> convertToRegistryContacts(Iterable<DigirContact> contacts) {
    List<Contact> resultList = Lists.newArrayList();
    for (DigirContact contact : contacts) {
      resultList.add(convertToRegistryContact(contact));
    }
    return resultList;
  }

  /**
   * Converts a single DiGIR contact to a GBIF {@link Contact}
   */
  private Contact convertToRegistryContact(DigirContact digirContact) {
    Contact contact = new Contact();
    contact.setFirstName(trimToNull(digirContact.getName()));
    contact.setPosition(trimToNull(digirContact.getTitle()));
    contact.setEmail(trimToNull(digirContact.getEmail()));
    contact.setPhone(trimToNull(digirContact.getPhone()));
    contact.setType(ContactType.inferType(digirContact.getType()));
    return contact;
  }

}
