/*
 * Copyright 2013 Global Biodiversity Information Facility (GBIF)
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
package org.gbif.api.model.registry2;

import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This interface provides a minimal contract that all network entities (The readable version) will adhere to. It is
 * used <em>only</em> to simplify consistent testing of operations on network entities, hence the restriction to package
 * visibility only.
 */
public interface NetworkEntity {

  @Nullable
  UUID getKey();

  void setKey(UUID key);

  @NotNull
  @Size(min = 2, max = 255)
  String getTitle();

  void setTitle(String title);

  @Nullable
  @Size(min = 10)
  String getDescription();

  void setDescription(String description);

  @Nullable
  Date getCreated();

  void setCreated(Date created);

  @Nullable
  Date getModified();

  void setModified(Date modified);

  @Nullable
  Date getDeleted();

  void setDeleted(Date deleted);

}
