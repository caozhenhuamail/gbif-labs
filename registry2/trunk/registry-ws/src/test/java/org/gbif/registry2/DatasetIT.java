/*
 * Copyright 2013 Global Biodiversity Information Facility (GBIF)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry2;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.registry2.Dataset;
import org.gbif.api.model.registry2.Installation;
import org.gbif.api.model.registry2.Metadata;
import org.gbif.api.model.registry2.Organization;
import org.gbif.api.model.registry2.search.DatasetSearchParameter;
import org.gbif.api.model.registry2.search.DatasetSearchRequest;
import org.gbif.api.model.registry2.search.DatasetSearchResult;
import org.gbif.api.service.registry2.DatasetSearchService;
import org.gbif.api.service.registry2.DatasetService;
import org.gbif.api.service.registry2.InstallationService;
import org.gbif.api.service.registry2.NodeService;
import org.gbif.api.service.registry2.OrganizationService;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.registry2.DatasetType;
import org.gbif.api.vocabulary.registry2.MetadataType;
import org.gbif.registry2.grizzly.RegistryServer;
import org.gbif.registry2.search.DatasetIndexUpdateListener;
import org.gbif.registry2.search.DatasetSearchUpdateUtils;
import org.gbif.registry2.search.SolrInitializer;
import org.gbif.registry2.utils.Datasets;
import org.gbif.registry2.utils.Installations;
import org.gbif.registry2.utils.Nodes;
import org.gbif.registry2.utils.Organizations;
import org.gbif.registry2.ws.resources.DatasetResource;
import org.gbif.registry2.ws.resources.InstallationResource;
import org.gbif.registry2.ws.resources.NodeResource;
import org.gbif.registry2.ws.resources.OrganizationResource;
import org.gbif.utils.file.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.solr.client.solrj.SolrServer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.gbif.registry2.guice.RegistryTestModules.webservice;
import static org.gbif.registry2.guice.RegistryTestModules.webserviceClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This is parameterized to run the same test routines for the following:
 * <ol>
 * <li>The persistence layer</li>
 * <li>The WS service layer</li>
 * <li>The WS service client layer</li>
 * </ol>
 */
@RunWith(Parameterized.class)
public class DatasetIT extends NetworkEntityTest<Dataset> {

  // Resets SOLR between each method
  @Rule
  public final SolrInitializer solrRule;

  private final DatasetService service;
  private final DatasetSearchService searchService;
  private final OrganizationService organizationService;
  private final NodeService nodeService;
  private final InstallationService installationService;
  private final DatasetIndexUpdateListener datasetIndexUpdater;

  @Parameters
  public static Iterable<Object[]> data() {
    final Injector client = webserviceClient();
    final Injector webservice = webservice();
    return ImmutableList.<Object[]>of(
      new Object[] {
        webservice.getInstance(DatasetResource.class),
        webservice.getInstance(DatasetResource.class),
        webservice.getInstance(OrganizationResource.class),
        webservice.getInstance(NodeResource.class),
        webservice.getInstance(InstallationResource.class),
        webservice.getInstance(Key.get(SolrServer.class, Names.named("Dataset"))),
        webservice.getInstance(DatasetIndexUpdateListener.class)},
      new Object[] {
        client.getInstance(DatasetService.class),
        client.getInstance(DatasetSearchService.class),
        client.getInstance(OrganizationService.class),
        client.getInstance(NodeService.class),
        client.getInstance(InstallationService.class),
        null, // use the SOLR in Grizzly
        null // use the SOLR in Grizzly
      }
      );
  }

  public DatasetIT(
    DatasetService service,
    DatasetSearchService searchService,
    OrganizationService organizationService,
    NodeService nodeService,
    InstallationService installationService,
    @Nullable SolrServer solrServer,
    @Nullable DatasetIndexUpdateListener datasetIndexUpdater) {
    super(service);
    this.service = service;
    this.searchService = searchService;
    this.organizationService = organizationService;
    this.nodeService = nodeService;
    this.installationService = installationService;
    // if no SOLR are given for the test, use the SOLR in Grizzly
    solrServer = solrServer == null ? RegistryServer.INSTANCE.getSolrServer() : solrServer;
    this.datasetIndexUpdater =
      datasetIndexUpdater == null ? RegistryServer.INSTANCE.getDatasetUpdater() : datasetIndexUpdater;

    this.solrRule = new SolrInitializer(solrServer, this.datasetIndexUpdater);
  }

  @Test
  public void testConstituents() {
    Dataset parent = create(newEntity(), 1);
    for (int id = 0; id < 10; id++) {
      Dataset constituent = newEntity();
      constituent.setParentDatasetKey(parent.getKey());
      constituent.setType(parent.getType());
      create(constituent, id + 2);
    }

    assertEquals(10, service.get(parent.getKey()).getNumConstituents());
  }

  // Easier to test this here than OrganizationIT due to our utility dataset factory
  @Test
  public void testHostedByList() {
    Dataset dataset = create(newEntity(), 1);
    Installation i = installationService.get(dataset.getInstallationKey());
    assertNotNull("Dataset should have an installation", i);
    PagingResponse<Dataset> owned = organizationService.ownedDatasets(i.getOrganizationKey(), new PagingRequest());
    PagingResponse<Dataset> hosted = organizationService.hostedDatasets(i.getOrganizationKey(), new PagingRequest());
    assertEquals("This installation should have only 1 owned dataset", 1, owned.getResults().size());
    assertTrue("This organization should not have any hosted datasets", hosted.getResults().isEmpty());
  }

  // Easier to test this here than OrganizationIT due to our utility dataset factory
  @Test
  public void testOwnedByList() {
    Dataset dataset = create(newEntity(), 1);
    PagingResponse<Dataset> owned =
      organizationService.ownedDatasets(dataset.getOwningOrganizationKey(), new PagingRequest());
    assertEquals("The organization should have only 1 dataset", 1, owned.getResults().size());
    assertEquals("The organization should own the dataset created", owned.getResults().get(0).getKey(),
      dataset.getKey());

    assertEquals("The organization should have 1 dataset count", 1,
      organizationService.get(dataset.getOwningOrganizationKey()).getNumOwnedDatasets());
  }

  // Easier to test this here than InstallationIT due to our utility dataset factory
  @Test
  public void testHostedByInstallationList() {
    Dataset dataset = create(newEntity(), 1);
    Installation i = installationService.get(dataset.getInstallationKey());
    assertNotNull("Dataset should have an installation", i);
    PagingResponse<Dataset> hosted =
      installationService.hostedDatasets(dataset.getInstallationKey(), new PagingRequest());
    assertEquals("This installation should have only 1 hosted dataset", 1, hosted.getResults().size());
    assertEquals("Paging response counts are not being set", Long.valueOf(1), hosted.getCount());
    assertEquals("The hosted installation should serve the dataset created", hosted.getResults().get(0).getKey(),
      dataset.getKey());
  }

  @Test
  public void testTypedSearch() {
    Dataset d = newEntity();
    d.setType(DatasetType.CHECKLIST);
    d = create(d, 1);
    assertSearch(d.getTitle(), 1); // 1 result expected for a simple search

    DatasetSearchRequest req = new DatasetSearchRequest();
    req.addTypeFilter(DatasetType.CHECKLIST);
    SearchResponse<DatasetSearchResult, DatasetSearchParameter> resp = searchService.search(req);
    assertNotNull(resp.getCount());
    assertEquals("SOLR does not have the expected number of results for query[" + req + "]", Long.valueOf(1),
      resp.getCount());
  }

  @Test
  public void testSearchListener() {
    Dataset d = newEntity();
    d = create(d, 1);
    assertSearch(d.getTitle(), 1); // 1 result expected

    // update
    String oldTitle = d.getTitle();
    d.setTitle("NEW-DATASET-TITLE");
    service.update(d);
    assertSearch("*", 1);
    assertSearch(oldTitle, 0);
    assertSearch(d.getTitle(), 1);

    // update owning organization title should be captured
    Organization owner = organizationService.get(d.getOwningOrganizationKey());
    assertSearch(owner.getTitle(), 1);
    oldTitle = owner.getTitle();
    owner.setTitle("NEW-OWNER-TITLE");
    organizationService.update(owner);
    assertSearch(oldTitle, 0);
    assertSearch(owner.getTitle(), 1);

    // update hosting organization title should be captured
    Installation installation = installationService.get(d.getInstallationKey());
    assertNotNull("Installation should be present", installation);
    Organization host = organizationService.get(installation.getOrganizationKey());
    assertSearch(host.getTitle(), 1);
    oldTitle = host.getTitle();
    host.setTitle("NEW-HOST-TITLE");
    organizationService.update(host);
    assertSearch(oldTitle, 0);
    assertSearch(host.getTitle(), 1);

    // check a deletion removes the dataset for search
    service.delete(d.getKey());
    assertSearch(host.getTitle(), 0);
  }

  @Test
  public void testInstallationMove() {
    Dataset d = newEntity();
    d = create(d, 1);
    assertSearch(d.getTitle(), 1); // 1 result expected

    UUID nodeKey = nodeService.create(Nodes.newInstance());
    Organization o = Organizations.newInstance(nodeKey);
    o.setTitle("A-NEW-ORG");
    UUID organizationKey = organizationService.create(o);
    assertSearch(o.getTitle(), 0); // No datasets hosted by that organization yet

    Installation installation = installationService.get(d.getInstallationKey());
    installation.setOrganizationKey(organizationKey);
    installationService.update(installation); // we just moved the installation to a new organization

    assertSearch(o.getTitle(), 1); // The dataset moved with the organization!
    assertSearch("*", 1);
  }

  /**
   * Utility to verify that after waiting for SOLR to update, the given query returns the expected count of results.
   */
  private void assertSearch(String query, int expected) {
    DatasetSearchUpdateUtils.awaitUpdates(datasetIndexUpdater); // SOLR updates are asynchronous
    DatasetSearchRequest req = new DatasetSearchRequest();
    req.setQ(query);
    SearchResponse<DatasetSearchResult, DatasetSearchParameter> resp = searchService.search(req);
    assertNotNull(resp.getCount());
    assertEquals("SOLR does not have the expected number of results for query[" + query + "]", Long.valueOf(expected),
      resp.getCount());
  }

  /**
   * Utility to verify that after waiting for SOLR to update, the given query returns the expected count of results.
   */
  private void assertSearch(Country publishingCountry, Country country, int expected) {
    DatasetSearchUpdateUtils.awaitUpdates(datasetIndexUpdater); // SOLR updates are asynchronous
    DatasetSearchRequest req = new DatasetSearchRequest();
    if (country != null) {
      req.addCountryFilter(country);
    }
    if (publishingCountry != null) {
      req.addPublishingCountryFilter(publishingCountry);
    }
    SearchResponse<DatasetSearchResult, DatasetSearchParameter> resp = searchService.search(req);
    assertNotNull(resp.getCount());
    assertEquals("SOLR does not have the expected number of results for country[" + country +
      "] and publishingCountry[" + publishingCountry + "]", Long.valueOf(expected),
      resp.getCount());
  }

  @Override
  protected Dataset newEntity() {
    // endorsing node for the organization
    UUID nodeKey = nodeService.create(Nodes.newInstance());
    // owning organization (required field)
    Organization o = Organizations.newInstance(nodeKey);
    UUID organizationKey = organizationService.create(o);

    Installation i = Installations.newInstance(organizationKey);
    UUID installationKey = installationService.create(i);

    return newEntity(organizationKey, installationKey);
  }

  private Dataset newEntity(UUID organizationKey, UUID installationKey) {
    // the dataset
    Dataset d = Datasets.newInstance(organizationKey);
    d.setInstallationKey(installationKey);
    return d;
  }

  @Test
  public void testCitation() {
    Dataset dataset = create(newEntity(), 1);

    // empty when new
    Dataset dRead = service.get(dataset.getKey());
    assertNotNull("Citation should never be null", dRead.getCitation());
    assertEquals("ABC", dRead.getCitation().getIdentifier());
    assertEquals("This is a citation text", dRead.getCitation().getText());

    dataset.getCitation().setIdentifier("doi:123");
    dataset.getCitation().setText("GOD publishing, volume 123");
    assertCitationChange(dataset, "doi:123", "GOD publishing, volume 123");

    dataset.getCitation().setText(null);
    assertCitationChange(dataset, "doi:123", null);
  }

  private void assertCitationChange(Dataset dataset, String identifier, String text) {
    service.update(dataset);

    Dataset dRead = service.get(dataset.getKey());
    assertNotNull("Citation should never be null", dRead.getCitation());
    assertEquals(identifier, dRead.getCitation().getIdentifier());
    assertEquals(text, dRead.getCitation().getText());
  }

  @Test
  public void test404() throws IOException {
    assertNull(service.get(UUID.randomUUID()));
  }

  @Test
  public void testMetadata() throws IOException {
    Dataset d1 = create(newEntity(), 1);

    // upload a valid EML doc
    service.insertMetadata(d1.getKey(), FileUtils.classpathStream("metadata/sample.xml"));

    // verify our dataset has changed
    Dataset d2 = service.get(d1.getKey());
    assertNotEquals("Dataset should have changed after metadata was uploaded", d1, d2);
    assertEquals("Tanzanian Entomological Collection", d2.getTitle());
    assertEquals("Created data should not change", d1.getCreated(), d2.getCreated());
    assertTrue("Dataset modification date should change", d1.getModified().before(d2.getModified()));

    // check metadata
    List<Metadata> metadata = service.listMetadata(d1.getKey(), MetadataType.DC);
    assertTrue("No Dublin Core uplaoded yet", metadata.isEmpty());

    metadata = service.listMetadata(d1.getKey(), MetadataType.EML);
    assertEquals("Exactly one EML uploaded", 1, metadata.size());
    assertEquals("Wrong metadata type", MetadataType.EML, metadata.get(0).getType());

    // upload subsequent DC document which has less priority than the previous EML doc
    service.insertMetadata(d1.getKey(), FileUtils.classpathStream("metadata/worms_dc.xml"));
    // verify our dataset has not changed
    Dataset d3 = service.get(d1.getKey());
    assertEquals("Tanzanian Entomological Collection", d3.getTitle());
    assertEquals("Created data should not change", d1.getCreated(), d3.getCreated());
  }

  @Test
  public void testByCountry() {
    createCountryDatasets(DatasetType.OCCURRENCE, Country.ANDORRA, 3);
    createCountryDatasets(DatasetType.OCCURRENCE, Country.DJIBOUTI, 1);
    createCountryDatasets(DatasetType.METADATA, Country.HAITI, 7);
    createCountryDatasets(DatasetType.OCCURRENCE, Country.HAITI, 3);
    createCountryDatasets(DatasetType.CHECKLIST, Country.HAITI, 2);

    assertResultsOfSize(service.listByCountry(Country.UNKNOWN, null, new PagingRequest()), 0);
    assertResultsOfSize(service.listByCountry(Country.ANDORRA, null, new PagingRequest()), 3);
    assertResultsOfSize(service.listByCountry(Country.DJIBOUTI, null, new PagingRequest()), 1);
    assertResultsOfSize(service.listByCountry(Country.HAITI, null, new PagingRequest()), 12);

    assertResultsOfSize(service.listByCountry(Country.ANDORRA, DatasetType.CHECKLIST, new PagingRequest()), 0);
    assertResultsOfSize(service.listByCountry(Country.HAITI, DatasetType.OCCURRENCE, new PagingRequest()), 3);
    assertResultsOfSize(service.listByCountry(Country.HAITI, DatasetType.CHECKLIST, new PagingRequest()), 2);
    assertResultsOfSize(service.listByCountry(Country.HAITI, DatasetType.METADATA, new PagingRequest()), 7);
  }

  @Test
  @Ignore("Country coverage not populated yet: http://dev.gbif.org/issues/browse/REG-393")
  public void testCountrySearch() {
    createCountryDatasets(Country.ANDORRA, 3);
    createCountryDatasets(DatasetType.OCCURRENCE, Country.DJIBOUTI, 1, Country.DJIBOUTI);
    createCountryDatasets(DatasetType.OCCURRENCE, Country.HAITI, 3, Country.AFGHANISTAN, Country.DENMARK);
    createCountryDatasets(DatasetType.CHECKLIST, Country.HAITI, 4, Country.GABON, Country.FIJI);
    createCountryDatasets(DatasetType.OCCURRENCE, Country.DOMINICA, 2, Country.DJIBOUTI);

    assertSearch(Country.ALBANIA, null, 0);
    assertSearch(Country.ANDORRA, null, 3);
    assertSearch(Country.DJIBOUTI, null, 1);
    assertSearch(Country.HAITI, null, 7);
    assertSearch(Country.UNKNOWN, null, 0);

    assertSearch(Country.HAITI, Country.GABON, 4);
    assertSearch(Country.HAITI, Country.FIJI, 4);
    assertSearch(Country.HAITI, Country.DENMARK, 3);
    assertSearch(Country.DJIBOUTI, Country.DENMARK, 0);
    assertSearch(Country.DJIBOUTI, Country.DJIBOUTI, 1);
    assertSearch(Country.DJIBOUTI, Country.GERMANY, 0);
    assertSearch(null, Country.DJIBOUTI, 3);
  }

  private void createCountryDatasets(Country publishingCountry, int number) {
    createCountryDatasets(DatasetType.OCCURRENCE, publishingCountry, number, null);
  }

  private void createCountryDatasets(DatasetType type, Country publishingCountry, int number, Country... countries) {
    Dataset d = addCountryCoverage(newEntity(), countries);
    d.setType(type);
    service.create(d);

    // assign a controlled country based organization
    Organization org = organizationService.get(d.getOwningOrganizationKey());
    org.setCountry(publishingCountry);
    organizationService.update(org);

    // create datasets for it
    for (int x = 1; x < number; x++) {
      d = addCountryCoverage(newEntity(org.getKey(), d.getInstallationKey()));
      d.setType(type);
      service.create(d);
    }
  }

  private Dataset addCountryCoverage(Dataset d, Country... countries) {
    if (countries != null) {
      for (Country c : countries) {
        if (c != null) {
          d.getCountryCoverage().add(c);
        }
      }
    }
    return d;
  }
}
