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
package org.gbif.registry.ims;

import org.gbif.api.vocabulary.ContactType;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ContactTypeHandler extends EnumDictTypeHandler<ContactType> {
  private static final Map<String, ContactType> DICT = ImmutableMap.<String, ContactType>builder()
    .put("nodes staff", ContactType.NODE_STAFF)
    .build();

  public ContactTypeHandler() {
    super(ContactType.class, null, DICT);
  }
}
