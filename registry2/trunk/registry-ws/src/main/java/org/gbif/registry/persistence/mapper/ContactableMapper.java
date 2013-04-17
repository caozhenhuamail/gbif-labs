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
package org.gbif.registry.persistence.mapper;

import org.gbif.api.registry.model.Contact;
import org.gbif.api.registry.vocabulary.ContactType;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;

public interface ContactableMapper {

  int addContact(
    @Param("targetEntityKey") UUID entityKey,
    @Param("contactKey") int contactKey,
    @Param("type") ContactType contactType,
    @Param("isPrimary") boolean isPrimary
  );

  int deleteContact(@Param("targetEntityKey") UUID entityKey, @Param("contactKey") int contactKey);

  List<Contact> listContacts(@Param("targetEntityKey") UUID targetEntityKey);

}
