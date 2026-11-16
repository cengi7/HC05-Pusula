# 🧭 HC05 Pusula

Arduino + pusula sensörü (QMC5883L / HMC5883L) ile ölçülen yön bilgisini **HC-05 Bluetooth** modülü üzerinden alıp telefonda canlı pusula olarak gösteren Android uygulaması.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Java-ED8B00?logo=openjdk&logoColor=white)
![MinSDK](https://img.shields.io/badge/minSdk-24-blue)
![Arduino](https://img.shields.io/badge/Arduino-Uno%20%2F%20Nano-00979D?logo=arduino&logoColor=white)

> Uygulama telefonun kendi sensörlerini **kullanmaz**. Yön verisi tamamen Arduino'dan gelir.

## ✨ Özellikler

- HC-05 ile klasik Bluetooth (SPP) bağlantısı
- Eşleşmiş cihaz listesinden seçip tek dokunuşla bağlanma
- Canvas ile çizilmiş özel pusula kadranı (K / D / G / B) ve dönen ibre
- Yön kısaltması (ör. `KD`) ve derece (ör. `47.5°`) gösterimi
- Android 12+ Bluetooth izinleriyle uyumlu
- Bağlantı durumu ve hata mesajları

## 🧩 Nasıl çalışır?

```
[Pusula sensörü] --I2C--> [Arduino] --UART--> [HC-05] ~~Bluetooth~~> [Android uygulaması]
```

Arduino, her 200 ms'de bir satır gönderir. Uygulama bu satırı okuyup ibreyi döndürür.

### Veri formatı

```
YON,DERECE\n
```

| Örnek | Anlamı |
|---|---|
| `K,0` | Kuzey, 0° |
| `KD,47.5` | Kuzeydoğu, 47.5° |
| `G,181` | Güney, 181° |

Yön kısaltmaları: `K, KD, D, GD, G, GB, B, KB`.
Format hatalı satırlar (virgülle ayrılmış tam 2 parça olmayanlar) yok sayılır.

## 🔌 Donanım

| Parça | Not |
|---|---|
| Arduino Uno / Nano | |
| QMC5883L veya HMC5883L (GY-271) | I2C pusula modülü |
| HC-05 | Bluetooth modülü |
| 1 kΩ + 2 kΩ direnç | HC-05 RX için gerilim bölücü |

### Bağlantılar

| Arduino | Bağlanacağı yer |
|---|---|
| 5V | Sensör VCC, HC-05 VCC |
| GND | Sensör GND, HC-05 GND |
| A4 (SDA) | Sensör SDA |
| A5 (SCL) | Sensör SCL |
| D10 | HC-05 **TX** |
| D11 | HC-05 **RX** (gerilim bölücü üzerinden, 5V → 3.3V) |

## 💻 Arduino kodu

Arduino IDE'de **Library Manager**'dan `QMC5883LCompass` (MPrograms) kütüphanesini kurun.

```cpp
#include <Wire.h>
#include <QMC5883LCompass.h>
#include <SoftwareSerial.h>

QMC5883LCompass compass;
SoftwareSerial bt(10, 11); // RX, TX

const char* yonler[] = {"K","KD","D","GD","G","GB","B","KB"};

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
```

## 📱 Uygulamayı kullanma

1. Telefonun **Ayarlar → Bluetooth** bölümünden HC-05'i eşleştirin (PIN: `1234` veya `0000`).
2. Arduino'ya güç verin (HC-05 LED'i yanıp sönmeli).
3. Uygulamayı açın, **Bağlan** düğmesine basın ve izinleri verin.
4. Listeden HC-05'i seçin. Bağlantı kurulunca ibre dönmeye başlar.

## 🛠️ Kaynaktan derleme

Gereksinimler: **Android Studio** (güncel sürüm), JDK 17+.

```bash
git clone https://github.com/KULLANICI_ADI/HC05-Pusula.git
```

1. Android Studio ile klasörü açın, Gradle senkronizasyonunu bekleyin.
2. Gerçek bir telefonda çalıştırın (▶ Run).

> ⚠️ Emülatörlerde (BlueStacks dahil) Bluetooth çalışmaz; yalnızca arayüz görülebilir.

### İmzalı sürüm (AAB / APK)

Release imzası için kendi keystore dosyanızı oluşturun (**Build → Generate Signed App Bundle / APK**).
Keystore (`.jks`) ve şifreler **bu depoda bulunmaz**, `.gitignore` ile dışarıda tutulur.

## 📂 Proje yapısı

```
app/src/main/
├── java/com/cengiz/hc05pusula/
│   ├── MainActivity.java   # Bluetooth bağlantısı ve veri okuma
│   └── PusulaView.java     # Özel pusula çizimi
├── res/layout/activity_main.xml
└── AndroidManifest.xml
```

## 🔐 İzinler

| İzin | Neden |
|---|---|
| `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` | Android 12+ için HC-05'e bağlanma |
| `BLUETOOTH`, `BLUETOOTH_ADMIN` | Android 11 ve altı |
| `ACCESS_FINE_LOCATION` | Android 11 ve altında Bluetooth için gerekli |

## 🩺 Sorun giderme

| Sorun | Çözüm |
|---|---|
| Listede cihaz yok | HC-05'i önce telefon ayarlarından eşleştirin |
| Bağlantı hatası | HC-05'in başka cihaza bağlı olmadığını kontrol edin |
| İbre sapıyor | Sensörü kalibre edin, mıknatıs ve metalden uzak tutun |
| Veri gelmiyor | Baud hızı 9600 mü, TX/RX doğru mu kontrol edin |

## 📄 Lisans

MIT Lisansı. Ayrıntılar için `LICENSE` dosyasına bakın.

## 👤 Geliştirici

**Cengiz**
