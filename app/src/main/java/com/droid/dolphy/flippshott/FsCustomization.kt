package com.droid.dolphy.flippshott

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droid.dolphy.SpamViewModel

object FsThemePrefs {
    const val PREFS = "FlippShottPrefs"
    const val KEY_AMOLED = "amoled_black"
    const val KEY_KEEP_ON = "keep_screen_on"
    const val KEY_HAPTICS = "haptics_enabled"
    const val KEY_CORNER = "corner_radius"
    const val KEY_GRID = "grid_overlay"
    const val KEY_BG_ALPHA = "bg_alpha"

    fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isAmoled(ctx: Context) = prefs(ctx).getBoolean(KEY_AMOLED, false)
    fun isKeepOn(ctx: Context) = prefs(ctx).getBoolean(KEY_KEEP_ON, false)
    fun isHaptics(ctx: Context) = prefs(ctx).getBoolean(KEY_HAPTICS, true)
    fun corner(ctx: Context) = prefs(ctx).getFloat(KEY_CORNER, 16f)
    fun isGrid(ctx: Context) = prefs(ctx).getBoolean(KEY_GRID, false)
}

val FsExtraAccents = listOf(
    "FlippShott" to Color(0xFFFF7A1A),
    "Red" to Color(0xFFFF5252),
    "Green" to Color(0xFF69F0AE),
    "Purple" to Color(0xFFE040FB),
    "Cyan" to Color(0xFF18FFFF),
    "Yellow" to Color(0xFFFFD600),
    "Pink" to Color(0xFFFF4081),
    "Blue" to Color(0xFF448AFF),
    "Lime" to Color(0xFFB2FF59),
    "Orange Deep" to Color(0xFFFF6D00),
    "Teal" to Color(0xFF64FFDA),
    "White" to Color(0xFFE0E0E0),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlippShottCustomizationSection(spamViewModel: SpamViewModel) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var amoled by remember { mutableStateOf(FsThemePrefs.isAmoled(ctx)) }
    var keepOn by remember { mutableStateOf(FsThemePrefs.isKeepOn(ctx)) }
    var hapticsOn by remember { mutableStateOf(FsThemePrefs.isHaptics(ctx)) }
    var grid by remember { mutableStateOf(FsThemePrefs.isGrid(ctx)) }
    var corner by remember { mutableFloatStateOf(FsThemePrefs.corner(ctx)) }
    var customHex by remember { mutableStateOf("") }
    var hexMsg by remember { mutableStateOf("") }

    LaunchedEffect(keepOn) {
        try {
            val act = ctx as? Activity
            if (keepOn) act?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else act?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (_: Exception) {}
    }

    fun save() {
        FsThemePrefs.prefs(ctx).edit()
            .putBoolean(FsThemePrefs.KEY_AMOLED, amoled)
            .putBoolean(FsThemePrefs.KEY_KEEP_ON, keepOn)
            .putBoolean(FsThemePrefs.KEY_HAPTICS, hapticsOn)
            .putBoolean(FsThemePrefs.KEY_GRID, grid)
            .putFloat(FsThemePrefs.KEY_CORNER, corner)
            .apply()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("FLIPPSHOTT КАСТОМИЗАЦИЯ", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("12 акцентов + свой HEX, AMOLED, скругление, сетка, вибрация, экран всегда вкл.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Text("Акцент (расширенный)", fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FsExtraAccents.forEach { (name, color) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                spamViewModel.setAccentColor(color)
                                if (hapticsOn) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // selected tick is drawn by caller via current accent? keep simple
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    label = { Text("#RRGGBB") },
                    placeholder = { Text("#00FFAA") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val clean = customHex.trim().removePrefix("#")
                    val parsed = runCatching { android.graphics.Color.parseColor("#$clean") }.getOrNull()
                    if (parsed == null) hexMsg = "HEX неверный"
                    else {
                        spamViewModel.setAccentColor(Color(parsed))
                        hexMsg = "Применён #$clean"
                    }
                }) { Text("OK") }
            }
            if (hexMsg.isNotBlank()) Text(hexMsg, style = MaterialTheme.typography.bodySmall)

            FsSwitchRow("AMOLED чёрный", "Чистый чёрный фон (применится сразу в FlippShott, полностью — после рестарта)", amoled) { amoled = it; save() }
            FsSwitchRow("Экран всегда включён", "FLAG_KEEP_SCREEN_ON для приложения", keepOn) { keepOn = it; save() }
            FsSwitchRow("Вибрация / Haptics", "Отклик при нажатиях во FlippShott", hapticsOn) { hapticsOn = it; save() }
            FsSwitchRow("Сетка поверх (Grid)", "Инженерная сетка в экранах FlippShott", grid) { grid = it; save() }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Скругление карточек", modifier = Modifier.weight(1f))
                Text("${corner.toInt()}dp", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(value = corner, onValueChange = { corner = it; save() }, valueRange = 8f..32f, steps = 11)

            OutlinedButton(onClick = {
                amoled = false; keepOn = false; hapticsOn = true; grid = false; corner = 16f
                save()
                spamViewModel.setAccentColor(Color(0xFFFF7A1A))
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("Сбросить FlippShott тему")
            }
        }
    }
}

@Composable
private fun FsSwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
