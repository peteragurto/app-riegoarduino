# RiegoApp
---
## Uso 
Para poder usar esta aplicación fue necesario este código en Arduino

```objectivec
#include <SPI.h>
#include <Ethernet.h>
#include <ArduinoJson.h>

// Dirección MAC del shield Ethernet
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };

// Objeto EthernetServer para crear el servidor web en el puerto 80
EthernetServer server(80);

// Pines
const int sensorPin = A0;
const int relayPin = 8;
const int led1Pin = 2;
const int led2Pin = 3;

void setup() {
// Iniciar comunicación serial
Serial.begin(9600);

// Configurar pines
pinMode(sensorPin, INPUT);
pinMode(relayPin, OUTPUT);
pinMode(led1Pin, OUTPUT);
pinMode(led2Pin, OUTPUT);

// Asegurarse de que el relé esté apagado inicialmente
digitalWrite(relayPin, LOW);
digitalWrite(led1Pin, LOW);
digitalWrite(led2Pin, HIGH);

// Iniciar Ethernet
if (Ethernet.begin(mac) == 0) {
    Serial.println("No se pudo obtener una dirección IP a través de DHCP.");
    while (true);
    }
    
    // Iniciar el servidor
    server.begin();
    Serial.print("Mi dirección IP: ");
    Serial.println(Ethernet.localIP());
    }
    
    void loop() {
    // Leer el valor del sensor de humedad
    int sensorValue = analogRead(sensorPin);
    Serial.println(sensorValue);
    
    // Comprobar si hay un cliente disponible
    EthernetClient client = server.available();
    if (client) {
    Serial.println("Nuevo cliente");
    boolean currentLineIsBlank = true;
    String currentRequest = "";
    while (client.connected()) {
    if (client.available()) {
    char c = client.read();
    Serial.write(c);
    currentRequest += c;
    
            // Verificar si se ha recibido una línea en blanco que indica el final de la solicitud del cliente
            if (c == '\n' && currentLineIsBlank) {
              // Manejar solicitudes específicas
              if (currentRequest.indexOf("GET /on") >= 0) {
                digitalWrite(relayPin, HIGH); // Encender el relé
                digitalWrite(led1Pin, HIGH); // Encender el LED1
                digitalWrite(led2Pin, LOW);
                Serial.println("Riego encendido");
              }
              if (currentRequest.indexOf("GET /off") >= 0) {
                digitalWrite(relayPin, LOW); // Apagar el relé
                digitalWrite(led1Pin, LOW);
                digitalWrite(led2Pin, HIGH);
                Serial.println("Riego apagado");
              }
    
              // Crear un objeto JSON
              StaticJsonDocument<200> jsonDoc;
              jsonDoc["sensorValue"] = sensorValue;
              jsonDoc["relayState"] = digitalRead(relayPin) == HIGH ? "on" : "off";
    
              String jsonResponse;
              serializeJson(jsonDoc, jsonResponse);
    
              // Enviar una respuesta estándar HTTP al cliente
              client.println("HTTP/1.1 200 OK");
              client.println("Content-Type: application/json");
              client.println("Connection: close");
              client.println();
              client.println(jsonResponse);
              break;
            }
            if (c == '\n') {
              currentLineIsBlank = true;
            } else if (c != '\r') {
              currentLineIsBlank = false;
            }
          }
        }
    
        // Dar tiempo al navegador para recibir los datos antes de cerrar la conexión
        delay(1);
        client.stop();
        Serial.println("Cliente desconectado");
    }
}
```