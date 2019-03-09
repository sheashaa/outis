
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#define FORWARD                 0
#define REVERSE                 1
#define STOP                    2

#define ON                      1
#define OFF                     0

#define MOTOR_LEFT_DIR1         D0
#define MOTOR_LEFT_DIR2         D1
#define MOTOR_RIGHT_DIR1        D2
#define MOTOR_RIGHT_DIR2        D3

#define GROUND_SPINNER          D7 // D4
#define FRONT_SPINNER           D5 // D6

WiFiUDP Udp;

const char *ssid = "OUTISDarkLord";
const char *password = "";
unsigned int localUdpPort = 4210;

boolean IsRunning;

void setup()
{
  pinMode(MOTOR_LEFT_DIR1, OUTPUT);
  pinMode(MOTOR_LEFT_DIR2, OUTPUT);
  pinMode(MOTOR_RIGHT_DIR1, OUTPUT);
  pinMode(MOTOR_RIGHT_DIR2, OUTPUT);
  pinMode(GROUND_SPINNER, OUTPUT);
  pinMode(FRONT_SPINNER, OUTPUT);

  digitalWrite(MOTOR_LEFT_DIR1, LOW);
  digitalWrite(MOTOR_LEFT_DIR2, LOW);
  digitalWrite(MOTOR_RIGHT_DIR1, LOW);
  digitalWrite(MOTOR_RIGHT_DIR2, LOW);
  digitalWrite(GROUND_SPINNER, LOW);
  digitalWrite(FRONT_SPINNER, LOW);

  ESP.eraseConfig();
  if (WiFi.status() == WL_CONNECTED) WiFi.disconnect();

  Serial.begin(115200);
  Serial.println();

  WiFi.softAP(ssid, password, true, 1);

  Udp.begin(localUdpPort);
  Stop();
  IsRunning = true;
}

void loop()
{
  int packetSize = Udp.parsePacket();
  if (packetSize)
  {
    char InputType, InputValue;

    while (!isAlpha(InputType = Udp.read()));
    while (!isdigit(InputValue = Udp.read()));

    int num = InputValue - '0';
    while (isdigit(InputValue = Udp.read()))
    {
      num = (num * 10) + (InputValue - '0');  //some magic
    }
    ProcessKeys(InputType, num);
  }
}
