/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ocurrence.index.solr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Progressable;

public class SolrOutputFormat<K, V> extends FileOutputFormat<K, V> {

  private static final Log LOG = LogFactory.getLog(SolrOutputFormat.class);

  /**
   * The parameter used to pass the solr config zip file information. This will
   * be the hdfs path to the configuration zip file
   */
  public static final String SETUP_OK = "solr.output.format.setup";

  /** The key used to pass the zip file name through the configuration. */
  public static final String ZIP_NAME = "solr.zip.name";

  /**
   * The base name of the zip file containing the configuration information.
   * This file is passed via the distributed cache using a unique name, obtained
   * via {@link #getZipName(Configuration jobConf)}.
   */
  public static final String ZIP_FILE_BASE_NAME = "solr.zip";

  /**
   * The key used to pass the boolean configuration parameter that instructs for
   * regular or zip file output
   */
  public static final String OUTPUT_ZIP_FILE = "solr.output.zip.format";

  static int defaultSolrWriterThreadCount = 2;

  public static final String SOLR_WRITER_THREAD_COUNT = "solr.record.writer.num.threads";

  static int defaultSolrWriterQueueSize = 100;

  public static final String SOLR_WRITER_QUEUE_SIZE = "solr.record.writer.max.queues.size";

  static int defaultSolrBatchSize = 20;

  public static final String SOLR_RECORD_WRITER_BATCH_SIZE = "solr.record.writer.batch.size";

  public static String getSetupOk() {
    return SETUP_OK;
  }

  /** Get the number of threads used for index writing */
  public static void setSolrWriterThreadCount(int count, Configuration conf) {
    conf.setInt(SOLR_WRITER_THREAD_COUNT, count);
  }

  /** Set the number of threads used for index writing */
  public static int getSolrWriterThreadCount(Configuration conf) {
    return conf.getInt(SOLR_WRITER_THREAD_COUNT, defaultSolrWriterThreadCount);
  }

  /**
   * Set the maximum size of the the queue for documents to be written to the
   * index.
   */
  public static void setSolrWriterQueueSize(int count, Configuration conf) {
    conf.setInt(SOLR_WRITER_QUEUE_SIZE, count);
  }

  /** Return the maximum size for the number of documents pending index writing. */
  public static int getSolrWriterQueueSize(Configuration conf) {
    return conf.getInt(SOLR_WRITER_QUEUE_SIZE, defaultSolrWriterQueueSize);
  }

  /**
   * Return the file name portion of the configuration zip file, from the
   * configuration.
   */
  public static String getZipName(Configuration conf) {
    return conf.get(ZIP_NAME, ZIP_FILE_BASE_NAME);
  }

  /**
   * configure the job to output zip files of the output index, or full
   * directory trees. Zip files are about 1/5th the size of the raw index, and
   * much faster to write, but take more cpu to create. zip files are ideal for
   * deploying into a katta managed shard.
   * 
   * @param output
   * @param conf
   */
  public static void setOutputZipFormat(boolean output, Configuration conf) {
    conf.setBoolean(OUTPUT_ZIP_FILE, output);
  }

  /**
   * return true if the output should be a zip file of the index, rather than
   * the raw index
   * 
   * @param conf
   * @return
   */
  public static boolean isOutputZipFormat(Configuration conf) {
    return conf.getBoolean(OUTPUT_ZIP_FILE, false);
  }

  @Override
  public void checkOutputSpecs(JobContext job) throws IOException {
    super.checkOutputSpecs(job);
   /* if (job.getConfiguration().get(SETUP_OK) == null) {
      throw new IOException("Solr home cache not set up!");
    }*/
  }


  @Override
  public RecordWriter<K, V> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
    return new SolrRecordWriter<K, V>(context);
  }

  public static void setupSolrHomeCache(File solrHome, Configuration jobConf)
      throws IOException {
    if (solrHome == null || !(solrHome.exists() && solrHome.isDirectory())) {
      throw new IOException("Invalid solr.home: " + solrHome);
    }
    File tmpZip = File.createTempFile("solr", "zip");
    createZip(solrHome, tmpZip);
    // Make a reasonably unique name for the zip file in the distributed cache
    // to avoid collisions if multiple jobs are running.
    String hdfsZipName = UUID.randomUUID().toString() + '.'
        + ZIP_FILE_BASE_NAME;
    jobConf.set(ZIP_NAME, hdfsZipName);

    Path zipPath = new Path("/tmp", getZipName(jobConf));
    FileSystem fs = FileSystem.get(jobConf);
    fs.copyFromLocalFile(new Path(tmpZip.toString()), zipPath);
    final URI baseZipUrl = fs.getUri().resolve(
        zipPath.toString() + '#' + getZipName(jobConf));

    DistributedCache.addCacheArchive(baseZipUrl, jobConf);
    LOG.info("Set Solr cache: "
        + Arrays.asList(DistributedCache.getCacheArchives(jobConf)));
    // Actually send the path for the configuration zip file
    jobConf.set(SETUP_OK, zipPath.toString());
  }

  private static void createZip(File dir, File out) throws IOException {
    HashSet<File> files = new HashSet<File>();
    // take only conf/ and lib/
    for (String allowedDirectory : SolrRecordWriter
        .getAllowedConfigDirectories()) {
      File configDir = new File(dir, allowedDirectory);
      boolean configDirExists;
      /** If the directory does not exist, and is required, bail out */
      if (!(configDirExists = configDir.exists())
          && SolrRecordWriter.isRequiredConfigDirectory(allowedDirectory)) {
        throw new IOException(String.format(
            "required configuration directory %s is not present in %s",
            allowedDirectory, dir));
      }
      if (!configDirExists) {
        continue;
      }
      listFiles(configDir, files); // Store the files in the existing, allowed
                                   // directory configDir, in the list of files
                                   // to store in the zip file
    }

    out.delete();
    int subst = dir.toString().length();
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out));
    byte[] buf = new byte[1024];
    for (File f : files) {
      ZipEntry ze = new ZipEntry(f.toString().substring(subst));
      zos.putNextEntry(ze);
      InputStream is = new FileInputStream(f);
      int cnt;
      while ((cnt = is.read(buf)) >= 0) {
        zos.write(buf, 0, cnt);
      }
      is.close();
      zos.flush();
      zos.closeEntry();
    }
    zos.close();
  }

  private static void listFiles(File dir, Set<File> files) throws IOException {
    File[] list = dir.listFiles();
    for (File f : list) {
      if (f.isFile()) {
        files.add(f);
      } else {
        listFiles(f, files);
      }
    }
  }

  public static int getBatchSize(Configuration jobConf) {
    // TODO Auto-generated method stub
    return jobConf.getInt(SolrOutputFormat.SOLR_RECORD_WRITER_BATCH_SIZE,
        defaultSolrBatchSize);
  }

  public static void setBatchSize(int count, Configuration jobConf) {
    jobConf.setInt(SOLR_RECORD_WRITER_BATCH_SIZE, count);
  }

}
