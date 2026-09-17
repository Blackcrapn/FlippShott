package com.droid.dolphy.flippshott

import android.Manifest
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.util.UUID
import kotlin.concurrent.thread

@Composable
fun FsBleBeaconScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var uuidText by remember { mutableStateOf("12345678-1234-1234-1234-1234567890AB") }
    var major by remember { mutableStateOf("1") }
    var minor by remember { mutableStateOf("1") }
    var out by remember { mutableStateOf("") }
    var advertising by remember { mutableStateOf(false) }
    var callback by remember { mutableStateOf<AdvertiseCallback?>(null) }

    DisposableEffect(Unit) { onDispose { try { callback?.let { cb -> bleAdvertiser(ctx)?.stopAdvertising(cb) } } catch (_: Exception) {} } }

    FsScaffold("BLE Beacon Broadcaster", "iBeacon • без ROOT", onBack) {
        FsRootBanner()
        FsCard("iBeacon маяк") {
            FsField("UUID", uuidText, { uuidText = it })
            FsField("Major", major, { major = it })
            FsField("Minor", minor, { minor = it })
            FsButtons(if (advertising) "Остановить" else "Старт маяка", {
                if (advertising) {
                    try { callback?.let { bleAdvertiser(ctx)?.stopAdvertising(it) } } catch (_: Exception) {}
                    advertising = false
                    out = "Маяк остановлен"
                } else {
                    val res = startIBeacon(ctx, uuidText, major.toIntOrNull() ?: 1, minor.toIntOrNull() ?: 1,
                        onStarted = { advertising = true; callback = it })
                    out = res
                }
            })
        }
        FsCard("Статус") { FsMonoLog(out) }
    }
}

private fun bleAdvertiser(ctx: Context): BluetoothLeAdvertiser? = try {
    val m = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    m.adapter?.bluetoothLeAdvertiser
} catch (_: Exception) { null }

private fun startIBeacon(ctx: Context, uuidStr: String, major: Int, minor: Int, onStarted: (AdvertiseCallback) -> Unit): String {
    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
        return "Нужно разрешение BLUETOOTH_ADVERTISE (выдайте в системе и повторите)"
    }
    val adv = bleAdvertiser(ctx) ?: return "BLE Advertiser недоступен на этом устройстве"
    return try {
        val uuid = UUID.fromString(uuidStr)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits
        val payload = ByteArray(23)
        payload[0] = 0x02
        payload[1] = 0x15.toByte()
        for (i in 0 until 8) payload[2 + i] = ((msb ushr (8 * (7 - i))) and 0xFF).toByte()
        for (i in 0 until 8) payload[10 + i] = ((lsb ushr (8 * (7 - i))) and 0xFF).toByte()
        payload[18] = ((major ushr 8) and 0xFF).toByte()
        payload[19] = (major and 0xFF).toByte()
        payload[20] = ((minor ushr 8) and 0xFF).toByte()
        payload[21] = (minor and 0xFF).toByte()
        payload[22] = 0xC5.toByte()
        val data = AdvertiseData.Builder()
            .addManufacturerData(0x004C, payload)
            .setIncludeDeviceName(false)
            .build()
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
        val cb = object : AdvertiseCallback() {
            override fun onStartSuccess(s: AdvertiseSettings) { onStarted(this) }
            override fun onStartFailure(code: Int) {}
        }
        adv.startAdvertising(settings, data, cb)
        "Маяк запущен: $uuidStr maj=$major min=$minor"
    } catch (e: Exception) {
        "Ошибка: ${e.message}"
    }
}

@Composable
fun FsWifiQrScreen(onBack: () -> Unit) {
    var ssid by remember { mutableStateOf("MyWiFi") }
    var pass by remember { mutableStateOf("12345678") }
    var out by remember { mutableStateOf("") }
    FsScaffold("WiFi QR + Evil-Twin детектор", "Шаринг • без ROOT", onBack) {
        FsRootBanner()
        FsCard("WiFi QR строка") {
            FsField("SSID", ssid, { ssid = it })
            FsField("Пароль", pass, { pass = it })
            FsButtons("Сгенерировать", {
                out = FsLogic.wifiQrString(ssid, pass) + "\n\nОтсканируйте QR в другом Dolphy/FlippShott (QR Multi-Tool) или камерой.\nEvil-Twin check: если рядом 2 сети с одинаковым SSID но разным BSSID/сигналом — возможен двойник. Проверяйте в WiFi Attacks / LAN Scanner."
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsWolPortscanScreen(onBack: () -> Unit) {
    var mac by remember { mutableStateOf("AA:BB:CC:DD:EE:FF") }
    var host by remember { mutableStateOf("192.168.1.1") }
    var out by remember { mutableStateOf("") }
    FsScaffold("Wake-on-LAN + Port Scanner", "LAN • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Wake-on-LAN") {
            FsField("MAC цели", mac, { mac = it }, "AA:BB:CC:DD:EE:FF")
            FsButtons("Отправить Magic Packet", {
                val pkt = FsLogic.wolMagicPacket(mac)
                out = if (pkt == null) "MAC неверный: нужно 12 hex (AA:BB:CC:DD:EE:FF)"
                else {
                    thread {
                        try {
                            DatagramSocket().use { s ->
                                s.broadcast = true
                                s.send(DatagramPacket(pkt, pkt.size, InetAddress.getByName("255.255.255.255"), 9))
                            }
                        } catch (_: Exception) {}
                    }
                    "Magic Packet (${pkt.size} байт) отправлен на ${FsLogic.macNormalize(mac)}:9 (broadcast)"
                }
            })
        }
        FsCard("Быстрый скан портов") {
            FsField("Хост", host, { host = it }, "192.168.1.1")
            FsButtons("Сканировать 21,22,80,443,8080", {
                out = "Сканирование $host ..."
                val h = host.trim()
                thread {
                    val ports = listOf(21, 22, 80, 443, 8080, 8888)
                    val sb = StringBuilder("Хост: $h\n")
                    for (p in ports) {
                        val open = try {
                            Socket().use { s -> s.connect(java.net.InetSocketAddress(h, p), 700); true }
                        } catch (_: Exception) { false }
                        sb.append("Порт $p: ${if (open) "OPEN" else "closed"}\n")
                    }
                    out = sb.toString()
                }
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsNfcProfilesScreen(onBack: () -> Unit) {
    var uid by remember { mutableStateOf("04A3B2C1D580") }
    var out by remember { mutableStateOf("") }
    FsScaffold("NFC UID Профили", "HCE • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Профиль эмуляции") {
            FsField("UID hex", uid, { uid = it })
            FsButtons("Проверить", {
                val b = FsLogic.hexToBytes(uid)
                out = if (b == null) "HEX неверный"
                else "UID bytes: ${b.size} (4/7/10 для Mifare)\nHEX-dump:\n${FsLogic.hexDump(b)}\n\nЭмуляция запускается через NFC Tools → Эмуляция NFC метки (HCE Type 4). Здесь — подготовка профилей."
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsQrMultiScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("https://flippshott.app") }
    var out by remember { mutableStateOf("") }
    FsScaffold("QR Multi-Tool", "Генератор • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Текст / URL / WiFi") {
            FsField("Содержимое", text, { text = it })
            FsButtons("Показать данные", {
                out = "Длина: ${text.length}\nБайт: ${text.toByteArray().size}\nПервые 200 символов:\n${text.take(200)}\n\nГенерация QR: раздел QR Tools (ZXing). Этот модуль готовит пакеты: текст, URL, WiFi QR, vCard."
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
        Text("Сканирование QR: используйте камеру + QR Tools.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
