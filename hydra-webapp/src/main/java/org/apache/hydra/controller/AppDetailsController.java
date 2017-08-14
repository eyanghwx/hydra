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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.hydra.application.YarnClient;
import org.apache.hydra.model.AppDetails;
import org.apache.hydra.model.AppStatus;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

@Path("/appDetails")
public class AppDetailsController {

  private static String urlString;

  public AppDetailsController() {
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
   * List detail information about the deployed application.
   * 
   * @param id
   * @return
   */
  @Path("config/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppDetails> getDetails(@PathParam("id") String id) {
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
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return list;
  }

  /**
   * Check application status
   */
  @Path("status/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AppStatus getStatus(@PathParam("id") String id) {
    AppStatus status = new AppStatus();
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
        String name = d.get("name_s").toString();
        status.setId(name);
        status.setState("UNKNOWN");
        YarnClient.getStatus(status);
      }
    } catch (SolrServerException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return status;
  }
}
