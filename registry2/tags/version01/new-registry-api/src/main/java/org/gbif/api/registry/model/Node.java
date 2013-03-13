package org.gbif.api.registry.model;

import org.gbif.api.registry.vocabulary.Continent;
import org.gbif.api.registry.vocabulary.GbifRegion;
import org.gbif.api.registry.vocabulary.NodeType;
import org.gbif.api.registry.vocabulary.ParticipationStatus;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;


/**
 * A GBIF participant node.
 */
public class Node implements NetworkEntity, Contactable, Taggable, MachineTaggable, Commentable {

  private UUID key;
  private NodeType type;
  private ParticipationStatus participationStatus;
  private GbifRegion gbifRegion;
  private Continent continent;
  private String title;
  private String description;
  private Language language;
  private String email;
  private String phone;
  private URI homepage;
  private URI logoUrl;
  private String address;
  private String city;
  private String province;
  private Country country;
  private String postalCode;
  private String createdBy;
  private String modifiedBy;
  private Date created;
  private Date modified;
  private Date deleted;
  private List<Contact> contacts = Lists.newArrayList();
  private List<MachineTag> machineTags = Lists.newArrayList();
  private List<Tag> tags = Lists.newArrayList();
  private List<Comment> comments = Lists.newArrayList();


  @Override
  public UUID getKey() {
    return key;
  }

  @Override
  public void setKey(UUID key) {
    this.key = key;
  }

  @NotNull
  public NodeType getType() {
    return type;
  }

  public void setType(NodeType type) {
    this.type = type;
  }

  @NotNull
  public ParticipationStatus getParticipationStatus() {
    return participationStatus;
  }

  public void setParticipationStatus(ParticipationStatus participationStatus) {
    this.participationStatus = participationStatus;
  }

  @Nullable
  public GbifRegion getGbifRegion() {
    return gbifRegion;
  }

  public void setGbifRegion(GbifRegion gbifRegion) {
    this.gbifRegion = gbifRegion;
  }

  @NotNull
  public Continent getContinent() {
    return continent;
  }

  public void setContinent(Continent continent) {
    this.continent = continent;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @NotNull
  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  @Nullable
  @Size(min = 5)
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Nullable
  @Size(min = 5)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Nullable
  public URI getHomepage() {
    return homepage;
  }

  public void setHomepage(URI homepage) {
    this.homepage = homepage;
  }

  @Nullable
  public URI getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(URI logoUrl) {
    this.logoUrl = logoUrl;
  }

  @Nullable
  @Size(min = 1)
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Nullable
  @Size(min = 1)
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  @Nullable
  @Size(min = 1)
  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  @Nullable
  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  @Nullable
  @Size(min = 1)
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @NotNull
  @Size(min = 3)
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @NotNull
  @Size(min = 3)
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  public void setCreated(Date created) {
    this.created = created;
  }

  @Override
  public Date getModified() {
    return modified;
  }

  @Override
  public void setModified(Date modified) {
    this.modified = modified;
  }

  @Override
  public Date getDeleted() {
    return deleted;
  }

  @Override
  public void setDeleted(Date deleted) {
    this.deleted = deleted;
  }

  @Override
  public List<Contact> getContacts() {
    return contacts;
  }

  @Override
  public void setContacts(List<Contact> contacts) {
    this.contacts = contacts;
  }

  @Override
  public List<MachineTag> getMachineTags() {
    return machineTags;
  }

  @Override
  public void setMachineTags(List<MachineTag> machineTags) {
    this.machineTags = machineTags;
  }

  @Override
  public List<Tag> getTags() {
    return tags;
  }

  @Override
  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  @Override
  public List<Comment> getComments() {
    return comments;
  }

  @Override
  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("key", key).add("type", type)
      .add("participationStatus", participationStatus).add("gbifRegion", gbifRegion).add("continent", continent)
      .add("title", title).add("description", description).add("language", language).add("email", email)
      .add("phone", phone).add("homepage", homepage).add("logoUrl", logoUrl).add("address", address).add("city", city)
      .add("province", province).add("country", country).add("postalCode", postalCode).add("createdBy", createdBy)
      .add("modifiedBy", modifiedBy).add("created", created).add("modified", modified).add("deleted", deleted)
      .add("contacts", contacts).add("machineTags", machineTags).add("tags", tags).add("comments", comments).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, type, participationStatus, gbifRegion, continent, title, description, language, email,
      phone, homepage, logoUrl, address, city, province, country, postalCode, createdBy, modifiedBy, created, modified,
      deleted, contacts, machineTags, tags, comments);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Node) {
      Node that = (Node) object;
      return Objects.equal(this.key, that.key) && Objects.equal(this.type, that.type)
        && Objects.equal(this.participationStatus, that.participationStatus)
        && Objects.equal(this.gbifRegion, that.gbifRegion) && Objects.equal(this.continent, that.continent)
        && Objects.equal(this.title, that.title) && Objects.equal(this.description, that.description)
        && Objects.equal(this.language, that.language) && Objects.equal(this.email, that.email)
        && Objects.equal(this.phone, that.phone) && Objects.equal(this.homepage, that.homepage)
        && Objects.equal(this.logoUrl, that.logoUrl) && Objects.equal(this.address, that.address)
        && Objects.equal(this.city, that.city) && Objects.equal(this.province, that.province)
        && Objects.equal(this.country, that.country) && Objects.equal(this.postalCode, that.postalCode)
        && Objects.equal(this.createdBy, that.createdBy) && Objects.equal(this.modifiedBy, that.modifiedBy)
        && Objects.equal(this.created, that.created) && Objects.equal(this.modified, that.modified)
        && Objects.equal(this.deleted, that.deleted) && Objects.equal(this.contacts, that.contacts)
        && Objects.equal(this.machineTags, that.machineTags) && Objects.equal(this.tags, that.tags)
        && Objects.equal(this.comments, that.comments);
    }
    return false;
  }


}