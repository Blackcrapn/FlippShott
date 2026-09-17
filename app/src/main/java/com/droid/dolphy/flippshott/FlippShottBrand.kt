package com.droid.dolphy.flippshott

/**
 * FlippShott — rebrand of Dolphy.
 * User-visible name is FlippShott everywhere.
 * Internal package stays com.droid.dolphy for build stability,
 * applicationId becomes com.flippshott.app (see app/build.gradle.kts).
 */
object FlippShottBrand {
    const val APP_NAME = "FlippShott"
    const val APPLICATION_ID = "com.flippshott.app"
    const val VERSION_NAME = "3.0"
    const val VERSION_CODE = 30
    const val TAGLINE_RU = "Мультитул для исследования беспроводных протоколов"
    const val TAGLINE_EN = "Wireless protocol research multi-tool"
    const val TELEGRAM = "https://t.me/Dolphy_app_official"
    const val PREFS = "FlippShottPrefs"
}

/**
 * Shared root / Shizuku status helper.
 * Works on both rooted and non-root devices:
 * returns human-readable status, never crashes.
 */
object FsRootStatus {
    data class Status(
        val rooted: Boolean,
        val shizukuRunning: Boolean,
        val shizukuGranted: Boolean,
        val summary: String
    )

    fun probe(): Status {
        val rooted = runCatching {
            com.droid.dolphy.RootUtils.isRooted()
        }.getOrDefault(false)
        var shizukuRunning = false
        var shizukuGranted = false
        try {
            shizukuRunning = rikka.shizuku.Shizuku.pingBinder()
            if (shizukuRunning) {
                shizukuGranted = rikka.shizuku.Shizuku.checkSelfPermission() ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Exception) {
        }
        val summary = buildString {
            append(if (rooted) "ROOT: yes" else "ROOT: no")
            append(" • Shizuku: ")
            append(
                when {
                    !shizukuRunning -> "not running"
                    shizukuGranted -> "granted"
                    else -> "no permission"
                }
            )
        }
        return Status(rooted, shizukuRunning, shizukuGranted, summary)
    }

    /** Execute shell via root if available, else via Shizuku, else fail with message. */
    fun execPrivileged(command: String): Triple<Int, String, String> {
        // 1) root
        try {
            val (code, out) = com.droid.dolphy.RootUtils.executeRootCommand(command)
            if (code == 0 || out.isNotBlank()) return Triple(code, out, "su")
        } catch (_: Exception) {
        }
        // 2) shizuku
        try {
            val json = com.droid.dolphy.ShizukuHelper.runShellCommandWithOutput(command)
            val obj = org.json.JSONObject(json)
            val code = obj.optInt("code", -1)
            val out = obj.optString("out", "") + obj.optString("err", "")
            if (code == 0 || out.isNotBlank()) return Triple(code, out, "shizuku")
            return Triple(code, out.ifBlank { "Shizuku failed" }, "shizuku")
        } catch (e: Exception) {
            return Triple(-1, "No root / Shizuku: ${e.message}", "none")
        }
    }
}
