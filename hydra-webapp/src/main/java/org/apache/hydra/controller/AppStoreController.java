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

package org.apache.hydra.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.hydra.model.AppStoreEntry;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

@Path("/appStore")
public class AppStoreController {

  private static String urlString;
  
  public AppStoreController() {
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

  /**
   * Display the most frequently used applications on Hydra home page.
   * 
   * @return - List of YARN applications
   */

  @GET
  @Path("recommended")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppStoreEntry> get() {
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

  /**
   * Search for yarn applications from solr.
   * 
   * @param keyword
   * @return - List of YARN applications matching keyword search.
   */
  @GET
  @Path("search")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppStoreEntry> search(@QueryParam("q") String keyword) {
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
}
