package org.gbif.registry.ws.client;

import org.gbif.api.model.common.paging.Pageable;


/**
 * Client-side implementation to the NodeService.
 */
public class NodeWsClient extends BaseWsGetClient<Node, UUID> implements NodeService {

  @Inject
  public NodeWsClient(@RegistryWs WebResource resource) {
    super(Node.class, resource.path("node"), (ClientFilter) null);
  }

  @Override
  public UUID create(Node entity) {
    return super.post(UUID.class, entity, "/");
  }

  @Override
  public void delete(UUID key) {
    super.delete(key.toString());
  }

  @Override
  public Node get(UUID key) {
    return super.get(key.toString());
  }

  @Override
  public PagingResponse<Node> list(Pageable page) {
    return super.get(GenericTypes.PAGING_NODE,
      (Locale) null,
      (MultivaluedMap<String, String>) null,
      page);
  }

  @Override
  public void update(Node entity) {
    Preconditions.checkArgument(entity.getKey() != null, "An entity must have a key to be updated");
    super.put(entity, entity.getKey().toString());
  }

  @Override
  public int addTag(UUID targetEntityKey, String value) {
    // post the value to .../uuid/tag and expect an int back
    return super.post(Integer.class, (Object) value, targetEntityKey.toString(), "tag");
  }

  @Override
  public void deleteTag(UUID targetEntityKey, int componentKey) {
    super.delete(targetEntityKey.toString(), "tag", String.valueOf(componentKey));
  }

  @Override
  public List<Tag> listTags(UUID targetEntityKey, String owner) {
    return super.get(GenericTypes.LIST_TAG,
      (Locale) null,
      (MultivaluedMap<String, String>) null, // TODO add owner here
      (Pageable) null,
      targetEntityKey.toString(), "tag");
  }

  @Override
  public int addContact(UUID targetEntityKey, Contact component) {
    // post the contact to .../uuid/contact and expect an int back
    return super.post(Integer.class, component, targetEntityKey.toString(), "contact");
  }

  @Override
  public void deleteContact(UUID targetEntityKey, int componentKey) {
    super.delete(targetEntityKey.toString(), "contact", String.valueOf(componentKey));
  }

  @Override
  public List<Contact> listContacts(UUID targetEntityKey) {
    return super.get(GenericTypes.LIST_CONTACT,
      (Locale) null,
      (MultivaluedMap<String, String>) null, // TODO: type on contact?
      (Pageable) null,
      targetEntityKey.toString(), "contact");
  }

  @Override
  public int addMachineTag(UUID targetEntityKey, MachineTag component) {
    return super.post(Integer.class, component, targetEntityKey.toString(), "machinetag");
  }

  @Override
  public void deleteMachineTag(UUID targetEntityKey, int componentKey) {
    super.delete(targetEntityKey.toString(), "machinetag", String.valueOf(componentKey));    
  }

  @Override
  public List<MachineTag> listMachineTags(UUID targetEntityKey) {
    return super.get(GenericTypes.LIST_MACHINETAG,
      (Locale) null,
      (MultivaluedMap<String, String>) null,
      (Pageable) null,
      targetEntityKey.toString(), "machinetag");
  }

  @Override
  public int addComment(UUID targetEntityKey, Comment component) {
    return super.post(Integer.class, component, targetEntityKey.toString(), "comment");
  }

  @Override
  public void deleteComment(UUID targetEntityKey, int componentKey) {
    super.delete(targetEntityKey.toString(), "comment", String.valueOf(componentKey));  
  }

  @Override
  public List<Comment> listComments(UUID targetEntityKey) {
    return super.get(GenericTypes.LIST_COMMENT,
      (Locale) null,
      (MultivaluedMap<String, String>) null,
      (Pageable) null,
      targetEntityKey.toString(), "comment");
  }
  
  @Override
  public PagingResponse<Organization> organizationsEndorsedBy(UUID nodeKey, Pageable page) {
    return super.get(GenericTypes.PAGING_ORGANIZATION,
      (Locale) null,
      (MultivaluedMap<String, String>) null,
      page,
      nodeKey.toString(), "organization");
  }

  @Override
  public PagingResponse<Organization> pendingEndorsements(Pageable page) {
    return super.get(GenericTypes.PAGING_ORGANIZATION,
      (Locale) null,
      (MultivaluedMap<String, String>) null,
      page,
      "pendingEndorsement");
  }  
}
