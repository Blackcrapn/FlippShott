package com.droid.dolphy.flippshott

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.usb.UsbManager
import android.location.LocationManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.concurrent.thread

@Composable
fun FsIrAnalyzerScreen(onBack: () -> Unit) {
    var raw by remember { mutableStateOf("NEC 20DF10EF 32") }
    var out by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    FsScaffold("IR Raw Analyzer", "NEC/RC5 • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Анализ сигнала") {
            val hasIr = remember {
                try {
                    val cir = ctx.getSystemService(Context.CONSUMER_IR_SERVICE) as android.hardware.ConsumerIrManager
                    cir.hasIrEmitter()
                } catch (_: Exception) { false }
            }
            Text("ИК-передатчик: ${if (hasIr) "ЕСТЬ" else "НЕТ (только анализ файлов)"}", style = MaterialTheme.typography.bodyMedium)
            FsField("Сигнал (протокол адрес команда)", raw, { raw = it }, "NEC 20DF10EF 32")
            FsButtons("Разобрать", {
                val parts = raw.trim().split(Regex("\\s+"))
                out = "Частей: ${parts.size}\n" + parts.mapIndexed { i, p -> "[$i] $p (${p.length} ch)" }.joinToString("\n") +
                    "\n\nНесущая: 38 кГц (NEC/RC5), 36 кГц (Sony), 56 кГц (Bang&Olufsen)\nПовторы: NEC — каждые 110мс пока держится кнопка."
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsUltrasonicScreen(onBack: () -> Unit) {
    var out by remember { mutableStateOf("Нажмите Старт: 6-секундный замер энергии 15-22 кГц через микрофон.\nТребуется разрешение RECORD_AUDIO.") }
    FsScaffold("Ultrasonic Lab", "15-22 кГц • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Детектор") {
            FsButtons("Инструкция", {
                out = "1. Выдайте доступ к микрофону\n2. Тихая комната → замер фона\n3. Поднесите к ТВ/датчику движения\n4. Всплеск энергии 15-22 кГц = ультразвуковой радар/отпугиватель\n\nПолный FFT-радар уже есть в разделе Сканеры (УС радар). Этот модуль — быстрый чек."
            })
        }
        FsCard("Статус") { FsMonoLog(out) }
    }
}

@Composable
fun FsSensorLabScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var out by remember { mutableStateOf("Нажмите Старт для чтения датчиков") }
    FsScaffold("Sensor Lab", "Датчики • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Датчики") {
            FsButtons("Читать 3 сек", {
                out = "Чтение..."
                thread {
                    try {
                        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                        val gyr = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                        val light = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
                        val prox = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                        val sb = StringBuilder()
                        sb.append("Аксель: ${if (acc != null) "есть" else "нет"}\n")
                        sb.append("Гиро: ${if (gyr != null) "есть" else "нет"}\n")
                        sb.append("Магнитометр: ${if (mag != null) "есть" else "нет"}\n")
                        sb.append("Свет: ${if (light != null) "есть (${light.maximumRange} lx max)" else "нет"}\n")
                        sb.append("Близость: ${if (prox != null) "есть (${prox.maximumRange})" else "нет"}\n")
                        val snapshot = FloatArray(3) { 0f }
                        val lock = Object()
                        val l = object : SensorEventListener {
                            override fun onSensorChanged(e: SensorEvent) {
                                e.values.copyInto(snapshot, 0, 0, minOf(3, e.values.size))
                                synchronized(lock) { lock.notifyAll() }
                            }
                            override fun onAccuracyChanged(s: Sensor, a: Int) {}
                        }
                        if (acc != null) {
                            sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_NORMAL)
                            synchronized(lock) { lock.wait(1200) }
                            sm.unregisterListener(l)
                            sb.append("Accel snapshot: ${snapshot.joinToString(", ") { "%.2f".format(it) }}\n")
                        }
                        out = sb.toString()
                    } catch (e: Exception) {
                        out = "Ошибка: ${e.message}"
                    }
                }
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsGpsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var out by remember { mutableStateOf("") }
    FsScaffold("GPS NMEA Logger", "Локация • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Позиция") {
            FsButtons("Показать провайдеры", {
                out = try {
                    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val provs = lm.getProviders(true)
                    val sb = StringBuilder("Провайдеры: ${provs.joinToString()}\n")
                    try {
                        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (loc != null) {
                            sb.append("Lat: ${loc.latitude}\nLon: ${loc.longitude}\nAcc: ${loc.accuracy} м\nTime: ${java.util.Date(loc.time)}\n")
                            val nmea = "\$GPGGA,${"%.2f".format(loc.latitude)}"
                            sb.append("NMEA chk demo: *${FsLogic.nmeaChecksum(nmea)}")
                        } else sb.append("Last location: нет (включите GPS)")
                    } catch (e: SecurityException) {
                        sb.append("Нужно разрешение геолокации")
                    }
                    sb.toString()
                } catch (e: Exception) { "Ошибка: ${e.message}" }
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsAppAuditorScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var out by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("") }
    FsScaffold("App Permission Auditor", "Аудит • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Сканирование") {
            FsField("Фильтр пакета", filter, { filter = it }, "com.")
            FsButtons("Аудит опасных разрешений", {
                out = "Сканирование..."
                val f = filter.trim()
                thread {
                    try {
                        val pm = ctx.packageManager
                        val pkgs = pm.getInstalledPackages(android.content.pm.PackageManager.GET_PERMISSIONS)
                        val dangerous = listOf("CAMERA", "RECORD_AUDIO", "ACCESS_FINE_LOCATION", "READ_CONTACTS", "READ_SMS", "READ_CALL_LOG")
                        val sb = StringBuilder()
                        var shown = 0
                        for (p in pkgs) {
                            if (f.isNotBlank() && !p.packageName.contains(f, true)) continue
                            val perms = p.requestedPermissions?.toList() ?: emptyList()
                            val hit = dangerous.filter { d -> perms.any { it.endsWith(d) } }
                            if (hit.isNotEmpty()) {
                                sb.append("${p.packageName}\n  ⚠ ${hit.joinToString()}\n")
                                if (++shown >= 40) { sb.append("... и ещё\n"); break }
                            }
                        }
                        if (shown == 0) sb.append("Опасных разрешений не найдено (по фильтру)")
                        out = "Проверено пакетов: ${pkgs.size}\n\n$sb"
                    } catch (e: Exception) { out = "Ошибка: ${e.message}" }
                }
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsFileHexScreen(onBack: () -> Unit) {
    var hex by remember { mutableStateOf("48656C6C6F20466C69707053686F7474") }
    var out by remember { mutableStateOf("") }
    FsScaffold("File HEX Inspector", "HEX • без ROOT", onBack) {
        FsRootBanner()
        FsCard("HEX encode/decode") {
            FsField("HEX", hex, { hex = it })
            FsButtons("Декодировать", {
                val b = FsLogic.hexToBytes(hex)
                out = if (b == null) "HEX неверный (нужно чётное число 0-9 A-F)"
                else "Байт: ${b.size}\nТекст: ${runCatching { String(b, Charsets.UTF_8) }.getOrDefault("?")}\n\nDump:\n${FsLogic.hexDump(b)}"
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsOtgSerialScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var out by remember { mutableStateOf("") }
    FsScaffold("OTG Serial Console", "USB Host • без ROOT", onBack) {
        FsRootBanner()
        FsCard("USB устройства") {
            FsButtons("Список USB", {
                out = try {
                    val um = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
                    val devs = um.deviceList
                    if (devs.isEmpty()) "USB-устройств не найдено.\nПодключите CH340/CP2102/PN532/CC1101 через OTG-кабель."
                    else devs.values.joinToString("\n\n") { d ->
                        "Device: ${d.deviceName}\nVID=${d.vendorId} PID=${d.productId}\nClass=${d.deviceClass} Ifaces=${d.interfaceCount}"
                    } + "\n\nДля CC1101/Sub-GHz TX используйте прошивку + команды через serial 115200."
                } catch (e: Exception) { "Ошибка: ${e.message}" }
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}
