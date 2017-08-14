package org.apache.hydra.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.hydra.model.AppDetails;
import org.apache.hydra.model.AppEntry;
import org.apache.hydra.model.AppStoreEntry;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

public class HydraSolrClient {

  private static String urlString;

  public HydraSolrClient() {
    // Locate Solr URL
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream input = classLoader.getResourceAsStream("hydra.properties");
    Properties properties = new Properties();
    try {
      properties.load(input);
      urlString = properties.getProperty("solr_url");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public List<AppStoreEntry> getRecommendedApps() {
    List<AppStoreEntry> apps = new ArrayList<AppStoreEntry>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");
    query.setFilterQueries("type_s:AppStoreEntry");
    query.setRows(40); 
    QueryResponse response;
    try {
      response = solr.query(query);
      Iterator<SolrDocument> list = response.getResults().listIterator();
      while (list.hasNext()) {
        SolrDocument d = list.next();
        AppStoreEntry entry = new AppStoreEntry();
        entry.setId(d.get("id").toString());
        entry.setOrg(d.get("org_s").toString());
        entry.setName(d.get("name_s").toString());
        entry.setDesc(d.get("desc_s").toString());
        entry.setLike(Integer.valueOf(d.get("like_i").toString()));
        entry.setDownload(Integer.valueOf(d.get("download_i").toString()));
        apps.add(entry);
      }
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return apps;
  }

  public List<AppStoreEntry> search(String keyword) {
    List<AppStoreEntry> apps = new ArrayList<AppStoreEntry>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    if (keyword.length()==0) {
      query.setQuery("*:*");
      query.setFilterQueries("type_s:AppStoreEntry");
    } else {
      query.setQuery(keyword);
      query.setFilterQueries("type_s:AppStoreEntry");
    }
    query.setRows(40); 
    QueryResponse response;
    try {
      response = solr.query(query);
      Iterator<SolrDocument> list = response.getResults().listIterator();
      while (list.hasNext()) {
        SolrDocument d = list.next();
        AppStoreEntry entry = new AppStoreEntry();
        entry.setId(d.get("id").toString());
        entry.setOrg(d.get("org_s").toString());
        entry.setName(d.get("name_s").toString());
        entry.setDesc(d.get("desc_s").toString());
        entry.setLike(Integer.valueOf(d.get("like_i").toString()));
        entry.setDownload(Integer.valueOf(d.get("download_i").toString()));
        apps.add(entry);
      }
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return apps;
  }

  public List<AppEntry> listAppEntries() {
    List<AppEntry> list = new ArrayList<AppEntry>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");
    query.setFilterQueries("type_s:AppEntry");
    query.setRows(40); 
    QueryResponse response;
    try {
      response = solr.query(query);
      Iterator<SolrDocument> appList = response.getResults().listIterator();
      while (appList.hasNext()) {
        SolrDocument d = appList.next();
        AppEntry entry = new AppEntry();
        entry.setId(d.get("id").toString());
        entry.setName(d.get("name_s").toString());
        entry.setApp(d.get("app_s").toString());
        list.add(entry);
      }
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return list;
  }

  public AppEntry findAppEntry(String id) {
    AppEntry entry = new AppEntry();

    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    query.setQuery("id:" + id);
    query.setFilterQueries("type_s:AppEntry");
    query.setRows(1);
    
    QueryResponse response;
    try {
      response = solr.query(query);
      Iterator<SolrDocument> appList = response.getResults().listIterator();
      while (appList.hasNext()) {
        SolrDocument d = appList.next();
        entry.setId(d.get("id").toString());
        entry.setApp(d.get("app_s").toString());
        entry.setName(d.get("name_s").toString());
      }
      solr.close();
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return entry;
  }
  
  public List<AppDetails> findAppConfig(String id) {
    List<AppDetails> list = new ArrayList<AppDetails>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    SolrQuery query = new SolrQuery();
    query.setQuery("id:" + id + "_*");
    query.setFilterQueries("type_s:AppDetails");
    query.setRows(40); 
    QueryResponse response;
    try {
      response = solr.query(query);
      Iterator<SolrDocument> appList = response.getResults().listIterator();
      while (appList.hasNext()) {
        SolrDocument d = appList.next();
        AppDetails entry = new AppDetails();
        entry.setImage(d.get("image_s").toString());
        entry.setVersion(d.get("version_s").toString());
        String[] env = d.getFieldValues("env").toArray(new String[d.getFieldValues("env").size()]);
        entry.setEnv(env);
        String[] ports = d.getFieldValues("ports").toArray(new String[d.getFieldValues("ports").size()]);
        entry.setPorts(ports);
        String[] volumes = d.getFieldValues("volumes").toArray(new String[d.getFieldValues("volumes").size()]);
        entry.setVolumes(volumes);
        list.add(entry);
      }
      solr.close();
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
    }
    return list;
  }

  public String[] deployApp(String id) throws SolrServerException, IOException {
    long download = 0;
    Collection<SolrInputDocument> docs = new HashSet<SolrInputDocument>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    // Find application information from AppStore
    String name = "c" + java.util.UUID.randomUUID().toString().substring(0, 11);
    String app = "";
    String appName = "";
    SolrQuery query = new SolrQuery();
    query.setQuery("id:" + id);
    query.setFilterQueries("type_s:AppStoreEntry");
    query.setRows(1);
    QueryResponse response = solr.query(query);
    Iterator<SolrDocument> appList = response.getResults().listIterator();
    while (appList.hasNext()) {
      SolrDocument d = appList.next();
      AppStoreEntry entry = new AppStoreEntry();
      entry.setOrg(d.get("org_s").toString());
      entry.setName(d.get("name_s").toString());
      entry.setDesc(d.get("desc_s").toString());
      entry.setLike(Integer.valueOf(d.get("like_i").toString()));
      entry.setDownload(Integer.valueOf(d.get("download_i").toString()));
      appName = entry.getName();
      download = entry.getDownload() + 1;
      app = entry.getOrg() + "/" + entry.getName();

      // Update download count
      docs.add(incrementDownload(d, download));
    }
    // increment download count for application
    String appInstanceId = id + "_" + download;

    // Register deployed application instance with AppList
    SolrInputDocument request = new SolrInputDocument();
    request.addField("type_s", "AppEntry");
    request.addField("id", appInstanceId);
    request.addField("name_s", name);
    request.addField("app_s", app);
    docs.add(request);

    // Register docker container instances with associated AppEntry
    SolrQuery findDockers = new SolrQuery();
    findDockers.setQuery("id:" + id + "_*");
    findDockers.setFilterQueries("type_s:docker");
    findDockers.setRows(100);

    QueryResponse findDockerResult = solr.query(findDockers);
    Iterator<SolrDocument> dockerList = findDockerResult.getResults().listIterator();
    int i = 0;
    while (dockerList.hasNext()) {
      SolrDocument doc = dockerList.next();
      SolrInputDocument buffer = convertSolrDocument(doc);
      String dockerId = appInstanceId + "_" + i;
      buffer.setField("id", dockerId);
      i++;
      docs.add(buffer);
    }

    // Commit Solr changes.
    UpdateResponse detailsResponse = solr.add(docs);
    if (detailsResponse.getStatus() != 0) {
      throw new IOException("Unable to register docker instance with application entry.");
    }
    solr.commit();
    solr.close();
    return new String[] { name, appName, appInstanceId };
  }

  private SolrInputDocument convertSolrDocument(SolrDocument doc) {
    Collection<String> names = doc.getFieldNames();
    SolrInputDocument s = new SolrInputDocument();
    for (String name : names) {
      if (name.equals("type_s")) {
        s.addField("type_s", "AppDetails");
      } else if(!name.equals("_version_")) {
        s.addField(name, doc.getFieldValues(name));
      }
    }
    return s;
  }

  private SolrInputDocument incrementDownload(SolrDocument doc, long download) {
    Collection<String> names = doc.getFieldNames();
    SolrInputDocument s = new SolrInputDocument();
    for (String name : names) {
      if(!name.equals("_version_")) {
        s.addField(name, doc.getFieldValues(name));
      }
    }
    s.setField("download_i", download++);
    return s;
  }

  public void deleteApp(String id) {
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    try {
      solr.deleteById(id);
      solr.commit();
      solr.close();
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    
  }

}
