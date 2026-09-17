package com.droid.dolphy.flippshott

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

data class FsFeature(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val requiresRoot: Boolean = false,
    val noRootWorks: Boolean = true
) {
    val route: String get() = "other/flippshott/$id"
    val routeTop: String get() = "flippshott/$id"
}

object FlippShottCatalog {
    const val SECTION = "FLIPPSHOTT LAB"

    val features: List<FsFeature> = listOf(
        FsFeature("subghz_db", "Sub-GHz Database", "Частоты 300-928 МГц, протоколы Flipper, валидатор", Icons.Default.Radio),
        FsFeature("rfid125_tool", "RFID 125 кГц", "EM4100 / HID26 калькулятор, Wiegand, база", Icons.Default.Nfc),
        FsFeature("ibutton_manager", "iButton Manager", "DS1990A CRC8-Dallas, база ключей", Icons.Default.Memory),
        FsFeature("gpio_reference", "GPIO / UART Справочник", "Распиновка Flipper, USB-UART консоль", Icons.Default.Build),
        FsFeature("ducky_editor", "DuckyScript Editor", "Редактор + запуск BadUSB скриптов", Icons.Default.Keyboard),
        FsFeature("ble_beacon", "BLE Beacon Broadcaster", "iBeacon / Eddystone маяк с телефона", Icons.Default.Bluetooth),
        FsFeature("wifi_qr_evil", "WiFi QR + Evil-Twin детектор", "Шаринг сети + проверка двойников", Icons.Default.Wifi),
        FsFeature("wol_portscan", "Wake-on-LAN + Port Scanner", "Пробуждение ПК + скан портов LAN", Icons.Default.Computer),
        FsFeature("nfc_uid_profiles", "NFC UID Профили", "HCE-профили эмуляции, random UID", Icons.Default.Nfc),
        FsFeature("qr_multi", "QR Multi-Tool", "Генератор QR / штрих-кодов, пакеты", Icons.Default.QrCode),
        FsFeature("ir_analyzer", "IR Raw Analyzer", "Анализ .ir файлов, повторы, несущая", Icons.Default.Sensors),
        FsFeature("ultrasonic_lab", "Ultrasonic Lab", "Детектор 15-22 кГц через микрофон", Icons.Default.Audiotrack),
        FsFeature("sensor_lab", "Sensor Lab", "Акселерометр, гироскоп, магнитометр, свет", Icons.Default.Sensors),
        FsFeature("gps_nmea", "GPS NMEA Logger", "Координаты, спутники, лог NMEA", Icons.Default.GpsFixed),
        FsFeature("app_auditor", "App Permission Auditor", "Аудит разрешений приложений", Icons.Default.Security),
        FsFeature("file_hex", "File HEX Inspector", "HEX-просмотр, encode/decode", Icons.Default.Storage),
        FsFeature("otg_serial", "OTG Serial Console", "USB-устройства, CC1101 через OTG", Icons.Default.Usb),
        FsFeature("root_tweaker", "Root System Tweaker", "DPI, build.prop, системные твики (ROOT)", Icons.Default.Settings, requiresRoot = true, noRootWorks = false),
        FsFeature("hosts_adblock", "Hosts AdBlock", "Блокировка рекламы через /etc/hosts (ROOT)", Icons.Default.Security, requiresRoot = true, noRootWorks = false),
        FsFeature("wifi_pass_extractor", "WiFi Password Extractor", "Сохранённые пароли WiFi (ROOT/Shizuku)", Icons.Default.Wifi, requiresRoot = true, noRootWorks = false),
        FsFeature("app_freezer", "System App Freezer", "Заморозка системных apps (Shizuku/ROOT)", Icons.Default.Build, requiresRoot = true, noRootWorks = false),
        FsFeature("selinux_reboot", "SELinux + Reboot Menu", "setenforce, reboot recovery/bootloader (ROOT)", Icons.Default.Terminal, requiresRoot = true, noRootWorks = false),
        FsFeature("logcat_kernel", "Logcat + dmesg", "Логи ядра и системы (ROOT для dmesg)", Icons.Default.BugReport, requiresRoot = true, noRootWorks = false),
        FsFeature("iptables_netstat", "Netstat + Iptables", "Соединения, правила firewall (ROOT)", Icons.Default.Router, requiresRoot = true, noRootWorks = false),
    )

    fun byId(id: String): FsFeature? = features.firstOrNull { it.id == id }
}
