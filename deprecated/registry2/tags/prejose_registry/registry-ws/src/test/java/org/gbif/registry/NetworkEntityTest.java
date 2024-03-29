/**
 * 
 */
package org.gbif.registry;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.registry.model.NetworkEntity;
import org.gbif.api.registry.service.NetworkEntityService;
import org.gbif.registry.database.DatabaseInitializer;
import org.gbif.registry.database.LiquibaseInitializer;
import org.gbif.registry.grizzly.RegistryServer;
import org.gbif.registry.guice.RegistryTestModules;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * A generic test that runs against the registry interfaces.
 */
public abstract class NetworkEntityTest<T extends NetworkEntity> {

  // Flushes the database on each run
  @ClassRule
  public static final LiquibaseInitializer liquibaseRule = new LiquibaseInitializer(RegistryTestModules.database());

  @ClassRule
  public static final RegistryServer registryServer = new RegistryServer();

  @Rule
  public final DatabaseInitializer databaseRule = new DatabaseInitializer(RegistryTestModules.database());

  private final NetworkEntityService<T> service; // under test

  /**
   * @return a new example instance
   */
  protected abstract T newEntity();

  public NetworkEntityTest(NetworkEntityService<T> service) {
    this.service = service;
  }

  @Test(expected = IllegalArgumentException.class)
  public void createWithKey() {
    T e = newEntity();
    e.setKey(UUID.randomUUID()); // illegal to provide a key
    service.create(e);
  }

  @Test
  public void testCreate() {
    create(newEntity(), 1);
    create(newEntity(), 2);
  }

  @Test
  public void testUpdate() {
    T n1 = create(newEntity(), 1);
    n1.setTitle("New title");
    service.update(asWritable(n1));
    NetworkEntity n2 = service.get(n1.getKey());
    assertEquals("Persisted does not reflect update", "New title", n2.getTitle());
    assertTrue("Modification date not changing on update",
      n2.getModified().after(n1.getModified()));
    assertTrue("Modification date is not after the creation date",
      n2.getModified().after(n1.getCreated()));
    assertEquals("List service does not reflect the number of created entities", 1,
      service.list(new PagingRequest()).getResults().size());
  }

  @Test
  public void testDelete() {
    NetworkEntity n1 = create(newEntity(), 1);
    NetworkEntity n2 = create(newEntity(), 2);
    service.delete(n1.getKey());
    T n4 = service.get(n1.getKey()); // one can get a deleted entity
    n1.setDeleted(n4.getDeleted());
    assertEquals("Persisted does not reflect original after a deletion", n1, n4);
    // check that one cannot see the deleted entity in a list
    assertEquals("List service does not reflect the number of created entities", 1,
      service.list(new PagingRequest()).getResults().size());
    assertEquals("Following a delete, the wrong entity is returned in list results", n2,
      service.list(new PagingRequest()).getResults().get(0));
  }

  public void testDoubleDelete() {
    NetworkEntity n1 = create(newEntity(), 1);
    service.delete(n1.getKey());
    service.delete(n1.getKey()); // should just do nothing silently
  }

  /**
   * Creates 5 entities, and then pages over them using differing paging strategies, confirming the correct number of
   * records are returned for each strategy.
   */
  @Test
  public void testPaging() {
    for (int i = 1; i <= 5; i++) {
      create(newEntity(), i);
    }

    // the expected number of records returned when paging at different page sizes
    int[][] expectedPages = new int[][] {
      {1, 1, 1, 1, 1, 0}, // page size of 1
      {2, 2, 1, 0}, // page size of 2
      {3, 2, 0}, // page size of 3
      {4, 1, 0}, // page size of 4
      {5, 0}, // page size of 5
      {5, 0}, // page size of 6
    };

    // test the various paging strategies (e.g. page size of 1,2,3 etc to verify they behave as outlined above)
    for (int pageSize = 1; pageSize <= expectedPages.length; pageSize++) {
      int offset = 0; // always start at beginning
      for (int page = 0; page < expectedPages[pageSize - 1].length; page++, offset += pageSize) {
        // request the page using the page size and offset
        List<T> results =
          service.list(new PagingRequest(offset, expectedPages[pageSize - 1][page])).getResults();
        // confirm it is the correct number of results as outlined above
        assertEquals("Paging is not operating as expected when requesting pages of size " + pageSize,
          expectedPages[pageSize - 1][page], results.size());
      }
    }
  }

  // Repeatable entity creation with verification tests
  protected T create(T orig, int expectedCount) {
    try {
      @SuppressWarnings("unchecked")
      T entity = (T) BeanUtils.cloneBean(orig);
      Preconditions.checkNotNull(entity, "Cannot create a non existing entity");
      UUID key = service.create(entity);
      entity.setKey(key);
      T written = service.get(key);
      assertNotNull(written.getCreated());
      assertNotNull(written.getModified());
      assertNull(written.getDeleted());
      assertEquals("Persisted does not reflect original", entity, asWritable(written));
      assertEquals("List service does not reflect the number of created entities", expectedCount,
        service.list(new PagingRequest()).getResults().size());
      return written;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Creates a new instance of the supplied entity clearing all fields that are out of scope for external clients.
   * TODO: should we consider clearing contacts, tags etc? (would need to increase visibility of those interfaces to
   * make so)
   * TODO: think of a better name for this. Perhaps consider refactoring so one calls assertEquivalent() instead of
   * this.
   */
  protected T asWritable(T source) {
    try {
      @SuppressWarnings("unchecked")
      T copy = (T) BeanUtils.cloneBean(source);
      copy.setCreated(null);
      copy.setModified(null);
      copy.setDeleted(null);
      return copy;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected NetworkEntityService<T> getService() {
    return service;
  }
}
