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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.hydra.application.HydraSolrClient;
import org.apache.hydra.application.YarnClient;
import org.apache.hydra.model.AppDetails;
import org.apache.hydra.model.AppEntry;
import org.apache.hydra.model.AppStatus;

@Path("/appDetails")
public class AppDetailsController {

  public AppDetailsController() {
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
    HydraSolrClient sc = new HydraSolrClient();
    return sc.findAppConfig(id);
  }

  /**
   * Check application status
   * 
   * @param id - Application ID
   */
  @Path("status/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AppStatus getStatus(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    AppEntry entry = sc.findAppEntry(id);
    AppStatus status = new AppStatus();
    status.setId(entry.getName());
    status.setState("UNKNOWN");
    YarnClient.getStatus(status);
    return status;
  }
  
  /**
   * Stop an application
   * 
   * @param id - Application ID
   */
  @Path("stop/{id}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response stopApp(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    String name = sc.findAppEntry(id).getName();
    YarnClient.stopApp(name);
    return Response.ok().build();
  }
  
  /**
   * Restart an application
   * 
   * @param id - Application ID
   */
  @Path("restart/{id}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response restartApp(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    String name = sc.findAppEntry(id).getName();
    YarnClient.restartApp(name);
    return Response.ok().build();
  }
}
