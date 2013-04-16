package org.gbif.api.registry.model;

import org.gbif.api.registry.vocabulary.EndpointType;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Endpoint {

  private Integer key;
  private EndpointType type;
  private String url;
  private String description;
  private String createdBy;
  private String modifiedBy;
  private Date created;
  private Date modified;
  private List<MachineTag> machineTags = Lists.newArrayList();

  @Min(1)
  public Integer getKey() {
    return key;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  @NotNull
  public EndpointType getType() {
    return type;
  }

  public void setType(EndpointType type) {
    this.type = type;
  }

  @Nullable
  @Size(min = 10)
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Nullable
  @Size(min = 10)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public List<MachineTag> getMachineTags() {
    return machineTags;
  }

  public void setMachineTags(List<MachineTag> machineTags) {
    this.machineTags = machineTags;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("key", key).add("type", type).add("url", url)
      .add("description", description).add("createdBy", createdBy).add("modifiedBy", modifiedBy)
      .add("created", created).add("modified", modified).add("machineTags", machineTags).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, type, url, description, createdBy, modifiedBy, created, modified, machineTags);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Endpoint) {
      Endpoint that = (Endpoint) object;
      return Objects.equal(this.key, that.key) && Objects.equal(this.type, that.type)
        && Objects.equal(this.url, that.url) && Objects.equal(this.description, that.description)
        && Objects.equal(this.createdBy, that.createdBy) && Objects.equal(this.modifiedBy, that.modifiedBy)
        && Objects.equal(this.created, that.created) && Objects.equal(this.modified, that.modified)
        && Objects.equal(this.machineTags, that.machineTags);
    }
    return false;
  }


}
