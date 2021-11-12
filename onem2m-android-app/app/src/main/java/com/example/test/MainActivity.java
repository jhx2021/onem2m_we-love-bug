package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
//该项目并未使用CSEHttpConnection.class和MonitorAE.class
public class MainActivity extends AppCompatActivity {
    //
    TextView logout;
    EditText cseUri;
    Button Start, Stop;
    Boolean isStopThread = false;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Init control
        Start = findViewById(R.id.start);
        Stop = findViewById(R.id.stop);
        logout = findViewById(R.id.logout);
        cseUri = findViewById(R.id.ipadress_text);
        cseUri.setText("172.20.10.3");
    }

    public void StartOnClick(View view) {
        clearText(logout);
        try {
            startConnect();
            isStopThread = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void StopOnClick(View view) {
        isStopThread = true;
    }

    private void startConnect() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                int n = 1;
                while (true) {
                    Map<Integer, String> hashMap = new HashMap();
                    hashMap.put(1, "temperature");
                    hashMap.put(2, "humidity");
                    hashMap.put(3, "turbidity");
                    hashMap.put(4, "ph");
                    //
                    URL url = null;
                    try {
                        url = new URL("http://" + cseUri.getText() + ":8080/server/mydevice1/" + hashMap.get(n) + "/la");
//                Tip("The connection is successful");
                    } catch (MalformedURLException e) {
//                Tip("The connection fails");
                        e.printStackTrace();
                    }
                    try {
                        assert url != null;
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setConnectTimeout(3000);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
                        conn.setInstanceFollowRedirects(true);
                        conn.connect();
                        Thread.sleep(1000);
                        String con = getResponseBodyCon(resolveInputStream(conn));
                        addText(logout, hashMap.get(n) + ": " + con);
                        System.out.println(con);
                    } catch (IOException | JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    n++;
                    if (n > 4) {
                        n = 1;
                        addText(logout, "----------");
                    }
                    if (isStopThread) {
                        break;
                    }
                }

            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private String getResponseBodyCon(String body) throws JSONException {
        JSONObject jsonObject = new JSONObject(body);
        return jsonObject.getJSONObject("m2m:cin").getString("con");

    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    private void clearText(TextView mTextView) {
        mTextView.setText("");
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

//    private void Tip(String text) {
//        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
//    }
}