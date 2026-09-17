package com.droid.dolphy.flippshott

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FsFeatureHost(featureId: String, onBack: () -> Unit) {
    when (featureId) {
        "subghz_db" -> FsSubGhzScreen(onBack)
        "rfid125_tool" -> FsRfid125Screen(onBack)
        "ibutton_manager" -> FsIButtonScreen(onBack)
        "gpio_reference" -> FsGpioScreen(onBack)
        "ducky_editor" -> FsDuckyEditorScreen(onBack)
        "ble_beacon" -> FsBleBeaconScreen(onBack)
        "wifi_qr_evil" -> FsWifiQrScreen(onBack)
        "wol_portscan" -> FsWolPortscanScreen(onBack)
        "nfc_uid_profiles" -> FsNfcProfilesScreen(onBack)
        "qr_multi" -> FsQrMultiScreen(onBack)
        "ir_analyzer" -> FsIrAnalyzerScreen(onBack)
        "ultrasonic_lab" -> FsUltrasonicScreen(onBack)
        "sensor_lab" -> FsSensorLabScreen(onBack)
        "gps_nmea" -> FsGpsScreen(onBack)
        "app_auditor" -> FsAppAuditorScreen(onBack)
        "file_hex" -> FsFileHexScreen(onBack)
        "otg_serial" -> FsOtgSerialScreen(onBack)
        "root_tweaker" -> FsRootTweakerScreen(onBack)
        "hosts_adblock" -> FsHostsScreen(onBack)
        "wifi_pass_extractor" -> FsWifiPassScreen(onBack)
        "app_freezer" -> FsAppFreezerScreen(onBack)
        "selinux_reboot" -> FsSelinuxRebootScreen(onBack)
        "logcat_kernel" -> FsLogcatScreen(onBack)
        "iptables_netstat" -> FsIptablesScreen(onBack)
        else -> FsScaffold("FlippShott", "Не найдено", onBack) {
            Text("Функция $featureId не найдена", color = MaterialTheme.colorScheme.error)
        }
    }
}
