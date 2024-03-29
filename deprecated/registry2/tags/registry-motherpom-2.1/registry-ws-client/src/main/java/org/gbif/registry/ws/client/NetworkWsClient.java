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
package org.gbif.registry.ws.client;

import org.gbif.api.model.registry.Network;
import org.gbif.api.service.registry.NetworkService;
import org.gbif.registry.ws.client.guice.RegistryWs;

import javax.annotation.Nullable;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Client-side implementation to the NetworkService.
 */
public class NetworkWsClient extends BaseNetworkEntityClient<Network> implements NetworkService {

  @Inject
  public NetworkWsClient(@RegistryWs WebResource resource, @Nullable ClientFilter authFilter) {
    super(Network.class, resource.path("network"), authFilter, GenericTypes.PAGING_NETWORK);
  }

}
