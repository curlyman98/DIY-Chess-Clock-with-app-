#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32_BT"); // Name of your ESP32 Bluetooth device
  Serial.println("Bluetooth started. Pair with 'ESP32_BT'.");
}

void loop() {
  if (Serial.available()) {
    SerialBT.write(Serial.read()); // Send from Serial Monitor to BT
  }
  if (SerialBT.available()) {
    Serial.write(SerialBT.read()); // Send from BT device to Serial Monitor
  }
}
