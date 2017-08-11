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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.hydra.application.YarnClient;
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

@Path("/appList")
public class AppListController {
  
  private static String urlString;
  
  public AppListController() {
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
   * Get list of deployed applications.
   * 
   * @return - Active application deployed by current user.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppEntry> getList() {
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

  /**
   * Delete an application
   */
  @DELETE
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") String id, @PathParam("name") String name) {
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    try {
      solr.deleteById(id);
      solr.commit();
      YarnClient.deleteApp(name);
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Response.status(Status.ACCEPTED).build();
  }

  /**
   * Deploy an application
   */
  @POST
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deploy(@PathParam("id") String id) {
    long download = 0;
    Collection<SolrInputDocument> docs = new HashSet<SolrInputDocument>();
    SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    // Find application information from AppStore
    String name = "c"+java.util.UUID.randomUUID().toString().substring(0,11);
    String app = "";
    String appName = "";
    SolrQuery query = new SolrQuery();
    query.setQuery("id:" + id);
    query.setFilterQueries("type_s:AppStoreEntry");
    query.setRows(1); 
    try {
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
        download = entry.getDownload()+1;
        app = entry.getOrg()+"/"+entry.getName();
        
        // Update download count
        docs.add(incrementDownload(d,download));
      }
    } catch (SolrServerException | IOException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    // increment download count for application
    String appInstanceId = id + "_" + download;

    try {
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
        return Response.status(Status.BAD_REQUEST).build();
      }
      solr.commit();
      solr.close();
      YarnClient.createApp(name, appName);
    } catch (SolrServerException | IOException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    String output = "{\"status\":\"Application deployed.\",\"id\":\"" + appInstanceId + "\"}";
    return Response.status(200).entity(output).build();
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

}
