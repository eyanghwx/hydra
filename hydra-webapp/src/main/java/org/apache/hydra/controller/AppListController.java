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
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.hadoop.yarn.service.api.records.Application;

import org.apache.hydra.application.HydraSolrClient;
import org.apache.hydra.application.YarnClient;
import org.apache.hydra.model.AppEntry;
import org.apache.solr.client.solrj.SolrServerException;

@Path("/appList")
public class AppListController {
  
  public AppListController() {
  }

  /**
   * Get list of deployed applications.
   * 
   * @return - Active application deployed by current user.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppEntry> getList() {
    HydraSolrClient sc = new HydraSolrClient();
    return sc.listAppEntries();
  }

  /**
   * Delete an application
   */
  @DELETE
  @Path("{id}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") String id, @PathParam("name") String name) {
    HydraSolrClient sc = new HydraSolrClient();
    sc.deleteApp(id);
    YarnClient.deleteApp(name);
    return Response.status(Status.ACCEPTED).build();
  }

  /**
   * Deploy an application
   */
  @POST
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deploy(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    Application app;
    try {
      app = sc.deployApp(id);
    } catch (SolrServerException | IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.toString()).build();
    }
    YarnClient.createApp(app);
    String output = "{\"status\":\"Application deployed.\",\"id\":\"" + app.getName() + "\"}";
    return Response.status(Status.ACCEPTED).entity(output).build();
  }

}
