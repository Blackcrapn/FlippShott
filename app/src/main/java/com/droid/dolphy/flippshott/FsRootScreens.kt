package com.droid.dolphy.flippshott

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.concurrent.thread

@Composable
private fun FsPrivShell(title: String, sub: String, onBack: () -> Unit, hintCmd: String, preset: List<String>) {
    var cmd by remember { mutableStateOf(preset.firstOrNull() ?: "") }
    var out by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    FsScaffold(title, sub, onBack) {
        FsRootBanner()
        FsCard("Команда (ROOT → Shizuku)") {
            FsField("Shell", cmd, { cmd = it }, hintCmd)
            FsButtons(if (busy) "Выполнение..." else "Выполнить", {
                if (busy) return@FsButtons
                busy = true
                out = "Выполнение: $cmd ..."
                val c = cmd
                thread {
                    val (code, res, via) = FsRootStatus.execPrivileged(c)
                    out = "via=$via code=$code\n$res".take(4000)
                    busy = false
                }
            })
        }
        FsCard("Пресеты") {
            preset.forEach { p ->
                androidx.compose.material3.OutlinedButton(onClick = { cmd = p }) {
                    androidx.compose.material3.Text(p)
                }
            }
        }
        FsCard("Вывод") { FsMonoLog(out) }
    }
}

@Composable fun FsRootTweakerScreen(onBack: () -> Unit) = FsPrivShell(
    "Root System Tweaker", "ROOT/Shizuku", onBack, "wm density",
    listOf("wm density", "wm size", "getprop ro.build.version.release", "settings list system | head -20")
)

@Composable fun FsHostsScreen(onBack: () -> Unit) = FsPrivShell(
    "Hosts AdBlock", "ROOT", onBack, "cat /system/etc/hosts",
    listOf("cat /system/etc/hosts | head -30", "cat /system/etc/hosts | wc -l", "mount | grep system")
)

@Composable fun FsWifiPassScreen(onBack: () -> Unit) = FsPrivShell(
    "WiFi Password Extractor", "ROOT/Shizuku", onBack, "cat /data/misc/wifi/WifiConfigStore.xml",
    listOf(
        "cat /data/misc/wifi/WifiConfigStore.xml | head -100",
        "cat /data/misc/apexdata/com.android.wifi/WifiConfigStore.xml | head -100",
        "dumpsys wifi | grep -i ssid | head -20"
    )
)

@Composable fun FsAppFreezerScreen(onBack: () -> Unit) {
    var pkg by remember { mutableStateOf("com.example.app") }
    var out by remember { mutableStateOf("") }
    FsScaffold("System App Freezer", "Shizuku/ROOT", onBack) {
        FsRootBanner()
        FsCard("pm disable/enable") {
            FsField("Пакет", pkg, { pkg = it }, "com.example.app")
            FsButtons("Заморозить", {
                val p = pkg.trim()
                out = "Выполнение..."
                thread {
                    val (code, res, via) = FsRootStatus.execPrivileged("pm disable-user --user 0 $p")
                    out = "via=$via code=$code\n$res"
                }
            }, "Разморозить", {
                val p = pkg.trim()
                out = "Выполнение..."
                thread {
                    val (code, res, via) = FsRootStatus.execPrivileged("pm enable $p")
                    out = "via=$via code=$code\n$res"
                }
            })
        }
        FsCard("Вывод") { FsMonoLog(out) }
    }
}

@Composable fun FsSelinuxRebootScreen(onBack: () -> Unit) = FsPrivShell(
    "SELinux + Reboot Menu", "ROOT", onBack, "getenforce",
    listOf("getenforce", "setenforce 0", "setenforce 1", "reboot", "reboot recovery", "reboot bootloader")
)

@Composable fun FsLogcatScreen(onBack: () -> Unit) = FsPrivShell(
    "Logcat + dmesg", "ROOT для dmesg", onBack, "logcat -d -t 100",
    listOf("logcat -d -t 100", "logcat -d *:E -t 100", "dmesg | tail -100", "getprop | grep version")
)

@Composable fun FsIptablesScreen(onBack: () -> Unit) = FsPrivShell(
    "Netstat + Iptables", "ROOT", onBack, "cat /proc/net/tcp",
    listOf("cat /proc/net/tcp | head -20", "ip neigh", "iptables -L -n | head -40", "ss -tulpn 2>&1 | head -40")
)
