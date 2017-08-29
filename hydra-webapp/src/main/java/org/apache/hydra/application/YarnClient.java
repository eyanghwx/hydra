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
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.service.api.records.Application;
import org.apache.hydra.model.AppEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class YarnClient {

  private static final Log LOG = LogFactory.getLog(YarnClient.class);
  private static String YARN_SERVICES_API_URL;

  static {
    // Locate Solr URL
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream input = classLoader.getResourceAsStream("hydra.properties");
    Properties properties = new Properties();
    try {
      properties.load(input);
      YARN_SERVICES_API_URL = properties.getProperty("yarn_services_api_url");
    } catch (IOException e) {
      LOG.error("Error in loading Hydra configuration: ", e);
    }
  }

  public static void createApp(Application app) {
    Client client = Client.create();

    WebResource webResource = client
       .resource(YARN_SERVICES_API_URL + "/services/v1/applications");

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ClientResponse response;
    try {
      response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, mapper.writeValueAsString(app));
      if (response.getStatus() >= 299) {
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus());
      }
    } catch (UniformInterfaceException | JsonProcessingException e) {
      LOG.error("Error in deploying application: ", e);
    }
  }
  
  public static void deleteApp(String appInstanceId) {
    Client client = Client.create();

    WebResource webResource = client
       .resource(YARN_SERVICES_API_URL + "/services/v1/applications/"+appInstanceId);

    ClientResponse response;
    try {
      response = webResource.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
      if (response.getStatus() >= 299) {
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus());
      }
    } catch (UniformInterfaceException e) {
      LOG.error("Error in deleting application: ", e);
    }
  }
  
  public static void restartApp(Application app) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Client client = Client.create();
    String appInstanceId = app.getName();
    String yarnFile = mapper.writeValueAsString(app);
    WebResource webResource = client
       .resource(YARN_SERVICES_API_URL + "/services/v1/applications/"+appInstanceId);

    ClientResponse response;
    try {
      response = webResource.type(MediaType.APPLICATION_JSON).put(ClientResponse.class, yarnFile);
      if (response.getStatus() >= 299) {
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus());
      }
    } catch (UniformInterfaceException e) {
      LOG.error("Error in restarting application: ", e);
    }
  }
  
  public static void stopApp(Application app) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Client client = Client.create();
    String appInstanceId = app.getName();
    String yarnFile = mapper.writeValueAsString(app);
    WebResource webResource = client
        .resource(YARN_SERVICES_API_URL + "/services/v1/applications/"+appInstanceId);

     ClientResponse response;
     try {
       response = webResource.type(MediaType.APPLICATION_JSON).put(ClientResponse.class, yarnFile);
       if (response.getStatus() >= 299) {
         throw new RuntimeException("Failed : HTTP error code : "
        + response.getStatus());
       }
     } catch (UniformInterfaceException e) {
       LOG.error("Error in stopping application: ", e);
     }
  }

  public static void getStatus(AppEntry entry) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Application app = null;
    Client client = Client.create();
    WebResource webResource = client
        .resource(YARN_SERVICES_API_URL + "/services/v1/applications/"+entry.getName());
     try {
       String yarnFile = webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
       app = mapper.readValue(yarnFile, org.apache.hadoop.yarn.service.api.records.Application.class);
       entry.setYarnfile(app);
     } catch (UniformInterfaceException | IOException e) {
       LOG.error("Error in fetching application status: ", e);
     }
  }
}
