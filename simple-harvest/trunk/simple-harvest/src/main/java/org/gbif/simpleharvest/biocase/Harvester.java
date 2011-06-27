package org.gbif.simpleharvest.biocase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.gbif.simpleharvest.model.Occurrence;
import org.gbif.simpleharvest.util.TemplateUtils;


/**
 * Performs a harvest
 * @author tim
 */
public class Harvester {
  private static final Logger LOG = Logger.getLogger(Harvester.class);
  
  // TODO: This does not need to be a multithreaded manager
  protected static ThreadSafeClientConnManager connectionManager;
  protected static HttpParams params = new BasicHttpParams();
  static {
    HttpConnectionParams.setConnectionTimeout(params, 600000);
    HttpConnectionParams.setSoTimeout(params, 600000);    
    ConnManagerParams.setMaxTotalConnections(params, 10);
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
  }
  
  
  private Connection conn;
  private int datasetId;
  private String url;
  private String databaseUrl;
  private String username;
  private String password;
  private String targetDirectory;
  private Map<String, String> templateParams = new HashMap<String, String>();
  private String templateLocation = "template/biocase/search.vm";
  private HttpClient httpClient = new DefaultHttpClient(connectionManager, params);
  private ResponseToModelHandler modelFactory = new ResponseToModelHandler();
  private OccurrenceToDBHandler databaseSync = new OccurrenceToDBHandler();
  
  /**
   * Creates the class 
   */
  public Harvester(int datasetId, String url, String databaseUrl, String username, String password, String targetDirectory) {
    this.datasetId = datasetId;
    this.url = url;
    this.databaseUrl = databaseUrl;
    this.username = username;
    this.password = password;
    this.targetDirectory = targetDirectory;
    
    File f = new File (targetDirectory + File.separator + "resource-" + datasetId);
    if (!f.exists()) {
      f.mkdirs();
    }
    this.targetDirectory = f.getAbsolutePath() + File.separator;
    
    // set up the template that will be used to issue biocase requests
    templateParams.put("destination", url);
    templateParams.put("hostAddress", "127.0.0.1");
    templateParams.put("contentNamespace", "http://www.tdwg.org/schemas/abcd/2.06");
    templateParams.put("subject", "/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString");
  }

  /**
   * The entry to the crawler
   * @param args Must contain:
   *  datasetID For the resource
   *  URL to crawl 
   *  database connection string
   *  database username
   *  database password
   */
  public static void main(String[] args) {
    if (args.length!=6) {
      LOG.error("Harvester takes 6 arguments");
      return;
    }
 
    int datasetId = Integer.parseInt(args[0]);
    String url = args[1];
    String databaseUrl = args[2];
    String username = args[3];
    String password = args[4];
    String targetDirectory = args[5];
    
    Harvester app = new Harvester(datasetId, url, databaseUrl, username, password, targetDirectory);
    app.run();
  }

  /**
   * Initiates a crawl
   */
  private void run() {
    try {
      init();
      harvest();
      
    } catch (Exception e) {
      LOG.error("Harvesting failed terminally", e);
      e.printStackTrace();
    } finally {
      close();
    }
    
  }
  
  /**
   * Does the harvesting
   */
  private void harvest() {
    LOG.info("Starting harvesting");
    // get the things before A
    try {
      pageRange(null, "AAA", 0);
    } catch (Exception e) {
      LOG.error("Error in range [null-AAA]", e);
    }
  
    // loop on the name basis Aaa-Aaz
    for (char c1='A'; c1<='Z'; c1++) {
      for (char c2='a'; c2<='z'; c2++) {
        String lower = c1 +"" + c2 +"a";
        String upper =  c1 +"" + c2 +"z";
        try {
          pageRange(lower, upper, 0);
        } catch (Exception e) {
          LOG.error("Error in range ["+lower+"-"+upper+"]", e);
        }
      }
    }

    // get the things after z
    try {
      pageRange("zzz", null, 0);
    } catch (Exception e) {
      LOG.error("Error in range [zzz-AAA]", e);
    }
    LOG.info("Finished harvesting");
  }
  
  /**
   * Issues a call to get a page of results
   */
  private void pageRange(String lower, String upper, int startAt) throws Exception {
	templateParams.put("lower", lower);
	templateParams.put("upper", upper);  
	templateParams.put("startAt", Integer.toString(startAt));  
    LOG.info("Starting lower[" + lower + "] upper[" + upper + "] start[" + startAt + "]");
    String query = TemplateUtils.getAndMerge(templateLocation, templateParams);
    String request = buildURL(url, "request", query);
    
    String requestFile = targetDirectory + lower + "-" + upper + "-" + startAt +"_.txt.gz";
    String responseFile = targetDirectory + lower + "-" + upper + "-" + startAt +".txt.gz";
    
    System.out.println(requestFile);
    
    // store the request
    GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(requestFile));
    IOUtils.write(query, gos);
    gos.close();

    // issue request and store response
    HttpGet httpget = new HttpGet(request);
    LOG.info("Initiating Request[" + requestFile + "] for Range[" + lower + "-" + upper + "] starting at[" + startAt + "]");
    httpClient.execute(httpget, new ResponseToFileHandler(responseFile));
    LOG.info("Range[" + lower + "-" + upper + "] starting at[" + startAt + "] returned response[" + responseFile + "]");
    
    // now process the response to build the records
    GZIPInputStream contentStream = null;
    List<Occurrence> results = new ArrayList<Occurrence>();
    try {
      contentStream = new GZIPInputStream(new FileInputStream(responseFile));
      results = modelFactory.handleResponse(contentStream);
    } finally {
      IOUtils.closeQuietly(contentStream);
    }
    
    // now synchronise the results to the database
    LOG.info("Number of results: " + results.size());
    for (Occurrence o : results) {
      try {
        databaseSync.synchronize(conn, o);
      } catch (RuntimeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
    }
    
    
    LOG.info("Finished lower[" + lower + "] upper[" + upper + "] start[" + startAt + "]");
    
    if (results.size() >= 1000)
    {
    	results = null; // make eligible for garbage collection
    	pageRange(lower, upper, startAt + 1000);
    }
  }
  
  /**
   * Closes the database connection
   */
  private void close() {
    if (conn!=null) {
      try {
        LOG.debug("Closing database connection");
        conn.close();
        LOG.debug("Database connection closed successfully");
      } catch (SQLException e) {
        // swallow exceptions on cleanup
      }
    }
  }

  /**
   * Opens the connection to the database
   * @throws SQLException 
   */
  private void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
    LOG.debug("Creating database connection");
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    conn = DriverManager.getConnection(databaseUrl, username, password);    
    LOG.debug("Database connection created successfully");
  }
  
  /**
   * Builds a URL from the base URL and the appending an encoded content as a parameter named
   * by the key.  E.g.
   * http://blahblah?request=<encodedXML>
   * @param url the base url
   * @param parameterKey The key to use as the parameter (e.g. to have ?request= would be "request")
   * @param content The content to encode in the request
   * @return The encoded url
   */
  public String buildURL(String url, String parameterKey, String content) throws UnsupportedEncodingException {
    if (content != null && content.length()>0) {
      if (url.contains("?")) {
        url = url + "&" + parameterKey + "=" + URLEncoder.encode(content, "UTF-8");
      } else {
        url = url + "?" + parameterKey + "=" + URLEncoder.encode(content, "UTF-8");
      }
    }
    return url;
  }   
}