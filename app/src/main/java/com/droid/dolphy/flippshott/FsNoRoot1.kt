package com.droid.dolphy.flippshott

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun FsSubGhzScreen(onBack: () -> Unit) {
    var freqText by remember { mutableStateOf("433.92") }
    var out by remember { mutableStateOf("") }
    FsScaffold("Sub-GHz Database", "Flipper-диапазоны • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Проверка частоты") {
            FsField("Частота, МГц", freqText, { freqText = it }, "433.92")
            FsButtons("Проверить", {
                val f = freqText.replace(',', '.').toDoubleOrNull()
                out = if (f == null) "Введите число, например 433.92"
                else {
                    val inBand = FsLogic.subghzInBand(f)
                    val near = FsLogic.subghzNearestPreset(f)
                    "Частота: $f МГц\nВ диапазоне Flipper: ${if (inBand) "ДА" else "НЕТ"}\nБлижайший пресет: $near МГц\n" +
                        "Диапазоны: 300-348, 387-464, 779-928 МГц\n" +
                        "Протоколы: Princeton, Nice FLO, CAME, Holtek, KeeLoq (анализ .sub файлов Flipper)"
                }
            })
        }
        FsCard("Пресеты Flipper") {
            Text(FsLogic.SUBGHZ_PRESETS.joinToString("  ") { "$it" }, style = MaterialTheme.typography.bodyMedium)
            Text("На телефоне нет CC1101-передатчика: модуль работает как база/валидатор. Для TX используйте Flipper Zero или CC1101 по OTG (см. OTG Serial Console).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsRfid125Screen(onBack: () -> Unit) {
    var hex by remember { mutableStateOf("4100ABCD12") }
    var out by remember { mutableStateOf("") }
    FsScaffold("RFID 125 кГц", "EM4100 / HID • без ROOT", onBack) {
        FsRootBanner()
        FsCard("EM4100 калькулятор") {
            FsField("ID hex (10 символов)", hex, { hex = it }, "4100ABCD12")
            FsButtons("Декодировать", {
                val e = FsLogic.em4100Parse(hex)
                out = if (e == null) "Нужно 10 hex-символов (0-9 A-F)"
                else "HEX: ${e.hex10}\nCustomer ID: ${e.customerId}\nCard ID: ${e.cardId}\n${FsLogic.em4100ToWiegand26(hex)}\n\nТелефон не читает 125 кГц (нет антенны): используйте внешний PN532/ACR122U по OTG или вносите ID вручную."
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsIButtonScreen(onBack: () -> Unit) {
    var key by remember { mutableStateOf("01000000000000") }
    var out by remember { mutableStateOf("") }
    FsScaffold("iButton Manager", "DS1990A CRC8 • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Проверка ключа (16 hex)") {
            FsField("Ключ iButton", key, { key = it }, "01000000000000+CRC")
            FsButtons("Проверить CRC8", {
                out = if (FsLogic.ibuttonValidate(key)) "CRC8-Dallas: OK — ключ валиден"
                else "CRC8: FAIL — проверьте 16 hex-символов. Пример валидного: 01A2B3C4D5E6F100 (CRC считается автоматически)"
            }, "Сгенерировать демо", {
                val demo = FsLogic.ibuttonBuildId(0x01, byteArrayOf(0xA2.toByte(), 0xB3.toByte(), 0xC4.toByte(), 0xD5.toByte(), 0xE6.toByte(), 0xF1.toByte()))
                key = demo
                out = "Сгенерировано: $demo\nCRC8: OK"
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}

@Composable
fun FsGpioScreen(onBack: () -> Unit) {
    FsScaffold("GPIO / UART Справочник", "Flipper pinout • без ROOT", onBack) {
        FsRootBanner()
        FsCard("Flipper Zero GPIO (2x9)") {
            FsMonoLog("1: 3V3   2: GND\n3: TX    4: RX\n5: C0    6: C1\n7: C2    8: C3\n9: 5V    10: GND\n11: A7    12: A6\n13: A5    14: A4\n15: A1    16: A2\n17: A3    18: GND\n\nUART: 115200 8N1\nSWV/SWD для отладки, I2C/SPI на C0-C3")
        }
        FsCard("USB-UART с телефона") {
            FsMonoLog("1. Подключите CH340/CP2102 по OTG\n2. Откройте OTG Serial Console (FLIPPSHOTT LAB)\n3. Скорость 115200, отправьте 'version'\nРаботает без ROOT через USB Host API.")
        }
    }
}

@Composable
fun FsDuckyEditorScreen(onBack: () -> Unit) {
    var script by remember { mutableStateOf("REM FlippShott demo\nSTRING Hello from FlippShott\nENTER\nDELAY 500\nSTRING whoami\nENTER") }
    var out by remember { mutableStateOf("") }
    FsScaffold("DuckyScript Editor", "BadUSB • без ROOT (через HID)", onBack) {
        FsRootBanner()
        FsCard("Скрипт") {
            FsField("DuckyScript", script, { script = it })
            FsButtons("Валидировать", {
                val lines = script.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val bad = lines.filter { l ->
                    val u = l.uppercase()
                    !(u.startsWith("REM") || u.startsWith("STRING") || u.startsWith("DELAY") || u == "ENTER" || u.startsWith("GUI") || u.startsWith("CTRL") || u.startsWith("ALT") || u.startsWith("SHIFT") || u == "TAB" || u.startsWith("REPEAT"))
                }
                out = "Строк: ${lines.size}\nНеизвестных команд: ${bad.size}\n" + (if (bad.isEmpty()) "Синтаксис OK. Запуск — через HID Keyboard / BadUSB раздел." else "Проверьте:\n" + bad.take(5).joinToString("\n"))
            })
        }
        FsCard("Результат") { FsMonoLog(out) }
    }
}
