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
package org.gbif.registry.ws.guice;

import org.gbif.registry.events.EventModule;
import org.gbif.registry.ims.ImsModule;
import org.gbif.registry.persistence.guice.RegistryMyBatisModule;
import org.gbif.registry.search.guice.RegistrySearchModule;
import org.gbif.registry.ws.servlet.LegacyWsFilter;
import org.gbif.registry.ws.util.AuthResponseCodeOverwriteFilter;
import org.gbif.drupal.guice.DrupalMyBatisModule;
import org.gbif.ws.server.guice.GbifServletListener;
import org.gbif.ws.server.guice.WsAuthModule;
import org.gbif.ws.util.PropertiesUtil;

import java.util.List;
import java.util.Properties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.apache.bval.guice.ValidationModule;

/**
 * The Registry WS module.
 */
public class RegistryWsServletListener extends GbifServletListener {

  public static final String APPLICATION_PROPERTIES = "registry.properties";
  public static final List<Class<? extends ContainerRequestFilter>> requestFilters = Lists.newArrayList();
  public static final List<Class<? extends ContainerResponseFilter>> responseFilters = Lists.newArrayList();

  static {
    requestFilters.add(LegacyWsFilter.class);
    responseFilters.add(AuthResponseCodeOverwriteFilter.class);

  }

  private static final String PACKAGES = "org.gbif.registry.ws.resources, org.gbif.registry.ws.provider";

  public RegistryWsServletListener() {
    super(PropertiesUtil.readFromClasspath(APPLICATION_PROPERTIES), PACKAGES, true, responseFilters, requestFilters);
  }

  @VisibleForTesting
  public RegistryWsServletListener(Properties properties) {
    super(properties, PACKAGES, true, null, requestFilters);
  }

  @Override
  protected List<Module> getModules(Properties properties) {
    return Lists.newArrayList(new RegistryMyBatisModule(properties),
                              new DrupalMyBatisModule(properties),
                              new ImsModule(),
                              StringTrimInterceptor.newMethodInterceptingModule(),
                              new ValidationModule(),
                              new EventModule(properties),
                              new RegistrySearchModule(properties),
                              new WsAuthModule(properties));
  }

  @VisibleForTesting
  @Override
  protected Injector getInjector() {
    return super.getInjector();
  }
}
