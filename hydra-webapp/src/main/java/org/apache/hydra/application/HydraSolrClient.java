/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hydra.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hydra.model.AppEntry;
import org.apache.hydra.model.AppStoreEntry;
import org.apache.hydra.model.Application;
import org.apache.hydra.utils.RandomWord;
import org.apache.hydra.utils.WordLengthException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HydraSolrClient {

  private static final Log LOG = LogFactory.getLog(HydraSolrClient.class);
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
      LOG.error("Error reading hydra configuration: ", e);
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
        if (d.get("icon_s")!=null) {
          entry.setIcon(d.get("icon_s").toString());
        }
        entry.setLike(Integer.valueOf(d.get("like_i").toString()));
        entry.setDownload(Integer.valueOf(d.get("download_i").toString()));
        apps.add(entry);
      }
    } catch (SolrServerException | IOException e) {
      LOG.error("Error getting a list of recommended applications: ", e);
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
      LOG.error("Error in searching for applications: ", e);
    }
    return apps;
  }

  public List<AppEntry> listAppEntries() {
    List<AppEntry> list = new ArrayList<AppEntry>();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        entry.setYarnfile(mapper.readValue(d.get("yarnfile_s").toString(), org.apache.hadoop.yarn.service.api.records.Application.class));
        list.add(entry);
      }
    } catch (SolrServerException | IOException e) {
      LOG.error("Error in listing deployed applications: ", e);
    }
    return list;
  }

  public AppEntry findAppEntry(String id) {
    AppEntry entry = new AppEntry();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        entry.setYarnfile(mapper.readValue(d.get("yarnfile_s").toString(), org.apache.hadoop.yarn.service.api.records.Application.class));
      }
      solr.close();
    } catch (SolrServerException | IOException e) {
      LOG.error("Error in finding deployed application: " + id, e);
    }
    return entry;
  }

  public org.apache.hadoop.yarn.service.api.records.Application deployApp(String id) throws SolrServerException, IOException {
    long download = 0;
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Collection<SolrInputDocument> docs = new HashSet<SolrInputDocument>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    // Find application information from AppStore
    String name;
    try {
      Random r = new Random();
      int Low = 3;
      int High = 10;
      int seed = r.nextInt(High-Low) + Low;
      int seed2 = r.nextInt(High-Low) + Low;
      name = RandomWord.getNewWord(seed).toLowerCase() + "_" + RandomWord.getNewWord(seed2).toLowerCase();
    } catch (WordLengthException e) {
      name = "c" + java.util.UUID.randomUUID().toString().substring(0, 11);
    }
    org.apache.hadoop.yarn.service.api.records.Application yarnApp = null;
    SolrQuery query = new SolrQuery();
    query.setQuery("id:" + id);
    query.setFilterQueries("type_s:AppStoreEntry");
    query.setRows(1);
    QueryResponse response = solr.query(query);
    Iterator<SolrDocument> appList = response.getResults().listIterator();
    AppStoreEntry entry = new AppStoreEntry();
    while (appList.hasNext()) {
      SolrDocument d = appList.next();
      entry.setOrg(d.get("org_s").toString());
      entry.setName(d.get("name_s").toString());
      entry.setDesc(d.get("desc_s").toString());
      yarnApp = mapper.readValue(d.get("yarnfile_s").toString(), org.apache.hadoop.yarn.service.api.records.Application.class);
      entry.setLike(Integer.valueOf(d.get("like_i").toString()));
      entry.setDownload(Integer.valueOf(d.get("download_i").toString()));
      download = entry.getDownload() + 1;

      // Update download count
      docs.add(incrementDownload(d, download));
    }
    
    // increment download count for application

    if (yarnApp!=null) {
      // Register deployed application instance with AppList
      yarnApp.setName(name);
      SolrInputDocument request = new SolrInputDocument();
      request.addField("type_s", "AppEntry");
      request.addField("id", name);
      request.addField("name_s", name);
      request.addField("app_s", entry.getOrg()+"/"+entry.getName());
      request.addField("yarnfile_s", mapper.writeValueAsString(yarnApp));
      docs.add(request);
    }
    
    // Commit Solr changes.
    UpdateResponse detailsResponse = solr.add(docs);
    if (detailsResponse.getStatus() != 0) {
      throw new IOException("Unable to register docker instance with application entry.");
    }
    solr.commit();
    solr.close();
    return yarnApp;
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
      LOG.error("Error in removing deployed application: "+id, e);
    }    
  }

  public void register(Application app) throws IOException {
    Collection<SolrInputDocument> docs = new HashSet<SolrInputDocument>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      SolrInputDocument buffer = new SolrInputDocument();
      buffer.setField("id", java.util.UUID.randomUUID().toString().substring(0, 11));
      buffer.setField("org_s", app.getOrganization());
      buffer.setField("name_s", app.getName());
      buffer.setField("desc_s", app.getDescription());
      if (app.getIcon() != null) {
        buffer.setField("icon_s", app.getIcon());
      }
      buffer.setField("type_s", "AppStoreEntry");
      buffer.setField("like_i", 0);
      buffer.setField("download_i", 0);
      
      // Keep only YARN data model for yarnfile field
      String yarnFile = mapper.writeValueAsString(app);
      LOG.info("app:"+yarnFile);
      org.apache.hadoop.yarn.service.api.records.Application yarnApp = mapper.readValue(yarnFile, org.apache.hadoop.yarn.service.api.records.Application.class);
      buffer.setField("yarnfile_s", mapper.writeValueAsString(yarnApp));

      docs.add(buffer);
      // Commit Solr changes.
      UpdateResponse detailsResponse = solr.add(docs);
      if (detailsResponse.getStatus() != 0) {
        throw new IOException("Unable to register application in Application Store.");
      }
      solr.commit();
      solr.close();
    } catch (SolrServerException | IOException e) {
      throw new IOException("Unable to register application in Application Store. "+ e.getMessage());
    }    
  }

}
