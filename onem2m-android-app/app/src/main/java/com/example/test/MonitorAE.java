package com.example.test;

import android.os.Handler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
//该Class弃用
public class MonitorAE {

    String cseUri;
    String aeID;
    String aeName;
    String aeIP;
    int aePort;

    public MonitorAE(String cseUri, String aeID, String aeName, String aeIP, int aePort) {
        this.cseUri = cseUri;
        this.aeID = aeID;
        this.aeName = aeName;
        this.aeIP = aeIP;
        this.aePort = aePort;
    }

    public void createAE(MonitorAE monitorAE, Handler handler) {
        System.out.println("\n[REQUEST]");
        String method = "POST";
        String uri = cseUri + "/server";
        String resourceType = "2";
        //create request content
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> cmap = new HashMap<>();
        map.put("m2m:ae", cmap);
        cmap.put("rn", aeName);
        cmap.put("api", "app.example.com");
        cmap.put("rr", "true");
        cmap.put("poa", new String[]{"http://" + monitorAE.aeIP + ":" + monitorAE.aePort});
        //HttpURLConnection
        String representation = new JSONObject(map).toString();
        System.out.println(method + " " + uri);
        System.out.println(representation);
        CSEHttpConnection connection = new CSEHttpConnection();
        connection.createPostConnection(monitorAE, uri, resourceType, representation, handler);
    }

    public void createSubscription(MonitorAE monitorAE, String MCUName, String deviceName, Handler handler) {
        System.out.println("\n[REQUEST]");
        String uri = cseUri + "/server/" + MCUName + "/" + deviceName;
        String resourceType = "23";
        //create request content
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> cmap = new HashMap<>();
        map.put("m2m:sub", cmap);
        cmap.put("rn", "sub");
        cmap.put("nu", new String[]{"Cae-monitor"});
        cmap.put("nct", 2);
        cmap.put("enc", new HashMap<>().put("net", 3));
        String representation = new JSONObject(map).toString();
        //HttpURLConnection
        CSEHttpConnection connection = new CSEHttpConnection();
        connection.createPostConnection(monitorAE, uri, resourceType, representation, handler);
    }

    public void restAE(MonitorAE monitorAE, Handler handler) {
        System.out.println("\n[REQUEST]");
        String method = "DELETE";
        String uri = cseUri + "/server/" + aeName;
        System.out.println(method + " " + uri);
        CSEHttpConnection connection = new CSEHttpConnection();
        connection.crateDeleteConnection(monitorAE, uri, handler);
    }

    public void testcreateSubscription(MonitorAE monitorAE, String deviceName, Handler handler) {
        System.out.println("\n[REQUEST]");
        String uri = cseUri + "/server/" + deviceName + "/data";
        String resourceType = "23";
        //create request content
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> cmap = new HashMap<>();
        map.put("m2m:sub", cmap);
        cmap.put("rn", "sub");
        cmap.put("nu", new String[]{"Cae-monitor"});
        cmap.put("nct", 2);
        cmap.put("enc", new HashMap<>().put("net", 3));
        String representation = new JSONObject(map).toString();
        //HttpURLConnection
        CSEHttpConnection connection = new CSEHttpConnection();
        connection.createPostConnection(monitorAE, uri, resourceType, representation, handler);
    }
}
