package org.gbif.util.zookeeper;

import org.gbif.util.HueCsvReader;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperCleanupFromFile {

  private static final Logger LOG = LoggerFactory.getLogger(ZookeeperCleanupFromFile.class);
  private static final String PROD = "prod";
  private static final String UAT = "uat";
  private static final String DEV = "dev";
  private static final String PROD_PATH = "/crawler/crawls/";
  private static final String UAT_PATH = "/uat_crawler/crawls/";
  private static final String DEV_PATH = "/dev_crawler/crawls/";

  private ZookeeperCleanupFromFile() {
  }

  /**
   * First and only arg needs to be the node whose content should be deleted, e.g. "/hbase".
   */
  public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
    LOG.debug("ZookeeperCleanupFromFile starting");
    if (args.length != 2) {
      LOG.error("Usage: ZookeeperCleanupFromFile <filename> <environment: prod, uat, or dev>");
      System.exit(1);
    }

    String path = null;
    if (args[1].equals(PROD)) {
      path = PROD_PATH;
    } else if (args[1].equals(UAT)) {
      path = UAT_PATH;
    } else if (args[1].equals(DEV)) {
      path = DEV_PATH;
    }

    if (path == null) {
      LOG.error("Environment must be one of: prod, uat, or dev");
      System.exit(1);
    }

    List<String> keys = HueCsvReader.readKeys(args[0]);
    ZookeeperCleaner zkCleaner = new ZookeeperCleaner();
    for (String key : keys) {
      LOG.debug("Deleting [{}]", path + key);
      zkCleaner.clean(path + key);
    }

    LOG.debug("ZookeeperCleanupFromFile finished");
  }
}
