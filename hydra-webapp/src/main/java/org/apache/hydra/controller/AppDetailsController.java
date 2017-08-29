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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.hadoop.yarn.service.api.records.Application;
import org.apache.hadoop.yarn.service.api.records.ApplicationState;
import org.apache.hydra.application.HydraSolrClient;
import org.apache.hydra.application.YarnClient;
import org.apache.hydra.model.AppEntry;

import com.fasterxml.jackson.core.JsonProcessingException;

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
  public AppEntry getDetails(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    return sc.findAppEntry(id);
  }

  /**
   * Check application status
   * 
   * @param id - Application ID
   */
  @Path("status/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AppEntry getStatus(@PathParam("id") String id) {
    HydraSolrClient sc = new HydraSolrClient();
    AppEntry appEntry = sc.findAppEntry(id);
    YarnClient.getStatus(appEntry);
    return appEntry;
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
    AppEntry app = sc.findAppEntry(id);
    Application yarnApp = app.getYarnfile();
    yarnApp.setState(ApplicationState.STOPPED);
    try {
      YarnClient.stopApp(yarnApp);
    } catch (JsonProcessingException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
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
    AppEntry app = sc.findAppEntry(id);
    Application yarnApp = app.getYarnfile();
    yarnApp.setState(ApplicationState.STARTED);
    try {
      YarnClient.restartApp(yarnApp);
    } catch (JsonProcessingException e) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    return Response.ok().build();
  }
}
