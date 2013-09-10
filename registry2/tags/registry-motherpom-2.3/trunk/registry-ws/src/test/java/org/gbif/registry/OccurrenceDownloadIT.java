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
package org.gbif.registry;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.occurrence.Download;
import org.gbif.api.model.occurrence.DownloadRequest;
import org.gbif.api.model.occurrence.predicate.EqualsPredicate;
import org.gbif.api.model.occurrence.search.OccurrenceSearchParameter;
import org.gbif.api.service.registry.OccurrenceDownloadService;
import org.gbif.registry.database.DatabaseInitializer;
import org.gbif.registry.database.LiquibaseInitializer;
import org.gbif.registry.grizzly.RegistryServer;
import org.gbif.registry.guice.RegistryTestModules;
import org.gbif.registry.ws.resources.OccurrenceDownloadResource;
import org.gbif.ws.client.filter.SimplePrincipalProvider;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.validation.ValidationException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.gbif.registry.guice.RegistryTestModules.webservice;
import static org.gbif.registry.guice.RegistryTestModules.webserviceBasicAuthClient;
import static org.gbif.registry.guice.RegistryTestModules.webserviceClient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs tests for the {@link OccurrenceDownloadService} implementations.
 * This is parameterized to run the same test routines for the following:
 * <ol>
 * <li>The persistence layer</li>
 * <li>The WS service layer</li>
 * <li>The WS service client layer</li>
 * </ol>
 */
@RunWith(Parameterized.class)
public class OccurrenceDownloadIT {

  // Flushes the database on each run
  @ClassRule
  public static final LiquibaseInitializer liquibaseRule = new LiquibaseInitializer(RegistryTestModules.database());

  @ClassRule
  public static final RegistryServer registryServer = RegistryServer.INSTANCE;

  // Tests user
  private static String TEST_ADMIN_USER = "admin";
  private static String TEST_USER = "user";

  @Parameters
  public static Iterable<Object[]> data() {
    final Injector webservice = webservice();
    final Injector client = webserviceClient();

    return ImmutableList.<Object[]>of(
      new Object[] {webservice.getInstance(OccurrenceDownloadResource.class), null},
      new Object[] {client.getInstance(OccurrenceDownloadService.class),
        client.getInstance(SimplePrincipalProvider.class)});
  }

  @Rule
  public final DatabaseInitializer databaseRule = new DatabaseInitializer(RegistryTestModules.database());

  private final OccurrenceDownloadService occurrenceDownloadService;

  private SimplePrincipalProvider simplePrincipalProvider;


  @Before
  public void setup() {
    setPrincipal(TEST_ADMIN_USER);
  }

  private void setPrincipal(String username) {
    // reset SimplePrincipleProvider, configured for web service client tests only
    if (simplePrincipalProvider != null) {
      simplePrincipalProvider.setPrincipal(username);
    }
  }

  public OccurrenceDownloadIT(
    OccurrenceDownloadService occurrenceDownloadService,
    SimplePrincipalProvider simplePrincipalProvider) {
    this.occurrenceDownloadService = occurrenceDownloadService;
    this.simplePrincipalProvider = simplePrincipalProvider;
  }


  /**
   * Creates {@link OccurrenceDownload} instance with test data.
   * The key is generated randomly using the class java.util.UUID.
   * The instance generated should be ready and valid to be persisted.
   */
  protected static Download getTestInstance() {
    Download download = new Download();
    final Collection<String> emails = Arrays.asList("downloadtest@gbif.org");
    DownloadRequest request =
      new DownloadRequest(new EqualsPredicate(OccurrenceSearchParameter.TAXON_KEY, "212"), TEST_ADMIN_USER, emails);
    download.setKey(UUID.randomUUID().toString());
    download.setStatus(Download.Status.PREPARING);
    download.setRequest(request);
    download.setDownloadLink("testUrl");
    return download;
  }

  /**
   * Persists a valid {@link OccurrenceDownload} instance.
   */
  @Test
  public void testCreate() {
    occurrenceDownloadService.create(getTestInstance());
  }

  /**
   * Creates a {@link OccurrenceDownload} with a null status which should trigger a validation exception.
   */
  @Test(expected = ValidationException.class)
  public void testCreateWithNullStatus() {
    Download occurrenceDownload = getTestInstance();
    occurrenceDownload.setStatus(null);
    occurrenceDownloadService.create(occurrenceDownload);
  }

  /**
   * Tests the create and get(key) methods.
   */
  @Test
  public void testCreateAndGet() {
    Download occurrenceDownload = getTestInstance();
    occurrenceDownloadService.create(occurrenceDownload);
    Download occurrenceDownload2 = occurrenceDownloadService.get(occurrenceDownload.getKey());
    assertNotNull(occurrenceDownload2);
  }

  /**
   * Tests the status update of {@link OccurrenceDownload}.
   */
  @Test
  public void testUpdateStatus() {
    Download occurrenceDownload = getTestInstance();
    occurrenceDownloadService.create(occurrenceDownload);
    occurrenceDownloadService.updateStatus(occurrenceDownload.getKey(), Download.Status.RUNNING);
    Download occurrenceDownload2 = occurrenceDownloadService.get(occurrenceDownload.getKey());
    assertTrue(occurrenceDownload2.getStatus() == Download.Status.RUNNING);
  }

  /**
   * Tests the status update of {@link OccurrenceDownload}.
   */
  @Test
  public void testUpdateStatusCompleted() {
    Download occurrenceDownload = getTestInstance();
    occurrenceDownloadService.create(occurrenceDownload);
    occurrenceDownloadService.updateStatus(occurrenceDownload.getKey(), Download.Status.SUCCEEDED);
    Download occurrenceDownload2 = occurrenceDownloadService.get(occurrenceDownload.getKey());
    assertTrue(occurrenceDownload2.getStatus() == Download.Status.SUCCEEDED);
    assertTrue(occurrenceDownload2.getModified() != null);
  }

  /**
   * Creates several occurrence download with the same user name. And retrieves the downloads done by the user.
   */
  @Test
  public void testListByUser() {
    for (int i = 1; i <= 5; i++) {
      occurrenceDownloadService.create(getTestInstance());
    }
    assertTrue("List by user operation should return 5 records",
      occurrenceDownloadService.listByUser(TEST_ADMIN_USER, new PagingRequest(3, 5)).getResults().size() > 0);
  }


  /**
   * Creates several occurrence download with the same user name and attempts to get them with a different user name.
   */
  @Test(expected = AccessControlException.class)
  public void testListByUnauthorizedUser() {
    // This test applies to web service calls only, requires a security context.
    if (simplePrincipalProvider != null) {
      for (int i = 1; i <= 5; i++) {
        occurrenceDownloadService.create(getTestInstance());
      }
      final Injector clientBasicAuth = webserviceBasicAuthClient(TEST_USER, TEST_USER);
      OccurrenceDownloadService downloadServiceAuth = clientBasicAuth.getInstance(OccurrenceDownloadService.class);
      assertTrue("List by user operation should return 5 records",
        downloadServiceAuth.listByUser(TEST_ADMIN_USER, new PagingRequest(3, 5)).getResults().size() > 0);
    }
    // Just to make the test pass
    throw new AccessControlException("Fake exception");
  }


  /**
   * Creates several occurrence download with the same user name. And retrieves the downloads done by the user.
   */
  @Test
  public void testList() {
    for (int i = 1; i <= 5; i++) {
      occurrenceDownloadService.create(getTestInstance());
    }

    assertTrue("List operation should return 5 records",
      occurrenceDownloadService.list(new PagingRequest(0, 20)).getResults().size() > 0);
  }

}
