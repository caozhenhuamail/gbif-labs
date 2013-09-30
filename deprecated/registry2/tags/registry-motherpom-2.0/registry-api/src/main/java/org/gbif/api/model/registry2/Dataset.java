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
package org.gbif.api.model.registry2;

import org.gbif.api.model.registry2.eml.Collection;
import org.gbif.api.model.registry2.eml.DataDescription;
import org.gbif.api.model.registry2.eml.KeywordCollection;
import org.gbif.api.model.registry2.eml.Project;
import org.gbif.api.model.registry2.eml.SamplingDescription;
import org.gbif.api.model.registry2.eml.TaxonomicCoverages;
import org.gbif.api.model.registry2.eml.curatorial.CuratorialUnitComposite;
import org.gbif.api.model.registry2.eml.geospatial.GeospatialCoverage;
import org.gbif.api.model.registry2.eml.temporal.TemporalCoverage;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import org.gbif.api.vocabulary.registry2.DatasetSubtype;
import org.gbif.api.vocabulary.registry2.DatasetType;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A GBIF dataset which provides occurrence data, checklist data or metadata.
 * This Dataset class is covering all of the GBIF metadata profile, but only few properties are kept in the
 * database table:
 * <ul>
 * <li>key</li>
 * <li>parentDatasetKey</li>
 * <li>duplicateOfDatasetKey</li>
 * <li>installationKey</li>
 * <li>owningOrganizationKey</li>
 * <li>external</li>
 * <li>numConstituents</li>
 * <li>type</li>
 * <li>subtype</li>
 * <li>title</li>
 * <li>alias</li>
 * <li>abbreviation</li>
 * <li>description</li>
 * <li>language</li>
 * <li>homepage</li>
 * <li>logoUrl</li>
 * <li>citation</li>
 * <li>rights</li>
 * <li>lockedForAutoUpdate</li>
 * <li>createdBy</li>
 * <li>modifiedBy</li>
 * <li>created</li>
 * <li>modified</li>
 * <li>deleted</li>
 * </ul>
 *
 * @see <a href="http://rs.gbif.org/schema/eml-gbif-profile/dev/eml.xsd">GBIF EML Profile XML Schema</a>
 */
public class Dataset
  implements NetworkEntity, Contactable, Endpointable, MachineTaggable, Taggable, Identifiable, Commentable {

  private UUID key;
  private UUID parentDatasetKey;
  private UUID duplicateOfDatasetKey;
  private UUID installationKey;
  private UUID owningOrganizationKey;
  private boolean external;
  private int numConstituents;
  private DatasetType type;
  private DatasetSubtype subtype;
  private String title;
  private String alias;
  private String abbreviation;
  private String description;
  private Language language;
  private URI homepage;
  private URI logoUrl;
  private Citation citation = new Citation();
  private String rights;
  private boolean lockedForAutoUpdate;
  private String createdBy;
  private String modifiedBy;
  private Date created;
  private Date modified;
  private Date deleted;
  private List<Contact> contacts = Lists.newArrayList();
  private List<Endpoint> endpoints = Lists.newArrayList();
  private List<MachineTag> machineTags = Lists.newArrayList();
  private List<Tag> tags = Lists.newArrayList();
  private List<Identifier> identifiers = Lists.newArrayList();
  private List<Comment> comments = Lists.newArrayList();
  // EML specific properties which are not persisted on the dataset table!
  private List<Citation> bibliographicCitations = Lists.newArrayList();
  private List<CuratorialUnitComposite> curatorialUnits = Lists.newArrayList();
  private List<TaxonomicCoverages> taxonomicCoverages = Lists.newArrayList();
  private String geographicCoverageDescription;
  private List<GeospatialCoverage> geographicCoverages = Lists.newArrayList();
  private List<TemporalCoverage> temporalCoverages = Lists.newArrayList();
  private List<KeywordCollection> keywordCollections = Lists.newArrayList();
  private Project project;
  private SamplingDescription samplingDescription;
  private Set<Country> countryCoverage = Sets.newHashSet();
  private List<Collection> collections = Lists.newArrayList();
  private List<DataDescription> dataDescriptions = Lists.newArrayList();
  private Language dataLanguage;
  private String purpose;
  private String additionalInfo;
  private Date pubDate;

  @Override
  public UUID getKey() {
    return key;
  }

  @Override
  /**
   * Persisted in the database table.
   */
  public void setKey(UUID key) {
    this.key = key;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  /**
   * Persisted in the database table.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  /**
   * Persisted in the database table.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  /**
   * Autoassigned in the database table, ignored when persisted.
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  @Override
  public Date getModified() {
    return modified;
  }

  @Override
  /**
   * Persisted in the database table.
   */
  public void setModified(Date modified) {
    this.modified = modified;
  }

  @Override
  public Date getDeleted() {
    return deleted;
  }

  @Override
  /**
   * Persisted in the database table.
   */
  public void setDeleted(Date deleted) {
    this.deleted = deleted;
  }

  @Nullable
  public UUID getParentDatasetKey() {
    return parentDatasetKey;
  }

  /**
   * Persisted in the database table.
   */
  public void setParentDatasetKey(UUID parentDatasetKey) {
    this.parentDatasetKey = parentDatasetKey;
  }

  /**
   * TODO: Someone verify that what I've written here makes sense
   * If a dataset is registered with GBIF through more than one place we'll mark all but one as a duplicate by pointing
   * it to the canonical dataset. That is done using this field. If it is {@code null} then this is not a known
   * duplicate.
   */
  @Nullable
  public UUID getDuplicateOfDatasetKey() {
    return duplicateOfDatasetKey;
  }

  /**
   * Persisted in the database table.
   */
  public void setDuplicateOfDatasetKey(UUID duplicateOfDatasetKey) {
    this.duplicateOfDatasetKey = duplicateOfDatasetKey;
  }

  @NotNull
  public UUID getInstallationKey() {
    return installationKey;
  }

  /**
   * Persisted in the database table.
   */
  public void setInstallationKey(UUID installationKey) {
    this.installationKey = installationKey;
  }

  @NotNull
  public UUID getOwningOrganizationKey() {
    return owningOrganizationKey;
  }

  /**
   * Persisted in the database table.
   */
  public void setOwningOrganizationKey(UUID owningOrganizationKey) {
    this.owningOrganizationKey = owningOrganizationKey;
  }

  public boolean isExternal() {
    return external;
  }

  /**
   * Persisted in the database table.
   */
  public void setExternal(boolean external) {
    this.external = external;
  }

  @Min(0)
  public int getNumConstituents() {
    return numConstituents;
  }

  /**
   * Not persisted in the database table, but calculated on the fly.
   */
  public void setNumConstituents(int numConstituents) {
    this.numConstituents = numConstituents;
  }

  @NotNull
  public DatasetType getType() {
    return type;
  }

  /**
   * Persisted in the database table.
   */
  public void setType(DatasetType type) {
    this.type = type;
  }

  @Nullable
  public DatasetSubtype getSubtype() {
    return subtype;
  }

  /**
   * Persisted in the database table.
   */
  public void setSubtype(DatasetSubtype subtype) {
    this.subtype = subtype;
  }

  /**
   * TODO: Document what this is
   */
  @Nullable
  @Size(min = 2, max = 50)
  public String getAlias() {
    return alias;
  }

  /**
   * Persisted in the database table.
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * TODO: Document what this is
   * TODO: are both alias & abbreviation needed?
   */
  @Nullable
  @Size(min = 1, max = 50)
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Persisted in the database table.
   */
  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  @NotNull
  public Language getLanguage() {
    return language;
  }

  /**
   * Persisted in the database table.
   */
  public void setLanguage(Language language) {
    this.language = language;
  }

  @Nullable
  public URI getHomepage() {
    return homepage;
  }

  /**
   * Persisted in the database table.
   */
  public void setHomepage(URI homepage) {
    this.homepage = homepage;
  }

  @Nullable
  public URI getLogoUrl() {
    return logoUrl;
  }

  /**
   * Persisted in the database table.
   */
  public void setLogoUrl(URI logoUrl) {
    this.logoUrl = logoUrl;
  }

  /**
   * The exact form of how to citate this dataset.
   */
  @Nullable
  public Citation getCitation() {
    return citation;
  }

  /**
   * Persisted in the database table.
   */
  public void setCitation(Citation citation) {
    this.citation = citation;
  }

  /**
   * Any kind of (copy)rights/IPR statements that apply to the datasets data.
   */
  @Nullable
  @Size(min = 1)
  public String getRights() {
    return rights;
  }

  /**
   * Persisted in the database table.
   */
  public void setRights(String rights) {
    this.rights = rights;
  }

  public boolean isLockedForAutoUpdate() {
    return lockedForAutoUpdate;
  }

  /**
   * Persisted in the database table.
   */
  public void setLockedForAutoUpdate(boolean lockedForAutoUpdate) {
    this.lockedForAutoUpdate = lockedForAutoUpdate;
  }

  @Size(min = 3)
  public String getCreatedBy() {
    return createdBy;
  }

  /**
   * Persisted in the database table.
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Size(min = 3)
  public String getModifiedBy() {
    return modifiedBy;
  }

  /**
   * Persisted in the database table.
   */
  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
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
  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  @Override
  public void setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
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
  public List<Identifier> getIdentifiers() {
    return identifiers;
  }

  @Override
  public void setIdentifiers(List<Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  @Override
  public List<Comment> getComments() {
    return comments;
  }

  @Override
  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  public List<Citation> getBibliographicCitations() {
    return bibliographicCitations;
  }

  public void setBibliographicCitations(List<Citation> bibliographicCitations) {
    this.bibliographicCitations = bibliographicCitations;
  }

  public List<CuratorialUnitComposite> getCuratorialUnits() {
    return curatorialUnits;
  }

  public void setCuratorialUnits(List<CuratorialUnitComposite> curatorialUnits) {
    this.curatorialUnits = curatorialUnits;
  }

  public List<TaxonomicCoverages> getTaxonomicCoverages() {
    return taxonomicCoverages;
  }

  public void setTaxonomicCoverages(List<TaxonomicCoverages> taxonomicCoverages) {
    this.taxonomicCoverages = taxonomicCoverages;
  }

  public String getGeographicCoverageDescription() {
    return geographicCoverageDescription;
  }

  public void setGeographicCoverageDescription(String geographicCoverageDescription) {
    this.geographicCoverageDescription = geographicCoverageDescription;
  }

  public List<GeospatialCoverage> getGeographicCoverages() {
    return geographicCoverages;
  }

  public void setGeographicCoverages(List<GeospatialCoverage> geographicCoverages) {
    this.geographicCoverages = geographicCoverages;
  }

  public List<TemporalCoverage> getTemporalCoverages() {
    return temporalCoverages;
  }

  public void setTemporalCoverages(List<TemporalCoverage> temporalCoverages) {
    this.temporalCoverages = temporalCoverages;
  }

  public List<KeywordCollection> getKeywordCollections() {
    return keywordCollections;
  }

  public void setKeywordCollections(List<KeywordCollection> keywordCollections) {
    this.keywordCollections = keywordCollections;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public SamplingDescription getSamplingDescription() {
    return samplingDescription;
  }

  public void setSamplingDescription(SamplingDescription samplingDescription) {
    this.samplingDescription = samplingDescription;
  }

  public Set<Country> getCountryCoverage() {
    return countryCoverage;
  }

  public void setCountryCoverage(Set<Country> countryCoverage) {
    this.countryCoverage = countryCoverage;
  }

  public List<Collection> getCollections() {
    return collections;
  }

  public void setCollections(List<Collection> collections) {
    this.collections = collections;
  }

  public List<DataDescription> getDataDescriptions() {
    return dataDescriptions;
  }

  public void setDataDescriptions(List<DataDescription> dataDescriptions) {
    this.dataDescriptions = dataDescriptions;
  }

  public Language getDataLanguage() {
    return dataLanguage;
  }

  public void setDataLanguage(Language dataLanguage) {
    this.dataLanguage = dataLanguage;
  }

  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  @Override
  public int hashCode() {
    return Objects
      .hashCode(key, parentDatasetKey, duplicateOfDatasetKey, installationKey, owningOrganizationKey, external,
        numConstituents, type, subtype, title, alias, abbreviation, description, language, homepage, logoUrl, citation,
        rights, lockedForAutoUpdate, createdBy, modifiedBy, created, modified, deleted, contacts, endpoints,
        machineTags, tags, identifiers, comments, bibliographicCitations, curatorialUnits, taxonomicCoverages,
        geographicCoverageDescription, geographicCoverages, temporalCoverages, keywordCollections, project,
        samplingDescription, countryCoverage, collections, dataDescriptions, dataLanguage, purpose, additionalInfo,
        pubDate);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Dataset) {
      Dataset that = (Dataset) object;
      return Objects.equal(this.key, that.key) && Objects.equal(this.parentDatasetKey, that.parentDatasetKey) && Objects
        .equal(this.duplicateOfDatasetKey, that.duplicateOfDatasetKey) && Objects
               .equal(this.installationKey, that.installationKey) && Objects
               .equal(this.owningOrganizationKey, that.owningOrganizationKey) && Objects
               .equal(this.external, that.external) && Objects.equal(this.numConstituents, that.numConstituents)
             && Objects.equal(this.type, that.type) && Objects.equal(this.subtype, that.subtype) && Objects
        .equal(this.title, that.title) && Objects.equal(this.alias, that.alias) && Objects
               .equal(this.abbreviation, that.abbreviation) && Objects.equal(this.description, that.description)
             && Objects.equal(this.language, that.language) && Objects.equal(this.homepage, that.homepage) && Objects
        .equal(this.logoUrl, that.logoUrl) && Objects.equal(this.citation, that.citation) && Objects
               .equal(this.rights, that.rights) && Objects.equal(this.lockedForAutoUpdate, that.lockedForAutoUpdate)
             && Objects.equal(this.createdBy, that.createdBy) && Objects.equal(this.modifiedBy, that.modifiedBy)
             && Objects.equal(this.created, that.created) && Objects.equal(this.modified, that.modified) && Objects
        .equal(this.deleted, that.deleted) && Objects.equal(this.contacts, that.contacts) && Objects
               .equal(this.endpoints, that.endpoints) && Objects.equal(this.machineTags, that.machineTags) && Objects
               .equal(this.tags, that.tags) && Objects.equal(this.identifiers, that.identifiers) && Objects
               .equal(this.comments, that.comments) && Objects
               .equal(this.bibliographicCitations, that.bibliographicCitations) && Objects
               .equal(this.curatorialUnits, that.curatorialUnits) && Objects
               .equal(this.taxonomicCoverages, that.taxonomicCoverages) && Objects
               .equal(this.geographicCoverageDescription, that.geographicCoverageDescription) && Objects
               .equal(this.geographicCoverages, that.geographicCoverages) && Objects
               .equal(this.temporalCoverages, that.temporalCoverages) && Objects
               .equal(this.keywordCollections, that.keywordCollections) && Objects.equal(this.project, that.project)
             && Objects.equal(this.samplingDescription, that.samplingDescription) && Objects
        .equal(this.countryCoverage, that.countryCoverage) && Objects.equal(this.collections, that.collections)
             && Objects.equal(this.dataDescriptions, that.dataDescriptions) && Objects
        .equal(this.dataLanguage, that.dataLanguage) && Objects.equal(this.purpose, that.purpose) && Objects
               .equal(this.additionalInfo, that.additionalInfo) && Objects.equal(this.pubDate, that.pubDate);
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("key", key).add("parentDatasetKey", parentDatasetKey)
      .add("duplicateOfDatasetKey", duplicateOfDatasetKey).add("installationKey", installationKey)
      .add("owningOrganizationKey", owningOrganizationKey).add("numConstituents", numConstituents).add("type", type)
      .add("subtype", subtype).add("title", title).add("alias", alias).add("abbreviation", abbreviation)
      .add("description", description).add("language", language).add("homepage", homepage).add("logoUrl", logoUrl)
      .add("citation", citation).add("rights", rights).add("lockedForAutoUpdate", lockedForAutoUpdate)
      .add("createdBy", createdBy).add("modifiedBy", modifiedBy).add("created", created).add("modified", modified)
      .add("deleted", deleted).add("contacts", contacts).add("endpoints", endpoints).add("machineTags", machineTags)
      .add("tags", tags).add("identifiers", identifiers).add("comments", comments)
      .add("bibliographicCitations", bibliographicCitations).add("curatorialUnits", curatorialUnits)
      .add("taxonomicCoverages", taxonomicCoverages).add("geographicCoverageDescription", geographicCoverageDescription)
      .add("geographicCoverages", geographicCoverages).add("temporalCoverages", temporalCoverages)
      .add("keywordCollections", keywordCollections).add("project", project)
      .add("samplingDescription", samplingDescription).add("countryCoverage", countryCoverage)
      .add("collections", collections).add("dataDescriptions", dataDescriptions).add("dataLanguage", dataLanguage)
      .add("purpose", purpose).add("additionalInfo", additionalInfo).add("pubDate", pubDate).toString();
  }

}
