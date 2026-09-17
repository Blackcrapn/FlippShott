# FlippShott

FlippShott — мощный Android-мультитул для исследования беспроводных протоколов, тестирования кибербезопасности и эмуляции железа. Переносит продвинутый технический функционал на обычные Android-смартфоны **без root-прав**, а с root/Shizuku — расширяет возможности до системного уровня.

Релизы в Telegram: https://t.me/Dolphy_app_official

## Функционал и модули

### 🚨 Инфракрасный порт (IR)
- **Универсальные пульты** — база Flipper-IRDB-main + flipperzero-firmware-dev: TV, AC, Audio, Projectors
- **ИК-Шторм (TV Be Gone)** — массовая отправка сигналов выключения
- **ИК Глушилка** — блокирует чужие пульты

### 📶 Bluetooth & BLE
- **Сканер аудиоустройств** — поиск и тест колонок/наушников
- **NRF Scanner** — BLE разведка и анализ пакетов
- **Dolphy Chat** — локальный чат через BLE
- **HID Клавиатура/Мышь** — телефон как BT HID
- **Bluetooth Jammer** — L2CAP flood стресс-тест
- **WhisperPair / DolphyPair** — тихое сопряжение

### 📡 NFC & QR
- **NFC Инструменты** — чтение, запись, эмуляция (HCE Type 4), анализ, мастер-ключи
- **QR Multi-Tool** — генерация QR, WiFi QR, vCard, штрих-коды

### 🌐 Сеть и Wi-Fi
- **Wi-Fi Attacks** — сканирование, DoS симуляция, проверка паролей
- **LAN Scanner** — карта хостов, Wake-on-LAN, скан портов, админ-панели роутеров
- **WiFi Print** — IPP печать

### 📺 Медиа-протоколы
- **SmartTV Cast** — Chromecast, DIAL, DLNA, UPnP

---

### 🧪 **FLIPPSHOTT LAB** (24 новые функции)

| Функция | Описание | Root |
|---------|----------|:---:|
| **Sub-GHz Database** | Диапазоны 300-928 МГц, протоколы Flipper, валидатор частот | ❌ |
| **RFID 125 кГц** | EM4100/HID26 калькулятор, Wiegand, база ключей | ❌ |
| **iButton Manager** | DS1990A CRC8-Dallas, генератор/валидатор ключей | ❌ |
| **GPIO/UART Справочник** | Распиновка Flipper, консоль USB-UART через OTG | ❌ |
| **DuckyScript Editor** | Редактор + валидация BadUSB скриптов | ❌ |
| **BLE Beacon Broadcaster** | iBeacon / Eddystone маяк с телефона | ❌ |
| **WiFi QR + Evil-Twin** | Генерация WiFi QR, проверка двойников | ❌ |
| **Wake-on-LAN + Port Scanner** | Пробуждение ПК + скан портов LAN | ❌ |
| **NFC UID Профили** | HCE-профили эмуляции, random UID | ❌ |
| **QR Multi-Tool** | Текст, URL, WiFi, vCard, пакеты | ❌ |
| **IR Raw Analyzer** | Разбор NEC/RC5/Sony, несущая, повторы | ❌ |
| **Ultrasonic Lab** | Детектор 15-22 кГц через микрофон | ❌ |
| **Sensor Lab** | Аксель, гиро, магнитометр, свет, близость | ❌ |
| **GPS NMEA Logger** | Координаты, спутники, NMEA чексуммы | ❌ |
| **App Permission Auditor** | Аудит опасных разрешений установленных приложений | ❌ |
| **File HEX Inspector** | HEX encode/decode, dump, UTF-8 preview | ❌ |
| **OTG Serial Console** | USB Host API: CH340/CP2102/PN532/CC1101 | ❌ |
| **Root System Tweaker** | wm density/size, getprop, settings | ✅ |
| **Hosts AdBlock** | Блокировка рекламы через /etc/hosts | ✅ |
| **WiFi Password Extractor** | Сохранённые пароли WiFi (Shizuku/ROOT) | ✅ |
| **System App Freezer** | pm disable/enable (Shizuku/ROOT) | ✅ |
| **SELinux + Reboot Menu** | setenforce, reboot recovery/bootloader | ✅ |
| **Logcat + dmesg** | Системные логи, kernel log (ROOT) | ✅ |
| **Netstat + Iptables** | Соединения, firewall правила (ROOT) | ✅ |

---

### ⚙️ **FlippShott Кастомизация** (в Настройках)
- **12 готовых акцентов** + свой **#HEX** цвет
- **AMOLED чёрный** (true black)
- **Экран всегда включён** (FLAG_KEEP_SCREEN_ON)
- **Вибрация/Haptics** вкл/выкл
- **Сетка поверх (Grid)** инженерная
- **Скругление карточек** 8–32 dp слайдер
- **Кнопка сброса** темы в один клик

---

## Сборка

```bash
# Локально
./gradlew assembleRelease

# Или через GitHub Actions (см. .github/workflows/build.yml)
```

Требования: JDK 21, Android SDK 36, Gradle 8.11+

---

## Лицензия

GPL-3.0 — некоторые ресурсы и код основаны на прошивке Flipper Zero (Copyright © Flipper Devices).

---

## Благодарности

- ZalexDev
- ars3nb
- Astrocodee
- Flipper Zero Community