/*
  HC05 Pusula - Arduino tarafı
  Sensör : QMC5883L (GY-271)
  Modül  : HC-05 (SoftwareSerial, 9600 baud)

  Bağlantılar:
    Sensör SDA -> A4, SCL -> A5, VCC -> 5V, GND -> GND
    HC-05 TX   -> D10 (Arduino RX)
    HC-05 RX   -> D11 (Arduino TX) - 1k/2k gerilim bölücü ile

  Gönderilen format (her satır):  YON,DERECE
  Örnek: KD,47
  Kütüphane: QMC5883LCompass (MPrograms) - Library Manager'dan kurun.
*/

#include <Wire.h>
#include <QMC5883LCompass.h>
#include <SoftwareSerial.h>

QMC5883LCompass compass;
SoftwareSerial bt(10, 11); // RX, TX

const char* yonler[] = {"K", "KD", "D", "GD", "G", "GB", "B", "KB"};

void setup() {
  bt.begin(9600);
  compass.init();
}

void loop() {
  compass.read();
  int az = compass.getAzimuth();
  if (az < 0) az += 360;

  int idx = ((az + 22) / 45) % 8;

  bt.print(yonler[idx]);
  bt.print(",");
  bt.println(az);

  delay(200);
}
