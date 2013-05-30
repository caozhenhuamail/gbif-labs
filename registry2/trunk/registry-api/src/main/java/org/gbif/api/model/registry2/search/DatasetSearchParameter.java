/*
 * Copyright 2012 Global Biodiversity Information Facility (GBIF)
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
package org.gbif.api.model.registry2.search;

import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.registry2.Continent;
import org.gbif.api.vocabulary.registry2.DatasetSubtype;
import org.gbif.api.vocabulary.registry2.DatasetType;

import java.util.UUID;

/**
 * Each value in the enum represents a possible facet for the Dataset search.
 */
public enum DatasetSearchParameter implements SearchParameter {

  /**
   * The key of the network of origin.
   */
  NETWORK_ORIGIN(UUID.class),

  /**
   * {@link org.gbif.api.vocabulary.registry2.DatasetType} enumeration value.
   */
  TYPE(DatasetType.class),

  /**
   * {@link org.gbif.api.vocabulary.DatasetSubtype} enumeration value.
   */
  SUBTYPE(DatasetSubtype.class),

  /**
   * The owning organizations uuid key.
   */
  OWNING_ORG(UUID.class),

  /**
   * The hosting organizations uuid key.
   */
  HOSTING_ORG(UUID.class),

  /**
   * A case insensitive plain text keyword or serialized tag as created by Tag.toString().
   * The search is done on the keywords generated through {@link org.gbif.api.model.registry2.eml.Dataset#getKeywords()}
   * by merging tags, the keywordCollections and temporalCoverages property.
   */
  KEYWORD(String.class),

  /**
   * Filters datasets by their temporal coverage broken down to decades.
   * Decade given as a full year, e.g. 1950, 1960 or 1980.
   */
  DECADE(Integer.class),

  /**
   * The owning organizations country.
   */
  PUBLISHING_COUNTRY(Country.class),

  /**
   * Country of the geospatial coverage of a dataset.
   */
  COUNTRY(Country.class),

  /**
   * {@link org.gbif.api.vocabulary.Continent} of the geospatial coverage of a dataset.
   */
  CONTINENT(Continent.class);


  private DatasetSearchParameter(Class<?> type) {
    this.type = type;
  }

  private final Class<?> type;

  /**
   * @return the data type expected for the value of the respective search parameter
   */
  public Class<?> type() {
    return type;
  }

}
