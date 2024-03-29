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

import org.gbif.api.model.registry.Node;
import org.gbif.service.guice.PrivateServiceModule;
import org.gbif.drupal.guice.DrupalMyBatisModule;

import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the persistence layer using the properties supplied.
 */
public class ImsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Augmenter.class).to(AugmenterImpl.class).in(Scopes.SINGLETON);
  }
}
