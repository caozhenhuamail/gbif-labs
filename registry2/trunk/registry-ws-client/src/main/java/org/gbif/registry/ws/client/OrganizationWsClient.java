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
package org.gbif.registry.ws.client;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.registry.model.Comment;
import org.gbif.api.registry.model.Contact;
import org.gbif.api.registry.model.Endpoint;
import org.gbif.api.registry.model.Identifier;
import org.gbif.api.registry.model.MachineTag;
import org.gbif.api.registry.model.Organization;
import org.gbif.api.registry.model.Tag;
import org.gbif.api.registry.service.OrganizationService;
import org.gbif.registry.ws.client.guice.RegistryWs;
import org.gbif.ws.client.BaseWsGetClient;

import java.util.List;
import java.util.UUID;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

/**
 * Client-side implementation to the OrganizationService.
 */
public class OrganizationWsClient extends BaseWsGetClient<Organization, UUID> implements OrganizationService {

  @Inject
  public OrganizationWsClient(@RegistryWs WebResource resource) {
    super(Organization.class, resource.path("organization"), null);
  }

  @Override
  public UUID create(Organization entity) {
    return post(UUID.class, entity, "/");
  }

  @Override
  public void delete(UUID key) {
    delete(key.toString());
  }

  @Override
  public PagingResponse<Organization> list(Pageable page) {
    return get(GenericTypes.PAGING_ORGANIZATION, null, null, page);
  }

  @Override
  public void update(Organization entity) {
    put(entity, entity.getKey().toString());
  }

  @Override
  public Organization get(UUID key) {
    return get(key.toString());
  }

  @Override
  public int addTag(UUID targetEntityKey, String value) {
    // post the value to .../uuid/tag and expect an int back
    return post(Integer.class, (Object) value, targetEntityKey.toString(), "tag");
  }

  @Override
  public void deleteTag(UUID taggedEntityKey, int tagKey) {
    delete(taggedEntityKey.toString(), "tag", String.valueOf(tagKey));
  }

  @Override
  public List<Tag> listTags(UUID taggedEntityKey, String owner) {
    return get(GenericTypes.LIST_TAG, null, null, // TODO add owner here
               (Pageable) null, taggedEntityKey.toString(), "tag");
  }

  @Override
  public int addContact(UUID targetEntityKey, Contact contact) {
    // post the contact to .../uuid/contact and expect an int back
    return post(Integer.class, contact, targetEntityKey.toString(), "contact");
  }

  @Override
  public void deleteContact(UUID targetEntityKey, int contactKey) {
    delete(targetEntityKey.toString(), "contact", String.valueOf(contactKey));
  }

  @Override
  public List<Contact> listContacts(UUID targetEntityKey) {
    return get(GenericTypes.LIST_CONTACT, null, null,
               // TODO: type on contact?
               (Pageable) null, targetEntityKey.toString(), "contact");
  }

  @Override
  public int addEndpoint(UUID targetEntityKey, Endpoint endpoint) {
    return post(Integer.class, endpoint, targetEntityKey.toString(), "endpoint");
  }

  @Override
  public void deleteEndpoint(UUID targetEntityKey, int endpointKey) {
    delete(targetEntityKey.toString(), "endpoint", String.valueOf(endpointKey));
  }

  @Override
  public List<Endpoint> listEndpoints(UUID targetEntityKey) {
    return get(GenericTypes.LIST_ENDPOINT, null, null,
               // TODO: type on endpoint?
               (Pageable) null, targetEntityKey.toString(), "endpoint");
  }

  @Override
  public int addMachineTag(UUID targetEntityKey, MachineTag machineTag) {
    return post(Integer.class, machineTag, targetEntityKey.toString(), "machinetag");
  }

  @Override
  public void deleteMachineTag(UUID targetEntityKey, int machineTagKey) {
    delete(targetEntityKey.toString(), "machinetag", String.valueOf(machineTagKey));
  }

  @Override
  public List<MachineTag> listMachineTags(UUID targetEntityKey) {
    return get(GenericTypes.LIST_MACHINETAG, null, null, (Pageable) null, targetEntityKey.toString(), "machinetag");
  }

  @Override
  public int addIdentifier(UUID targetEntityKey, Identifier identifier) {
    return post(Integer.class, identifier, targetEntityKey.toString(), "identifier");
  }

  @Override
  public void deleteIdentifier(UUID targetEntityKey, int identifierKey) {
    delete(targetEntityKey.toString(), "identifier", String.valueOf(identifierKey));
  }

  @Override
  public List<Identifier> listIdentifiers(UUID targetEntityKey) {
    return get(GenericTypes.LIST_IDENTIFIER, null, null,
               // TODO: identifier type?
               (Pageable) null, targetEntityKey.toString(), "identifier");
  }

  @Override
  public int addComment(UUID targetEntityKey, Comment comment) {
    return post(Integer.class, comment, targetEntityKey.toString(), "comment");
  }

  @Override
  public void deleteComment(UUID targetEntityKey, int commentKey) {
    delete(targetEntityKey.toString(), "comment", String.valueOf(commentKey));
  }

  @Override
  public List<Comment> listComments(UUID targetEntityKey) {
    return get(GenericTypes.LIST_COMMENT, null, null, (Pageable) null, targetEntityKey.toString(), "comment");
  }

}
