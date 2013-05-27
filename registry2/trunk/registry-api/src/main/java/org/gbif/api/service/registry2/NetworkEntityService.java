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
package org.gbif.api.service.registry2;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public interface NetworkEntityService<T> extends MachineTagService, TagService, CommentService,
  IdentifierService, EndpointService, ContactService {

  UUID create(@NotNull T entity);

  // TODO: Define behavior when it is already deleted or does not exist
  void delete(@NotNull UUID key);

  T get(@NotNull UUID key);

  /**
   * @return a list of network entities ordered by their creation date, newest coming first
   */
  PagingResponse<T> list(@Nullable Pageable page);

  /**
   * A simple search that supports paging.
   * 
   * @return a pageable response of network entities, with accurate counts.
   */
  PagingResponse<T> search(String query, @Nullable Pageable page);

  void update(@NotNull T entity);

}
