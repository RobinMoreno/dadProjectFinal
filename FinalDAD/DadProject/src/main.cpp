
#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>
#include <RestClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <PubSubClient.h>


//DECLARACIÓN DE PINES
int wifiPin = D0; //D0
int powerPin = D7; //D7
int tempPin = D2; //D2
int buzzerPin = D3; //D3
int smokePin = A0; //D6



OneWire tempSense(tempPin);
double actualTemp = 0.0;
const char* id = "1";
String response = "";
int state = 0;
const char* name = "";
double temp = 0.0;
int smoke = 0;
long countsmoke = 0;

DallasTemperature temperature(&tempSense);

//CONEXIÓN CON LA APREST

const char* ssid = "AndroidAP5991";
const char* pass = "pass4321";
char IP[] = "192.168.43.66";

RestClient client = RestClient(IP, 80);

//DECALARACIÓN DE FUNCIONES

	void reconnect();
	void tempUpdate();
	void loadData();
	void smokeUpdate();
	void readTemperature();
	void turnOff();
	void turnOn();
//CONEXIÓN CON MQTT

WiFiClient espClient;
PubSubClient pubsubClient(espClient);

void callback(char* topic, byte* payload, unsigned int length) {
	Serial.print("Mensaje recibido [");
	Serial.print(topic);
	Serial.print("] ");
	String message = String((char *)payload);
	Serial.println(message);

		if(message == "temperature"){
			Serial.println("Actualizando Temperatura");
				tempUpdate();
		}else if(message == "smoke"){
			Serial.println("Actualizando Humo");
				smokeUpdate();
		}else if(message == "turnOff"){
			Serial.println("Apagando dispositivo");
				turnOff();
		}else if(message == "turnOnn"){
			Serial.println("Encendiendo dispositivo");
				turnOn();
		}
}

void setup(){

	Serial.begin(9600);

	pinMode(buzzerPin, OUTPUT);
	pinMode(powerPin, OUTPUT);
	pinMode(wifiPin, OUTPUT);
	pinMode(smokePin, INPUT);

	WiFi.mode(WIFI_STA);
	WiFi.begin(ssid, pass);

while(WiFi.status() != WL_CONNECTED){

		delay(1000);
		Serial.print(".");
}

	Serial.println("Conexion establecida");
	Serial.print("IP asignada:");
	Serial.println(WiFi.localIP());

	pubsubClient.setServer(IP, 1883);
	pubsubClient.setCallback(callback);

	temperature.begin();
	digitalWrite(buzzerPin, LOW);
	digitalWrite(powerPin, LOW);
	digitalWrite(wifiPin, HIGH);
	smokeUpdate();

}

void reconnect() {

		while (!pubsubClient.connected()) {
			Serial.print("Conectando al servidor MQTT");
		if (pubsubClient.connect("MC")) {
			Serial.println("Conectado");
			pubsubClient.subscribe("Lecturas");
		} else {
			Serial.print("Error, rc=");
			Serial.print(pubsubClient.state());
			Serial.println(" Reintentando en 5 segundos");
			delay(5000);
		}
	}

}

void loop(){

loadData();
readTemperature();


if(state == 1 && actualTemp < temp){
	digitalWrite(powerPin, HIGH);

}
else if(state == 1 && actualTemp > temp){
	digitalWrite(powerPin, LOW);

}else if(state == 0){
	digitalWrite(powerPin, LOW);
	turnOff();

}

if(WiFi.status() != WL_CONNECTED){
	digitalWrite(wifiPin, LOW);
}


if(analogRead(smokePin) >= 400){
smoke = 1;
smokeUpdate();
turnOff();
digitalWrite(powerPin, LOW);

while(countsmoke != 20){
digitalWrite(buzzerPin, HIGH);
delay(400);
digitalWrite(buzzerPin, LOW);
delay(100);

Serial.print(smoke);

countsmoke += 1;
}

exit(0);

}

countsmoke = 0;

tempUpdate();

Serial.println(analogRead(smokePin));
Serial.println(state);
Serial.print(temp);
Serial.println("ºC");
Serial.print(actualTemp);
Serial.println("ºC");

if (!pubsubClient.connected()) {
    reconnect();
  }

	 pubsubClient.loop();

}

void loadData(){

String aux = "";

int statusCode = client.get("/api/devices/1",	&aux);

	const int size_t_capacity = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 120;

	DynamicJsonBuffer jsonBuffer(size_t_capacity);
	JsonObject& root = jsonBuffer.parseObject(aux);

		state = root["state"];
		name = root["name"];
		temp = root["temperature"];
		smoke = root["smoke"];
}

void tempUpdate(){

	const int size_t_capacity = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 120;
     DynamicJsonBuffer jsonBuffer(size_t_capacity);
	char json[256];
	 JsonObject& object = jsonBuffer.createObject();
	 object["temperature"] = actualTemp;
	 object["iddevice"] = 1;
	 object.printTo(json, sizeof(json));
	 int statusCode = client.post("/api/reading", json, &response);
}

void smokeUpdate(){

String auxiliar = "";

	const int size_t_capacity = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 120;
     DynamicJsonBuffer jsonBuffer(size_t_capacity);
	char json[256];
	 JsonObject& object = jsonBuffer.createObject();
	 object["smoke"] = smoke;
	 object["temperature"] = actualTemp;
	 object["iddevice"] = 1;
	 object.printTo(json, sizeof(json));
	 int statusCode = client.post("/api/reading/smoke", json, &auxiliar);

}

void readTemperature(){

	temperature.requestTemperatures();
	actualTemp = temperature.getTempCByIndex(0);
}

void turnOn(){

	const int size_t_capacity = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 120;
     DynamicJsonBuffer jsonBuffer(size_t_capacity);
	char json[256];
	 JsonObject& object = jsonBuffer.createObject();
	 object["id"] = id;
	 object.printTo(json, sizeof(json));
	 int statusCode = client.post("/api/devices/1/1", json, &response);


}

void turnOff(){

	const int size_t_capacity = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 120;
		 DynamicJsonBuffer jsonBuffer(size_t_capacity);
	char json[256];
	 JsonObject& object = jsonBuffer.createObject();
	 object["id"] = id;
	 object.printTo(json, sizeof(json));
	 int statusCode = client.post("/api/devices/1/0", json, &response);

}
