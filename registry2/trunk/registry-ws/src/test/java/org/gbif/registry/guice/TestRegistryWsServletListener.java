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
package org.gbif.registry.guice;

import org.gbif.api.service.common.UserService;
import org.gbif.registry.events.EventModule;
import org.gbif.registry.grizzly.RegistryServer;
import org.gbif.registry.ims.ImsModule;
import org.gbif.registry.persistence.guice.RegistryMyBatisModule;
import org.gbif.registry.search.DatasetIndexUpdateListener;
import org.gbif.registry.search.guice.RegistrySearchModule;
import org.gbif.registry.ws.guice.StringTrimInterceptor;
import org.gbif.registry.ws.servlet.LegacyWsFilter;
import org.gbif.ws.server.guice.GbifServletListener;
import org.gbif.ws.server.guice.WsAuthModule;
import org.gbif.ws.util.PropertiesUtil;

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.bval.guice.ValidationModule;
import org.apache.solr.client.solrj.SolrServer;

/**
 * The Registry WS module for testing in Grizzly.
 * This is the same as the production listener except that:
 * <ol>
 * <li>The registry-test.properties file is used</li>
 * <li>A life-cycle event monitor registers the SOLR server and Updater for JVM wide interaction</li>
 * </ol>
 */
public class TestRegistryWsServletListener extends GbifServletListener {

  public static final String APPLICATION_PROPERTIES = "registry-test.properties";
  @SuppressWarnings("unchecked")
  public final static List<Class<? extends ContainerRequestFilter>> requestFilters = Lists
    .<Class<? extends ContainerRequestFilter>>newArrayList(LegacyWsFilter.class);

// static {
// requestFilters.add(LegacyWsFilter.class);
// }

  public TestRegistryWsServletListener() {
    super(PropertiesUtil.readFromClasspath(APPLICATION_PROPERTIES),
      "org.gbif.registry.ws,org.gbif.registry.ws.provider", true, null, requestFilters);
  }

  @Override
  protected List<Module> getModules(Properties props) {
    return Lists.<Module>newArrayList(new RegistryMyBatisModule(props),
      new ImsModule(props),
      StringTrimInterceptor.newMethodInterceptingModule(),
      new ValidationModule(),
      new EventModule(props),
      new RegistrySearchModule(props),
      new UsersServiceModule(),
      new WsAuthModule(props));
  }

  /**
   * After startup registers the SOLR server and index updater, to allow WS client tests to interact.
   */
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    super.contextInitialized(servletContextEvent);
    RegistryServer.INSTANCE
      .setSolrServer(getInjector().getInstance(Key.get(SolrServer.class, Names.named("Dataset"))));
    RegistryServer.INSTANCE.setDatasetUpdater(getInjector().getInstance(DatasetIndexUpdateListener.class));
  }


  private static class UsersServiceModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(UserService.class).to(MockUserService.class);
    }

  }
}
