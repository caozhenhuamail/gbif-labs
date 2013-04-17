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
package org.gbif.registry2.guice;

import org.gbif.registry2.grizzly.RegistryServer;
import org.gbif.registry2.persistence.guice.RegistryMyBatisModule;
import org.gbif.registry2.ws.client.guice.RegistryWsClientModule;
import org.gbif.registry2.ws.resources.DatasetResource;
import org.gbif.registry2.ws.resources.InstallationResource;
import org.gbif.registry2.ws.resources.NetworkResource;
import org.gbif.registry2.ws.resources.NodeResource;
import org.gbif.registry2.ws.resources.OrganizationResource;

import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;
import javax.sql.DataSource;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.bval.guice.ValidationModule;
import org.apache.ibatis.io.Resources;

/**
 * Utility to provide the different Guice configurations for:
 * <ol>
 * <li>The WS service layer</li>
 * <li>The WS service client layer</li>
 * <li>A management configuration to allow utilities to manipulate the database (Liquibase etc)</li>
 * </ol>
 * Everything is cached, and reused on subsequent calls.
 */
public class RegistryTestModules {

  // cache everything, for reuse in repeated calls (e.g. eclipse IDE test everything)
  private static Injector webservice;
  private static Injector webserviceClient;
  private static Injector management;
  private static DataSource managementDatasource;

  /**
   * @return An injector that is bound for the webservice layer.
   */
  public static synchronized Injector webservice() {
    if (webservice == null) {
      try {
        final Properties p = new Properties();
        p.load(Resources.getResourceAsStream("registry-test.properties"));
        webservice = Guice.createInjector(new AbstractModule() {

          @Override
          protected void configure() {
            bind(NodeResource.class);
            bind(OrganizationResource.class);
            bind(InstallationResource.class);
            bind(DatasetResource.class);
            bind(NetworkResource.class);
          }
        }, new RegistryMyBatisModule(p), new ValidationModule());
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }

    }
    return webservice;
  }

  /**
   * @return An injector that is bound for the webservice client layer.
   */
  public static synchronized Injector webserviceClient() {
    if (webserviceClient == null) {
      Properties props = new Properties();
      props.put("registry.ws.url", "http://localhost:" + RegistryServer.getPort());
      webserviceClient = Guice.createInjector(new RegistryWsClientModule(props));
    }
    return webserviceClient;
  }

  /**
   * @return A datasource that is for use in management activities such as Liquibase, or cleaning between tests.
   */
  public static DataSource database() {
    if (managementDatasource == null) {
      managementDatasource = RegistryTestModules.management().getInstance(DataSource.class);
    }
    return managementDatasource;

  }

  /**
   * @return An injector configured to issue a Datasource suitable for database management activities (Liquibase etc).
   */
  private static synchronized Injector management() {
    if (management == null) {
      try {
        final Properties p = new Properties();
        p.load(Resources.getResourceAsStream("registry-test.properties"));
        management = Guice.createInjector(new AbstractModule() {

          @Override
          protected void configure() {
            Names.bindProperties(binder(), p);
            bind(DataSource.class).toProvider(ManagementProvider.class);
          }
        });
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    return management;
  }

  /**
   * Provides a datasource that can issue connections for management activities, such as Liquibase or
   * clearing tables before tests run etc.
   */
  @Singleton
  public static class ManagementProvider implements Provider<DataSource> {

    // Limit to a single (reusable) connection
    public static final int PARTITION_COUNT = 1;
    public static final int POOL_SIZE_PER_PARTITION = 1;
    private final String url;
    private final String username;
    private final String password;

    @Inject
    public ManagementProvider(
      @Named("registry.db.JDBC.driver") String driver,
      @Named("registry.db.JDBC.url") String url,
      @Named("registry.db.JDBC.username") String username,
      @Named("registry.db.JDBC.password") String password
    ) {
      this.url = url;
      this.username = username;
      this.password = password;
      try {
        DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
      } catch (Exception e) {
        Throwables.propagate(e);
      }

    }

    @Override
    public DataSource get() {
      BoneCPDataSource ds = new BoneCPDataSource();
      ds.setJdbcUrl(url);
      ds.setUsername(username);
      ds.setPassword(password);
      ds.setMaxConnectionsPerPartition(PARTITION_COUNT);
      ds.setPartitionCount(POOL_SIZE_PER_PARTITION);
      return ds;
    }
  }

}
