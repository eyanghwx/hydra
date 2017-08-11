package org.apache.hydra.application;

import java.io.IOException;

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
}
