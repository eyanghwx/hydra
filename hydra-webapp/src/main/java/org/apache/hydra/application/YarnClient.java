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
import java.util.List;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.service.api.records.Service;
import org.apache.hadoop.yarn.util.RMHAUtils;
import org.apache.hydra.model.AppEntry;
import org.eclipse.jetty.util.UrlEncoded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class YarnClient {

  private static final Log LOG = LogFactory.getLog(YarnClient.class);
  private static Configuration conf = null;
//  private static String YARN_SERVICES_API_URL;
//  private static boolean kerberosSecurity = false;
//  private static String runAsUser = "hbase";

//  static {
//    // Locate Solr URL
//    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//    InputStream input = classLoader.getResourceAsStream("hydra.properties");
//    Properties properties = new Properties();
//    try {
//      properties.load(input);
//      YARN_SERVICES_API_URL = getRMWebAddress();
//    } catch (IOException e) {
//      LOG.error("Error in loading Hydra configuration: ", e);
//    }
//  }

  /**
   * Calculate Resource Manager address base on working REST API.
   */
  private String getRMWebAddress() {
    Configuration yconf = new YarnConfiguration();
    if (conf==null) {
      conf = yconf;
    }
    String scheme = "http://";
    String path = "/app/v1/services/version";
    String rmAddress = conf.get("yarn.resourcemanager.webapp.address");
    if (YarnConfiguration.useHttps(conf)) {
      scheme = "https://";
      rmAddress = conf.get("yarn.resourcemanager.webapp.https.address");
    }
    boolean useKerberos = UserGroupInformation.isSecurityEnabled();
    LOG.info("security enabled: "+useKerberos);
    List<String> rmServers = RMHAUtils.getRMHAWebappAddresses(new YarnConfiguration(conf));
    for (String host : rmServers) {
      LOG.info("RM host: " + host);
      try {
        Client client = Client.create();
        StringBuilder sb = new StringBuilder();
        sb.append(scheme);
        sb.append(host);
        sb.append(path);
        if (!useKerberos) {
          try {
            String username = UserGroupInformation.getCurrentUser().getShortUserName();
            sb.append("?user.name=");
            sb.append(username);
          } catch (IOException e) {
            LOG.debug("Fail to resolve username: {}", e);
          }
        }
        WebResource webResource = client.resource(sb.toString());
        if (useKerberos) {
          UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
          for (Token<? extends TokenIdentifier> str : ugi.getTokens()) {
            LOG.info("Token is " + str.encodeToUrlString());
          }
          AuthenticatedURL.Token token = new AuthenticatedURL.Token();
          webResource.header("WWW-Authenticate", token);
          LOG.info("mytoken: " + token);
        }
        ClientResponse test = webResource.get(ClientResponse.class);
        LOG.info("Test status:" + test.getStatus());
        if (test.getStatus() == 200) {
          rmAddress = host;
          break;
        }
      } catch (Exception e) {
        LOG.warn("Fail to connect to: " + host, e);
      }
    }
    return scheme + rmAddress;
  }

  /**
   * Compute active resource manager API service location.
   *
   * @param appName - YARN service name
   * @return URI to API Service
   * @throws IOException
   */
  private String getApiUrl(String appName) throws IOException {
    String url = getRMWebAddress();
    LOG.info("Connect to: "+url);
    StringBuilder api = new StringBuilder();
    api.append(url);
    api.append("/app/v1/services");
    if (appName != null) {
      api.append("/");
      api.append(appName);
    }
    Configuration conf = new Configuration();
    if (conf.get("hadoop.http.authentication.type").equalsIgnoreCase("simple")) {
      api.append("?user.name=" + UrlEncoded
          .encodeString(System.getProperty("user.name")));
    }
    return api.toString();
  }

  private Builder getApiClient() throws IOException {
    return getApiClient(null);
  }

  private static ClientConfig getClientConfig() {
    ClientConfig config = new DefaultClientConfig();
    config.getProperties().put(
        ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 0);
    config.getProperties().put(
        ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true);
    return config;
  }

  /**
   * Setup API service web request.
   *
   * @param appName
   * @return
   * @throws IOException
   */
  private Builder getApiClient(String appName) throws IOException {
    Client client = Client.create(getClientConfig());
    Configuration conf = new Configuration();
    client.setChunkedEncodingSize(null);
    Builder builder = client
        .resource(getApiUrl(appName)).type(MediaType.APPLICATION_JSON);
    if (conf.get("hadoop.http.authentication.type").equals("kerberos")) {
      AuthenticatedURL.Token token = new AuthenticatedURL.Token();
      builder.header("WWW-Authenticate", token);
    }
    return builder
        .accept("application/json;charset=utf-8");
  }

  public void createApp(Service app) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ClientResponse response;
    try {
      response = getApiClient().post(ClientResponse.class, mapper.writeValueAsString(app));
      if (response.getStatus() >= 299) {
        String message = response.getEntity(String.class);
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus() + " error: " + message);
      }
    } catch (UniformInterfaceException | ClientHandlerException | IOException e) {
      LOG.error("Error in deploying application: ", e);
    }
  }

  public void deleteApp(String appInstanceId) {
    ClientResponse response;
    try {
      response = getApiClient(appInstanceId).delete(ClientResponse.class);
      if (response.getStatus() >= 299) {
        String message = response.getEntity(String.class);
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus() + " error: " + message);
      }
    } catch (UniformInterfaceException | ClientHandlerException | IOException e) {
      LOG.error("Error in deleting application: ", e);
    }
  }

  public void restartApp(Service app) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String appInstanceId = app.getName();
    String yarnFile = mapper.writeValueAsString(app);
    ClientResponse response;
    try {
      response = getApiClient(appInstanceId).put(ClientResponse.class, yarnFile);
      if (response.getStatus() >= 299) {
        String message = response.getEntity(String.class);
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus() + " error: " + message);
      }
    } catch (UniformInterfaceException | ClientHandlerException | IOException e) {
      LOG.error("Error in restarting application: ", e);
    }
  }
  
  public void stopApp(Service app) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String appInstanceId = app.getName();
    String yarnFile = mapper.writeValueAsString(app);
     ClientResponse response;
     try {
       response = getApiClient(appInstanceId).put(ClientResponse.class, yarnFile);
       if (response.getStatus() >= 299) {
         String message = response.getEntity(String.class);
         throw new RuntimeException("Failed : HTTP error code : "
        + response.getStatus() + " error: " + message);
       }
     } catch (UniformInterfaceException | ClientHandlerException | IOException e) {
       LOG.error("Error in stopping application: ", e);
     }
  }

  public void getStatus(AppEntry entry) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String appInstanceId = entry.getName();
    Service app = null;
     try {
       String yarnFile = getApiClient(appInstanceId).get(String.class);
       app = mapper.readValue(yarnFile, Service.class);
       entry.setYarnfile(app);
     } catch (UniformInterfaceException | IOException e) {
       LOG.error("Error in fetching application status: ", e);
     }
  }
}
