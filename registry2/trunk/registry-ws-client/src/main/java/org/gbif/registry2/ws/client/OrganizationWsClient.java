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
package org.gbif.registry2.ws.client;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry2.Dataset;
import org.gbif.api.model.registry2.Organization;
import org.gbif.api.service.registry2.OrganizationService;
import org.gbif.registry2.ws.client.guice.RegistryWs;

import java.util.UUID;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

/**
 * Client-side implementation to the OrganizationService.
 */
public class OrganizationWsClient extends BaseNetworkEntityClient<Organization>
  implements OrganizationService {

  @Inject
  public OrganizationWsClient(@RegistryWs WebResource resource) {
    super(Organization.class, resource.path("organization"), null, GenericTypes.PAGING_ORGANIZATION);
  }

  @Override
  public PagingResponse<Dataset> hostedDatasets(UUID organizationKey, Pageable page) {
    return get(GenericTypes.PAGING_DATASET, null, null, page, String.valueOf(organizationKey), "hostedDataset");
  }

  @Override
  public PagingResponse<Dataset> ownedDatasets(UUID organizationKey, Pageable page) {
    return get(GenericTypes.PAGING_DATASET, null, null, page, String.valueOf(organizationKey), "ownedDataset");
  }

}
