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
package org.gbif.registry.ws.resources;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry.Comment;
import org.gbif.api.model.registry.Contact;
import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Identifier;
import org.gbif.api.model.registry.MachineTag;
import org.gbif.api.model.registry.NetworkEntity;
import org.gbif.api.model.registry.PostPersist;
import org.gbif.api.model.registry.PrePersist;
import org.gbif.api.model.registry.Tag;
import org.gbif.api.service.registry.NetworkEntityService;
import org.gbif.registry.events.CreateEvent;
import org.gbif.registry.events.DeleteEvent;
import org.gbif.registry.events.UpdateEvent;
import org.gbif.registry.persistence.WithMyBatis;
import org.gbif.registry.persistence.mapper.BaseNetworkEntityMapper;
import org.gbif.registry.persistence.mapper.CommentMapper;
import org.gbif.registry.persistence.mapper.ContactMapper;
import org.gbif.registry.persistence.mapper.EndpointMapper;
import org.gbif.registry.persistence.mapper.IdentifierMapper;
import org.gbif.registry.persistence.mapper.MachineTagMapper;
import org.gbif.registry.persistence.mapper.TagMapper;
import org.gbif.registry.ws.guice.Trim;
import org.gbif.ws.server.interceptor.NullToNotFound;
import org.gbif.ws.util.ExtraMediaTypes;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import org.apache.bval.guice.Validate;
import org.mybatis.guice.transactional.Transactional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Provides a skeleton implementation of the following.
 * <ul>
 * <li>Base CRUD operations for a network entity</li>
 * <li>Comment operations</li>
 * <li>Contact operations (in addition to BaseNetworkEntityResource)</li>
 * <li>Endpoint operations (in addition to BaseNetworkEntityResource)</li>
 * <li>Identifier operations (in addition to BaseNetworkEntityResource2)</li>
 * <li>MachineTag operations</li>
 * <li>Tag operations</li>
 * </ul>
 * 
 * @param <T> The type of resource that is under CRUD
 */
@Produces({MediaType.APPLICATION_JSON, ExtraMediaTypes.APPLICATION_JAVASCRIPT})
@Consumes(MediaType.APPLICATION_JSON)
public class BaseNetworkEntityResource<T extends NetworkEntity> implements NetworkEntityService<T> {

  protected static final String ADMIN_ROLE = "ADMIN";
  private final BaseNetworkEntityMapper<T> mapper;
  private final CommentMapper commentMapper;
  private final MachineTagMapper machineTagMapper;
  private final TagMapper tagMapper;
  private final ContactMapper contactMapper;
  private final EndpointMapper endpointMapper;
  private final IdentifierMapper identifierMapper;
  private final Class<T> objectClass;
  private final EventBus eventBus;

  protected BaseNetworkEntityResource(
    BaseNetworkEntityMapper<T> mapper,
    CommentMapper commentMapper,
    ContactMapper contactMapper,
    EndpointMapper endpointMapper,
    IdentifierMapper identifierMapper,
    MachineTagMapper machineTagMapper,
    TagMapper tagMapper,
    Class<T> objectClass,
    EventBus eventBus) {
    this.mapper = mapper;
    this.commentMapper = commentMapper;
    this.machineTagMapper = machineTagMapper;
    this.tagMapper = tagMapper;
    this.contactMapper = contactMapper;
    this.endpointMapper = endpointMapper;
    this.identifierMapper = identifierMapper;
    this.objectClass = objectClass;
    this.eventBus = eventBus;
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled fields for createdBy and modifiedBy. It then creates the entity.
   * 
   * @param entity entity that extends NetworkEntity
   * @param security SecurityContext (security related information)
   * @return key of entity created
   */
  @POST
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public UUID create(@NotNull @Trim T entity, @Context SecurityContext security) {
    entity.setCreatedBy(security.getUserPrincipal().getName());
    entity.setModifiedBy(security.getUserPrincipal().getName());
    return create(entity);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public UUID create(@Valid T entity) {
    WithMyBatis.create(mapper, entity);
    eventBus.post(CreateEvent.newInstance(entity, objectClass));
    return entity.getKey();
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the entity.
   * </br>
   * Relax content-type to wildcard to allow angularjs.
   * 
   * @param key key of entity to delete
   */
  @DELETE
  @Path("{key}")
  @Consumes(MediaType.WILDCARD)
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public void delete(@PathParam("key") UUID key) {
    T objectToDelete = get(key);
    WithMyBatis.delete(mapper, key);
    eventBus.post(DeleteEvent.newInstance(objectToDelete, objectClass));
  }

  @GET
  @Path("{key}")
  @Nullable
  @NullToNotFound
  @Validate(groups = {PostPersist.class, Default.class})
  @Override
  public T get(@PathParam("key") UUID key) {
    return WithMyBatis.get(mapper, key);
  }

  /**
   * The simple search is not mapped to a URL, but called from the root path (e.g. /dataset) when the optional query
   * parameter is given.
   */
  @Override
  public PagingResponse<T> search(String query, @Nullable Pageable page) {
    page = page == null ? new PagingRequest() : page;
    // trim and handle null from given input
    String q = Strings.nullToEmpty(CharMatcher.WHITESPACE.trimFrom(query));
    return WithMyBatis.search(mapper, q, page);
  }

  @Override
  public PagingResponse<T> list(@Nullable Pageable page) {
    page = page == null ? new PagingRequest() : page;
    return WithMyBatis.list(mapper, page);
  }

  /**
   * This method ensures that the path variable for the key matches the entity's key, ensures that the caller is
   * authorized to perform the action and then adds the server controlled field modifiedBy.
   * 
   * @param key key of entity to update
   * @param entity entity that extends NetworkEntity
   * @param security SecurityContext (security related information)
   */
  @PUT
  @Path("{key}")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public void update(@PathParam("key") UUID key, @NotNull @Trim T entity, @Context SecurityContext security) {
    checkArgument(key.equals(entity.getKey()), "Provided entity must have the same key as the resource URL");
    entity.setModifiedBy(security.getUserPrincipal().getName());
    update(entity);
  }

  @Validate(groups = {PostPersist.class, Default.class})
  @Override
  public void update(@Valid T entity) {
    T oldEntity = get(entity.getKey());
    WithMyBatis.update(mapper, entity);
    eventBus.post(UpdateEvent.newInstance(entity, oldEntity, objectClass));
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled fields for createdBy and modifiedBy.
   * 
   * @param targetEntityKey key of target entity to add comment to
   * @param comment Comment to add
   * @param security SecurityContext (security related information)
   * @return key of Comment created
   */
  @POST
  @Path("{key}/comment")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public int addComment(@NotNull @PathParam("key") UUID targetEntityKey, @NotNull @Trim Comment comment,
    @Context SecurityContext security) {
    comment.setCreatedBy(security.getUserPrincipal().getName());
    comment.setModifiedBy(security.getUserPrincipal().getName());
    return addComment(targetEntityKey, comment);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addComment(UUID targetEntityKey, @Valid Comment comment) {
    return WithMyBatis.addComment(commentMapper, mapper, targetEntityKey, comment);
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the Comment.
   * 
   * @param targetEntityKey key of target entity to delete comment from
   * @param commentKey key of Comment to delete
   */
  @DELETE
  @Path("{key}/comment/{commentKey}")
  @Consumes(MediaType.WILDCARD)
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public void deleteComment(@NotNull @PathParam("key") UUID targetEntityKey, @PathParam("commentKey") int commentKey) {
    WithMyBatis.deleteComment(mapper, targetEntityKey, commentKey);
  }

  @GET
  @Path("{key}/comment")
  @Override
  public List<Comment> listComments(@NotNull @PathParam("key") UUID targetEntityKey) {
    return WithMyBatis.listComments(mapper, targetEntityKey);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled field for createdBy.
   * 
   * @param targetEntityKey key of target entity to add MachieTag to
   * @param machineTag MachineTag to add
   * @param security SecurityContext (security related information)
   * @return key of MachineTag created
   */
  @POST
  @Path("{key}/machinetag")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public int addMachineTag(@PathParam("key") UUID targetEntityKey, @NotNull @Trim MachineTag machineTag,
    @Context SecurityContext security) {
    machineTag.setCreatedBy(security.getUserPrincipal().getName());
    return addMachineTag(targetEntityKey, machineTag);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addMachineTag(UUID targetEntityKey, @Valid MachineTag machineTag) {
    return WithMyBatis.addMachineTag(machineTagMapper, mapper, targetEntityKey, machineTag);
  }

  @Override
  public int addMachineTag(
    @NotNull UUID targetEntityKey, @NotNull String namespace, @NotNull String name, @NotNull String value) {
    MachineTag machineTag = new MachineTag();
    machineTag.setNamespace(namespace);
    machineTag.setName(name);
    machineTag.setValue(value);
    return addMachineTag(targetEntityKey, machineTag);
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the MachineTag.
   * 
   * @param targetEntityKey key of target entity to delete MachineTag from
   * @param machineTagKey key of MachineTag to delete
   */
  @DELETE
  @Path("{key}/machinetag/{machinetagKey}")
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.WILDCARD)
  @Override
  public void deleteMachineTag(@PathParam("key") UUID targetEntityKey, @PathParam("machinetagKey") int machineTagKey) {
    WithMyBatis.deleteMachineTag(mapper, targetEntityKey, machineTagKey);
  }

  @Override
  public void deleteMachineTags(@NotNull UUID targetEntityKey, @NotNull String namespace) {
    // TODO: Write implementation
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void deleteMachineTags(
    @NotNull UUID targetEntityKey, @NotNull String namespace, @NotNull String name
    ) {
    // TODO: Write implementation
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GET
  @Path("{key}/machinetag")
  @Override
  public List<MachineTag> listMachineTags(@PathParam("key") UUID targetEntityKey) {
    return WithMyBatis.listMachineTags(mapper, targetEntityKey);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled fields for createdBy.
   * 
   * @param targetEntityKey key of target entity to add Tag to
   * @param value Tag to add
   * @param security SecurityContext (security related information)
   * @return key of Tag created
   */
  @POST
  @Path("{key}/tag")
  @RolesAllowed(ADMIN_ROLE)
  public int addTag(@PathParam("key") UUID targetEntityKey, @NotNull @Size(min = 1) String value,
    @Context SecurityContext security) {
    Tag tag = new Tag(value, security.getUserPrincipal().getName());
    return addTag(targetEntityKey, tag);
  }

  @Override
  public int addTag(@NotNull UUID targetEntityKey, @NotNull String value) {
    Tag tag = new Tag();
    tag.setValue(value);
    return addTag(targetEntityKey, tag);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addTag(UUID targetEntityKey, @Valid Tag tag) {
    return WithMyBatis.addTag(tagMapper, mapper, targetEntityKey, tag);
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the Tag.
   * 
   * @param taggedEntityKey key of target entity to delete Tag from
   * @param tagKey key of Tag to delete
   */
  @DELETE
  @Path("{key}/tag/{tagKey}")
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.WILDCARD)
  @Override
  public void deleteTag(@PathParam("key") UUID taggedEntityKey, @PathParam("tagKey") int tagKey) {
    WithMyBatis.deleteTag(mapper, taggedEntityKey, tagKey);
  }

  @GET
  @Path("{key}/tag")
  @Override
  public List<Tag> listTags(@PathParam("key") UUID taggedEntityKey, @QueryParam("owner") String owner) {
    return WithMyBatis.listTags(mapper, taggedEntityKey, owner);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled fields for createdBy and modifiedBy.
   * 
   * @param targetEntityKey key of target entity to add Contact to
   * @param contact Contact to add
   * @param security SecurityContext (security related information)
   * @return key of Contact created
   */
  @POST
  @Path("{key}/contact")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public int addContact(@PathParam("key") UUID targetEntityKey, @NotNull @Trim Contact contact,
    @Context SecurityContext security) {
    contact.setCreatedBy(security.getUserPrincipal().getName());
    contact.setModifiedBy(security.getUserPrincipal().getName());
    return addContact(targetEntityKey, contact);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addContact(UUID targetEntityKey, @Valid Contact contact) {
    return WithMyBatis.addContact(contactMapper, mapper, targetEntityKey, contact);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled field for modifiedBy.
   * 
   * @param targetEntityKey key of target entity to update contact
   * @param contactKey key of Contact to update
   * @param contact updated Contact
   * @param security SecurityContext (security related information)
   */
  @PUT
  @Path("{key}/contact/{contactKey}")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public void updateContact(@PathParam("key") UUID targetEntityKey, @PathParam("contactKey") int contactKey,
    @NotNull @Trim Contact contact, @Context SecurityContext security) {
    // for safety, and to match a nice RESTful URL structure
    Preconditions.checkArgument(Integer.valueOf(contactKey).equals(contact.getKey()),
      "Provided contact (key) does not match the path provided");
    contact.setModifiedBy(security.getUserPrincipal().getName());
    updateContact(targetEntityKey, contact);
  }

  @Validate(groups = {PostPersist.class, Default.class})
  @Override
  public void updateContact(UUID targetEntityKey, @Valid Contact contact) {
    WithMyBatis.updateContact(contactMapper, mapper, targetEntityKey, contact);
  }

  /**
   * This method ensures that the caller is authorized to perform the action.
   * 
   * @param targetEntityKey key of target entity to delete Contact from
   * @param contactKey key of Contact to delete
   */
  @DELETE
  @Path("{key}/contact/{contactKey}")
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.WILDCARD)
  @Override
  public void deleteContact(@PathParam("key") UUID targetEntityKey, @PathParam("contactKey") int contactKey) {
    WithMyBatis.deleteContact(mapper, targetEntityKey, contactKey);
  }

  @GET
  @Path("{key}/contact")
  @Override
  public List<Contact> listContacts(@PathParam("key") UUID targetEntityKey) {
    return WithMyBatis.listContacts(mapper, targetEntityKey);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled fields for createdBy and modifiedBy.
   * 
   * @param targetEntityKey key of target entity to add Endpoint to
   * @param endpoint Endpoint to add
   * @param security SecurityContext (security related information)
   * @return key of Endpoint created
   */
  @POST
  @Path("{key}/endpoint")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public int addEndpoint(@PathParam("key") UUID targetEntityKey, @NotNull @Trim Endpoint endpoint,
    @Context SecurityContext security) {
    endpoint.setCreatedBy(security.getUserPrincipal().getName());
    endpoint.setModifiedBy(security.getUserPrincipal().getName());
    return addEndpoint(targetEntityKey, endpoint);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addEndpoint(UUID targetEntityKey, @Valid Endpoint endpoint) {
    // This WILL create machine tags if they exist as nested entities, which can safely be done since endpoint
    // is immutable.
    return WithMyBatis.addEndpoint(endpointMapper, mapper, targetEntityKey, endpoint, machineTagMapper);
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the Endpoint.
   * 
   * @param targetEntityKey key of target entity to delete Endpoint from
   * @param endpointKey key of Endpoint to delete
   */
  @DELETE
  @Path("{key}/endpoint/{endpointKey}")
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.WILDCARD)
  @Override
  public void deleteEndpoint(@PathParam("key") UUID targetEntityKey, @PathParam("endpointKey") int endpointKey) {
    WithMyBatis.deleteEndpoint(mapper, targetEntityKey, endpointKey);
  }

  @GET
  @Path("{key}/endpoint")
  @Override
  public List<Endpoint> listEndpoints(@PathParam("key") UUID targetEntityKey) {
    return WithMyBatis.listEndpoints(mapper, targetEntityKey);
  }

  /**
   * This method ensures that the caller is authorized to perform the action and then adds the server
   * controlled field for createdBy.
   * 
   * @param targetEntityKey key of target entity to add Identifier to
   * @param identifier Identifier to add
   * @param security SecurityContext (security related information)
   * @return key of Identifier created
   */
  @POST
  @Path("{key}/identifier")
  @Trim
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  public int addIdentifier(@PathParam("key") UUID targetEntityKey, @NotNull @Trim Identifier identifier,
    @Context SecurityContext security) {
    identifier.setCreatedBy(security.getUserPrincipal().getName());
    return addIdentifier(targetEntityKey, identifier);
  }

  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public int addIdentifier(UUID targetEntityKey, @Valid Identifier identifier) {
    return WithMyBatis.addIdentifier(identifierMapper, mapper, targetEntityKey, identifier);
  }

  /**
   * This method ensures that the caller is authorized to perform the action, and then deletes the Identifier.
   * 
   * @param targetEntityKey key of target entity to delete Identifier from
   * @param identifierKey key of Identifier to delete
   */
  @DELETE
  @Path("{key}/identifier/{identifierKey}")
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.WILDCARD)
  @Override
  public void deleteIdentifier(@PathParam("key") UUID targetEntityKey, @PathParam("identifierKey") int identifierKey) {
    WithMyBatis.deleteIdentifier(mapper, targetEntityKey, identifierKey);
  }


  @GET
  @Path("{key}/identifier")
  @Override
  public List<Identifier> listIdentifiers(@PathParam("key") UUID targetEntityKey) {
    return WithMyBatis.listIdentifiers(mapper, targetEntityKey);
  }

  /**
   * Null safe builder to construct a paging response.
   * 
   * @param page page to create response for, can be null
   */
  protected static <T> PagingResponse<T> pagingResponse(@Nullable Pageable page, Long count, List<T> result) {
    if (page == null) {
      // use default request
      page = new PagingRequest();
    }
    return new PagingResponse<T>(page, count, result);
  }

}
