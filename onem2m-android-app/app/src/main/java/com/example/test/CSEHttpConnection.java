package com.example.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
//该Class文件弃用
public class CSEHttpConnection {

    //Http Post
    public void createPostConnection(MonitorAE monitorAE, String uri, String resourceType, String representation, Handler handler) {
        //new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("\n[REQUEST]");
                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(3000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(true);
                    //set request head
                    conn.addRequestProperty("X-M2M-Origin", monitorAE.aeID);
                    conn.addRequestProperty("X-M2M-RI", createRequestID());
                    conn.setRequestProperty("Content-Type", "application/json;ty=" + resourceType);
                    conn.connect();
                    OutputStream out = conn.getOutputStream();
                    out.write(representation.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    out.close();
                    //read and send response
                    //
                    String key = "POST";
                    String value = "";

                    System.out.println("\n[RESPONSE]");
                    if (conn.getResponseCode() == 409) {
                        value = conn.getResponseMessage();
                        sendResponseBody(key, value, handler);
                        value = "AE-ID already registered\n";
                        sendResponseBody(key, value, handler);
                        throw new Exception(conn.getResponseMessage());
                    } else {
                        System.out.println(conn.getResponseCode());
                        System.out.println(resolveInputStream(conn));
                        value = resolveInputStream(conn);
                        sendResponseBody(key, value, handler);
                        value = "AE registering\n";
                        sendResponseBody(key, value, handler);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //Http Delete
    public void crateDeleteConnection(MonitorAE monitorAE, String uri, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("\n[REQUEST]");
                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");
                    conn.setConnectTimeout(3000);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(true);
                    //set request head
                    conn.addRequestProperty("X-M2M-Origin", monitorAE.aeID);
                    conn.addRequestProperty("X-M2M-RI", createRequestID());
                    conn.connect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void createGetConnection(MonitorAE monitorAE, String uri, Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("\n[REQUEST]");
                try {
                    URL url = new URL(uri + "/" + monitorAE.aeName);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(true);
                    //set request head
                    conn.addRequestProperty("X-M2M-Origin", monitorAE.aeID);
                    conn.addRequestProperty("X-M2M-RI", createRequestID());
                    conn.connect();
                    //
                    String key = "GET";
                    String value = "";
                    value = resolveInputStream(conn);
                    sendResponseBody(key, value, handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //resolve input stream
    private String resolveInputStream(HttpURLConnection connection) throws IOException {
        InputStream in = connection.getInputStream();
        String responseBody;
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            baos.write((byte) result);
            result = bis.read();
        }
        responseBody = baos.toString();
        return responseBody;
    }

    //create requestID(Maybe it doesn't meet the oneM2M standard)
    private String createRequestID() {
        return String.valueOf(Math.floor(Math.random() * 10000));
    }

    private void sendResponseBody(String key, String value, Handler handler) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
