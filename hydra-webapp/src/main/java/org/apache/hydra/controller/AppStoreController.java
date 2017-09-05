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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hydra.application.HydraSolrClient;

import org.apache.hydra.model.AppStoreEntry;
import org.apache.hydra.model.Application;

@Path("/app_store")
public class AppStoreController {

  private static final Log LOG = LogFactory.getLog(AppStoreController.class);

  public AppStoreController() {
  }

  /**
   * Display the most frequently used applications on Hydra home page.
   * 
   * @apiGroup AppStoreController
   * @apiName get
   * @api {get} /app_store/recommended  Display recommended applications.
   * @apiSuccess {Object} AppEntry Application configuration.
   * @apiSuccessExample {json} Success-Response:
   *     HTTP/1.1 200 OK
   *     [  
   *        {  
   *           "id":"96b7833a-e3",
   *           "org":"Hortonworks",
   *           "name":"LAMP",
   *           "desc":"Linux Apache MySQL PHP web application",
   *           "icon":"/css/img/hwx_logo.png",
   *           "like":0,
   *           "download":0,
   *           "app":null
   *        },
   *        {
   *        ...
   *        }
   *     ]
   * @return - List of YARN applications
   */
  @GET
  @Path("recommended")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppStoreEntry> get() {
    HydraSolrClient sc = new HydraSolrClient();
    return sc.getRecommendedApps();
  }

  /**
   * Search for yarn applications from solr.
   * 
   * @apiGroup AppStoreController
   * @apiName search
   * @api {get} /app_store/search  Find application from appstore.
   * @apiParam {String} q Keyword to search.
   * @apiSuccess {Object} AppStoreEntry List of matched applications.
   * @apiSuccessExample {json} Success-Response:
   *     HTTP/1.1 200 OK
   *     [  
   *        {  
   *           "id":"96b7833a-e3",
   *           "org":"Hortonworks",
   *           "name":"LAMP",
   *           "desc":"Linux Apache MySQL PHP web application",
   *           "icon":"/css/img/hwx_logo.png",
   *           "like":0,
   *           "download":0,
   *           "app":null
   *        },
   *        {
   *        ...
   *        }
   *     ]
   * @param keyword - search for keyword
   * @return - List of YARN applications matching keyword search.
   */
  @GET
  @Path("search")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AppStoreEntry> search(@QueryParam("q") String keyword) {
    HydraSolrClient sc = new HydraSolrClient();
    return sc.search(keyword);
  }
  
  /**
   * Register an application
   * 
   * @apiGroup AppStoreController
   * @apiName register
   * @api {post} /app_store/register  Register an application in appstore.
   * @apiParam {Object} app Application definition.
   * @apiParamExample {json} Request-Example:
   *     {
   *       "name": "Jenkins",
   *       "organization": "Jenkins-ci.org",
   *       "description": "The leading open source automation server",
   *       "icon": "/css/img/jenkins.png",
   *       "lifetime": "3600",
   *       "components": [
   *         {
   *           "name": "jenkins",
   *           "number_of_containers": 1,
   *           "artifact": {
   *             "id": "eyang-1.openstacklocal:5000/jenkins:latest",
   *             "type": "DOCKER"
   *           },
   *           "launch_command": "",
   *           "resource": {
   *             "cpus": 1,
   *             "memory": "2048"
   *           },
   *           "configuration": {
   *             "env": {
   *             },
   *             "files": [
   *             ]
   *           }
   *         }
   *       ],
   *       "quicklinks": {
   *         "Jenkins UI": "http://jenkins.${SERVICE_NAME}.${USER}.${DOMAIN}:8080/"
   *       }
   *     }
   * @apiSuccess {String} Response Application register result.
   * @apiError BadRequest Error in process application registration.
   * @param app - Yarnfile in JSON form
   * @return Web response
   */
  @POST
  @Path("register")
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(Application app) {
    try {
      LOG.info(app.toString());
      HydraSolrClient sc = new HydraSolrClient();
      sc.register(app);
    } catch (IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
    return Response.status(Status.ACCEPTED).build();
  }
}
