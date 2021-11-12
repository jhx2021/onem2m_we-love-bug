#include <ESP8266WiFi.h>
#include "Timer.h"

#include<dht11.h>

#define cdin A0
#define dhtOut D5
#define cdzhuo D3
#define cdph D4
dht11 DHT11;

// WIFI params
const char *ssid = "HUAWEI Mate 30 Pro 5G";
const char *password = "sdgdsgdss";
// CSE params
const char *host = "192.168.43.202";//ipv4
const int httpPort = 8080;//port

// AE params
const int aePort = 80;//ae port
const char *origin = "Cae_device1";//ae origin

Timer t;
WiFiServer server(aePort);

float TU = 0.0;
float TU_value = 0.0;
float TU_calibration = 0.0;
float temp_data = 25.0;
float K_Value = 3347.19;
int tem = 0;
int hum = 0;
float phValue = 0;
unsigned long int avgValue;

void setup() {

    Serial.begin(9600);
    pinMode(dhtOut, INPUT);
    pinMode(cdin, INPUT);

    // Connect to WIFI network
    Serial.print("Connecting to ");
    Serial.println(ssid);

    WiFi.persistent(false);

    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println();
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    // Start HTTP server
    server.begin();
    Serial.println("Server started");

    // Create AE resource 
    String resulat = send("/server", 2,
                          "{\"m2m:ae\":{\"rn\":\"mydevice1\",\"api\":\"mydevice1.company.com\",\"rr\":\"true\",\"poa\":[\"http://" +
                          WiFi.localIP().toString() + ":" + aePort + "\"]}}");

    if (resulat == "HTTP/1.1 201 Created") {
        // Create Container resource
        //temperature
        send("/server/mydevice1", 3, "{\"m2m:cnt\":{\"rn\":\"temperature\"}}");

        // Create ContentInstance resource创建ContentInstance资源
        send("/server/mydevice1/temperature", 4, "{\"m2m:cin\":{\"con\":\"0\"}}");

        //humidity
        send("/server/mydevice1", 3, "{\"m2m:cnt\":{\"rn\":\"humidity\"}}");

        send("/server/mydevice1/humidity", 4, "{\"m2m:cin\":{\"con\":\"0\"}}");

        //turbidity
        send("/server/mydevice1", 3, "{\"m2m:cnt\":{\"rn\":\"turbidity\"}}");

        send("/server/mydevice1/turbidity", 4, "{\"m2m:cin\":{\"con\":\"0\"}}");
        //ph
        send("/server/mydevice1", 3, "{\"m2m:cnt\":{\"rn\":\"ph\"}}");

        send("/server/mydevice1/ph", 4, "{\"m2m:cin\":{\"con\":\"0\"}}");

        // Create Subscription resource
        send("/server/mydevice1/led", 23, "{\"m2m:sub\":{\"rn\":\"led_sub\",\"nu\":[\"Cae_device1\"],\"nct\":1}}");
    }
    t.every(1000 * 5, push);
}


void loop() {
    t.update();
    // Check if a client is connected
    WiFiClient client = server.available();
    if (!client) {
        return;
    }

    // Wait until the client sends some data
    Serial.println("new client");
    while (!client.available()) {
        delay(1);
    }

    // Read the request
    String req = client.readString();
    Serial.println(req);
    client.flush();


    // Send HTTP response to the client
    String s = "HTTP/1.1 200 OK\r\n";
    client.print(s);
    delay(1);
    Serial.println("Client disonnected");

}


// Method in charge of sending request to the CSE   
String send(String url, int ty, String rep) {

    // Connect to the CSE address 
    Serial.print("connecting to ");
    Serial.println(host);

    WiFiClient client;

    if (!client.connect(host, httpPort)) {
        Serial.println("connection failed");
        return "error";
    }


    // prepare the HTTP request
    String req = String() + "POST " + url + " HTTP/1.1\r\n" +
                 "Host: " + host + "\r\n" +
                 "X-M2M-Origin: " + origin + "\r\n" +
                 "Content-Type: application/json;ty=" + ty + "\r\n" +
                 "Content-Length: " + rep.length() + "\r\n"
                                                     "Connection: close\r\n\n" +
                 rep;

    Serial.println(req + "\n");

    // Send the HTTP request
    client.print(req);

    unsigned long timeout = millis();
    while (client.available() == 0) {
        if (millis() - timeout > 5000) {
            Serial.println(">>> Client Timeout !");
            client.stop();
            return "error";
        }
    }

    // Read the HTTP response
    String res = "";
    if (client.available()) {
        res = client.readStringUntil('\r');
        Serial.print(res);
    }
    while (client.available()) {
        String line = client.readStringUntil('\r');
        Serial.print(line);
    }

    Serial.println();
    Serial.println("closing connection");
    Serial.println();
    return res;

}


void push() {

    int dhtData = DHT11.read(dhtOut);
    int tem = (float) DHT11.temperature; //temperature
    int hum = (float) DHT11.humidity;    // humidity

    //turbitity
    pinMode(cdzhuo, LOW);
    pinMode(cdph, HIGH);
    delay(1000);
    int sensorValue = analogRead(A0);// read the input on analog pin 0:
    float TU = sensorValue *
               (5.0 / 1024.0); // Convert the analog reading (which goes from 0 - 1023) to a voltage (0 - 5V):
    TU_calibration = -0.0192 * (temp_data - 25) + TU;
    TU_value = -865.68 * TU_calibration + K_Value;

    if (TU_value <= 0) { TU_value = 0; }
    if (TU_value >= 3000) { TU_value = 3000; }

    //ph in
    pinMode(cdzhuo, HIGH);
    pinMode(cdph, LOW);
    delay(1000);
    int buf[10];
    for (int i = 0; i < 10; i++) {
        buf[i] = analogRead(cdin);
        delay[10];
    }
    for (int i = 0; i < 9; i++) {
        for (int j = i + 1; j < 10; j++) {
            if (buf[i] > buf[j]) {
                int temp = buf[i];
                buf[i] = buf[j];
                buf[j] = temp;

            }
        }
    }
    avgValue = 0;
    for (int i = 2; i < 8; i++) {
        avgValue += buf[i];
    }
    float phValue = (float) avgValue * 5.0 / 1024 / 6 - 1;
    Serial.print("phValue:");
    Serial.println(phValue, 4);
    phValue = 14 - 5.122 * (phValue - 1.758);


    Serial.print("temperature:");
    Serial.println(tem);
    Serial.print("humidity:");
    Serial.println(hum);

    Serial.print("TU Value:");
    Serial.print(TU_value); // print out the value you read:
    Serial.println("NTU");
    Serial.print("ph:");
    Serial.println(phValue);

    String data1 = String() + "{\"m2m:cin\":{\"con\":\"" + tem + "\"}}";
    String data2 = String() + "{\"m2m:cin\":{\"con\":\"" + hum + "\"}}";
    String data3 = String() + "{\"m2m:cin\":{\"con\":\"" + TU_value + "\"}}";
    String data4 = String() + "{\"m2m:cin\":{\"con\":\"" + phValue + "\"}}";
    send("/server/mydevice1/temperature", 4, data1);
    send("/server/mydevice1/humidity", 4, data2);
    send("/server/mydevice1/turbidity", 4, data3);
    send("/server/mydevice1/ph", 4, data4);

}
