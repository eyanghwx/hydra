package org.apache.hydra.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hydra.model.AppStatus;

public class YarnClient {
  public static void createApp(String appInstanceId, String appName) {
    String[] cmd = {
      "/usr/bin/ssh",
      "spark@eyang-1",
      "slider", 
      "create",
      appInstanceId,
      "--template",
      appName + "/configuration/appConfig-default.json",
      "--resources",
      appName + "/configuration/resources-default.json"
    };
    try {
      Process process = Runtime.getRuntime().exec(cmd,null);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void deleteApp(String appInstanceId) {
    String[] cmd = {
        "/usr/bin/ssh",
        "spark@eyang-1",
        "slider",
        "stop",
        appInstanceId
    };
    try {
      Process process = Runtime.getRuntime().exec(cmd,null);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    String[] cmd2 = {
        "/usr/bin/ssh",
        "spark@eyang-1",
        "slider",
        "destroy",
        appInstanceId,
        "--force"
    };
    try {
      Process process = Runtime.getRuntime().exec(cmd2,null);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void restartApp(String appInstanceId) {
    String[] cmd = {
        "/usr/bin/ssh",
        "spark@eyang-1",
        "slider",
        "start",
        appInstanceId
    };
    try {
      Process process = Runtime.getRuntime().exec(cmd,null);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void stopApp(String appInstanceId) {
    String[] cmd = {
        "/usr/bin/ssh",
        "spark@eyang-1",
        "slider",
        "stop",
        appInstanceId
    };
    try {
      Process process = Runtime.getRuntime().exec(cmd,null);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void getStatus(AppStatus status) {
    String[] cmd = {
        "/usr/bin/ssh",
        "spark@eyang-1",
        "slider",
        "list"
    };
    try {
      ProcessBuilder pb = new ProcessBuilder(cmd);
      Process p = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if(line.startsWith(status.getId())) {
          String[] tmp = line.split("\\s+");
          status.setState(tmp[1]);
          if(tmp.length>=4) {
            URI tracker = new URI(tmp[3]);
            status.setTracker(tracker);
          }
        }
      }
    } catch (IOException | URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
