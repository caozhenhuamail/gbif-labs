package org.gbif.registry.persistence;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.registry.model.Comment;
import org.gbif.api.registry.model.Contact;
import org.gbif.api.registry.model.Endpoint;
import org.gbif.api.registry.model.Identifier;
import org.gbif.api.registry.model.MachineTag;
import org.gbif.api.registry.model.NetworkEntity;
import org.gbif.api.registry.model.Tag;
import org.gbif.api.registry.vocabulary.ContactType;
import org.gbif.registry.persistence.mapper.AccessibleMapper;
import org.gbif.registry.persistence.mapper.CommentMapper;
import org.gbif.registry.persistence.mapper.CommentableMapper;
import org.gbif.registry.persistence.mapper.ContactMapper;
import org.gbif.registry.persistence.mapper.ContactableMapper;
import org.gbif.registry.persistence.mapper.EndpointMapper;
import org.gbif.registry.persistence.mapper.IdentifiableMapper;
import org.gbif.registry.persistence.mapper.IdentifierMapper;
import org.gbif.registry.persistence.mapper.MachineTagMapper;
import org.gbif.registry.persistence.mapper.MachineTaggableMapper;
import org.gbif.registry.persistence.mapper.NetworkEntityMapper;
import org.gbif.registry.persistence.mapper.TagMapper;
import org.gbif.registry.persistence.mapper.TaggableMapper;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import org.mybatis.guice.transactional.Transactional;

public class WithMyBatis {

  @Transactional
  public static <T extends NetworkEntity> UUID create(NetworkEntityMapper<T> mapper, T entity) {
    Preconditions.checkArgument(entity.getKey() == null, "Unable to create an entity which already has a key");
    entity.setKey(UUID.randomUUID());
    mapper.create(entity);
    return entity.getKey();
  }

  public static <T extends NetworkEntity> T get(NetworkEntityMapper<T> mapper, UUID key) {
    return mapper.get(key);
  }

  @Transactional
  public static <T extends NetworkEntity> void update(NetworkEntityMapper<T> mapper, T entity) {
    Preconditions.checkNotNull(entity, "Unable to update an entity when it is not provided");
    T existing = mapper.get(entity.getKey());
    Preconditions.checkNotNull(existing, "Unable to update a non existing entity");
    Preconditions.checkArgument(existing.getDeleted() == null, "Unable to update a previously deleted entity");
    mapper.update(entity);
  }

  @Transactional
  public static <T extends NetworkEntity> void delete(NetworkEntityMapper<T> mapper, UUID key) {
    mapper.delete(key);
  }

  public static <T extends NetworkEntity> PagingResponse<T> list(NetworkEntityMapper<T> mapper, Pageable page) {
    long total = mapper.count();
    return new PagingResponse<T>(page.getOffset(), page.getLimit(), total, mapper.list(page));
  }

  @Transactional
  public static int addContact(ContactMapper contactMapper, ContactableMapper contactableMapper, UUID targetEntityKey,
    Contact contact) {
    Preconditions.checkArgument(contact.getKey() == null, "Unable to create an entity which already has a key");
    contactMapper.createContact(contact);
    contactableMapper.addContact(targetEntityKey, contact.getKey(), ContactType.ADMINISTRATIVE_POINT_OF_CONTACT, false);
    return contact.getKey();
  }

  public static void deleteContact(ContactableMapper contactableMapper, UUID targetEntityKey, int contactKey) {
    contactableMapper.deleteContact(targetEntityKey, contactKey);
  }

  public static List<Contact> listContacts(ContactableMapper contactableMapper, UUID targetEntityKey) {
    return contactableMapper.listContacts(targetEntityKey);
  }

  @Transactional
  public static int addEndpoint(EndpointMapper endpointMapper, AccessibleMapper accessibleMapper, UUID targetEntityKey,
    Endpoint endpoint) {
    Preconditions.checkArgument(endpoint.getKey() == null, "Unable to create an entity which already has a key");
    endpointMapper.createEndpoint(endpoint);
    accessibleMapper.addEndpoint(targetEntityKey, endpoint.getKey());
    return endpoint.getKey();
  }

  public static void deleteEndpoint(AccessibleMapper accessibleMapper, UUID targetEntityKey, int endpointKey) {
    accessibleMapper.deleteEndpoint(targetEntityKey, endpointKey);
  }

  public static List<Endpoint> listEndpoints(AccessibleMapper accessibleMapper, UUID targetEntityKey) {
    return accessibleMapper.listEndpoints(targetEntityKey);
  }

  public static int addMachineTag(MachineTagMapper machineTagMapper, MachineTaggableMapper machineTaggableMapper,
    UUID targetEntityKey, MachineTag machineTag) {
    Preconditions.checkArgument(machineTag.getKey() == null, "Unable to create an entity which already has a key");
    machineTagMapper.createMachineTag(machineTag);
    machineTaggableMapper.addMachineTag(targetEntityKey, machineTag.getKey());
    return machineTag.getKey();
  }

  public static void deleteMachineTag(MachineTaggableMapper machineTaggableMapper, UUID targetEntityKey,
    int machineTagKey) {
    machineTaggableMapper.deleteMachineTag(targetEntityKey, machineTagKey);
  }

  public static List<MachineTag> listMachineTags(MachineTaggableMapper machineTaggableMapper, UUID targetEntityKey) {
    return machineTaggableMapper.listMachineTags(targetEntityKey);
  }

  @Transactional
  public static int addTag(TagMapper tagMapper, TaggableMapper taggableMapper, UUID targetEntityKey, String value) {
    // Mybatis needs an object to set the key on
    Tag t = new Tag(value, "TODO: Implement with Apache shiro?");
    tagMapper.createTag(t);
    taggableMapper.addTag(targetEntityKey, t.getKey());
    return t.getKey();
  }

  public static void deleteTag(TaggableMapper taggableMapper, UUID targetEntityKey, int tagKey) {
    taggableMapper.deleteTag(targetEntityKey, tagKey);
  }

  public static List<Tag> listTags(TaggableMapper taggableMapper, UUID targetEntityKey, String owner) {
    // TODO: support the owner
    return taggableMapper.listTags(targetEntityKey);
  }

  @Transactional
  public static int addIdentifier(IdentifierMapper identifierMapper, IdentifiableMapper identifiableMapper,
    UUID targetEntityKey, Identifier identifier) {
    Preconditions.checkArgument(identifier.getKey() == null, "Unable to create an entity which already has a key");
    identifierMapper.createIdentifier(identifier);
    identifiableMapper.addIdentifier(targetEntityKey, identifier.getKey());
    return identifier.getKey();
  }

  public static void deleteIdentifier(IdentifiableMapper identifiableMapper, UUID targetEntityKey, int identifierKey) {
    identifiableMapper.deleteIdentifier(targetEntityKey, identifierKey);
  }

  public static List<Identifier> listIdentifiers(IdentifiableMapper identifiableMapper, UUID targetEntityKey) {
    return identifiableMapper.listIdentifiers(targetEntityKey);
  }

  @Transactional
  public static int addComment(CommentMapper commentMapper, CommentableMapper commentableMapper, UUID targetEntityKey,
    Comment comment) {
    Preconditions.checkArgument(comment.getKey() == null, "Unable to create an entity which already has a key");
    commentMapper.createComment(comment);
    commentableMapper.addComment(targetEntityKey, comment.getKey());
    return comment.getKey();
  }

  public static void deleteComment(CommentableMapper commentableMapper, UUID targetEntityKey, int commentKey) {
    commentableMapper.deleteComment(targetEntityKey, commentKey);
  }

  public static List<Comment> listComments(CommentableMapper commentableMapper, UUID targetEntityKey) {
    return commentableMapper.listComments(targetEntityKey);
  }

}
