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
package org.gbif.registry2.persistence.mapper;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.registry2.Organization;
import org.gbif.api.vocabulary.Country;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Param;

public interface OrganizationMapper extends BaseNetworkEntityMapper<Organization> {

  /**
   * At higher levels this appears on the NodeService, but it makes a cleaner MyBatis implementation on this mapper.
   */
  List<Organization> organizationsEndorsedBy(@Param("nodeKey") UUID nodeKey, @Nullable @Param("page") Pageable page);

  /**
   * At higher levels this appears on the NodeService, but it makes a cleaner MyBatis implementation on this mapper.
   */
  List<Organization> pendingEndorsements(@Nullable @Param("nodeKey") UUID nodeKey,
    @Nullable @Param("page") Pageable page);

  /**
   * At higher levels this appears on the OrganizationService.
   * Selects all organizations by the country of their address.
   */
  List<Organization> organizationsByCountry(@Param("country") Country country, @Nullable @Param("page") Pageable page);

  /**
   * @return The count of all organizations with approved endorsements for the node
   */
  long countOrganizationsEndorsedBy(@Param("nodeKey") UUID nodeKey);

  /**
   * @return The count of all organizations with a pending endorsement approval, optionally limited by the node if
   *         supplied
   */
  long countPendingEndorsements(@Nullable @Param("nodeKey") UUID nodeKey);

  long countOrganizationsByCountry(@Param("country") Country country);
}
