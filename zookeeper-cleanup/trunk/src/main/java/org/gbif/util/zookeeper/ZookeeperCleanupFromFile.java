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
  private static final String PROD_PATH = "/prod_crawler/crawls/";
  private static final String UAT_PATH = "/uat_crawler/crawls/";
  private static final String DEV_PATH = "/dev_crawler/crawls/";
  private static final String PROD_ZK = "c1n8.gbif.org:2181,c1n9.gbif.org:2181,c1n10.gbif.org:2181";
  private static final String DEV_ZK = "c1n1.gbif.org:2181,c1n2.gbif.org:2181,c1n3.gbif.org:2181";

  private ZookeeperCleanupFromFile() {
  }

  /**
   * Delete crawls specified in file from given environment
   */
  public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
    LOG.debug("ZookeeperCleanupFromFile starting");
    if (args.length != 3) {
      LOG.error("Usage: ZookeeperCleanupFromFile <filename> <environment: prod, uat, or dev>");
      System.exit(1);
    }

    String path = null;
    String zkPath = null;
    if (args[1].equals(PROD)) {
      path = PROD_PATH;
      zkPath = PROD_ZK;
    } else if (args[1].equals(UAT)) {
      path = UAT_PATH;
      zkPath = PROD_ZK;
    } else if (args[1].equals(DEV)) {
      path = DEV_PATH;
      zkPath = DEV_ZK;
    }

    if (path == null) {
      LOG.error("Environment must be one of: prod, uat, or dev");
      System.exit(1);
    }

    List<String> keys = HueCsvReader.readKeys(args[0]);
    ZookeeperCleaner zkCleaner = new ZookeeperCleaner(zkPath);
    for (String key : keys) {
      LOG.debug("Deleting [{}]", path + key);
      zkCleaner.clean(path + key, false);
    }

    LOG.debug("ZookeeperCleanupFromFile finished");
  }
}
