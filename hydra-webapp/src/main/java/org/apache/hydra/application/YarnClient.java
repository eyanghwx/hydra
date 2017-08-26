package org.apache.hydra.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.apache.hydra.model.AppStatus;
import org.apache.hadoop.yarn.service.api.records.Application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class YarnClient {
  
  public static void createApp(Application app) {
    Client client = Client.create();

    WebResource webResource = client
       .resource("http://eyang-1:9191/services/v1/applications");

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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void deleteApp(String appInstanceId) {
    Client client = Client.create();

    WebResource webResource = client
       .resource("http://eyang-1:9191/services/v1/applications/"+appInstanceId);

    ClientResponse response;
    try {
      response = webResource.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
      if (response.getStatus() >= 299) {
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus());
      }
    } catch (UniformInterfaceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void restartApp(String appInstanceId, Application app) {
    Client client = Client.create();

    WebResource webResource = client
       .resource("http://eyang-1:9191/services/v1/applications/"+appInstanceId);

    ClientResponse response;
    try {
      response = webResource.type(MediaType.APPLICATION_JSON).put(ClientResponse.class, app);
      if (response.getStatus() >= 299) {
        throw new RuntimeException("Failed : HTTP error code : "
       + response.getStatus());
      }
    } catch (UniformInterfaceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void stopApp(String appInstanceId, Application app) {
    Client client = Client.create();
    WebResource webResource = client
        .resource("http://eyang-1:9191/services/v1/applications/"+appInstanceId);

     ClientResponse response;
     try {
       response = webResource.type(MediaType.APPLICATION_JSON).put(ClientResponse.class, app);
       if (response.getStatus() >= 299) {
         throw new RuntimeException("Failed : HTTP error code : "
        + response.getStatus());
       }
     } catch (UniformInterfaceException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
  }

  public static void getStatus(AppStatus status) {
    Client client = Client.create();
    WebResource webResource = client
        .resource("http://eyang-1:9191/services/v1/applications/"+status.getId());

     try {
       Application app = webResource.accept(MediaType.APPLICATION_JSON).get(Application.class);
       status.setState(app.getState().name());
       status.setTracker(new URI(app.getUri()));
     } catch (UniformInterfaceException | URISyntaxException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
  }
}
