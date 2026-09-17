package com.droid.dolphy.flippshott

import java.util.Locale

/** Pure logic for FlippShott Lab — works without Android, easy to verify. */
object FsLogic {

    // ---------- Dallas CRC8 (iButton DS1990A) ----------
    fun dallasCrc8(data: ByteArray): Int {
        var crc = 0
        for (b in data) {
            var cur = (b.toInt() and 0xFF) xor crc
            repeat(8) {
                cur = if ((cur and 0x01) != 0) (cur ushr 1) xor 0x8C else (cur ushr 1)
            }
            crc = cur
        }
        return crc and 0xFF
    }

    fun ibuttonBuildId(family: Int, serial6: ByteArray): String {
        require(serial6.size == 6)
        val raw = ByteArray(7)
        raw[0] = family.toByte()
        serial6.copyInto(raw, 1)
        val crc = dallasCrc8(raw)
        val full = raw + crc.toByte()
        return full.joinToString("") { "%02X".format(it) }
    }

    fun ibuttonValidate(hex16: String): Boolean {
        val clean = hex16.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (clean.length != 16) return false
        return try {
            val bytes = ByteArray(8) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
            dallasCrc8(bytes.copyOf(7)) == (bytes[7].toInt() and 0xFF)
        } catch (_: Exception) { false }
    }

    // ---------- EM4100 125kHz ----------
    data class Em4100(val customerId: Int, val cardId: Long, val hex10: String)

    fun em4100Parse(hex10: String): Em4100? {
        val clean = hex10.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (clean.length != 10) return null
        return try {
            val v = clean.toLong(16)
            val customer = ((v ushr 32) and 0xFF).toInt()
            Em4100(customer, v and 0xFFFFFFFFL, clean.uppercase(Locale.US))
        } catch (_: Exception) { null }
    }

    fun em4100ToWiegand26(hex10: String): String? {
        val e = em4100Parse(hex10) ?: return null
        val facility = (e.cardId ushr 16) and 0xFF
        val card = e.cardId and 0xFFFF
        return "FC=$facility Card=$card"
    }

    // ---------- Sub-GHz ----------
    val SUBGHZ_BANDS = listOf(
        300.0 to 348.0, 387.0 to 464.0, 779.0 to 928.0
    )
    val SUBGHZ_PRESETS = listOf(315.0, 318.0, 390.0, 433.42, 433.92, 434.42, 434.775, 438.9, 868.35, 915.0)

    fun subghzInBand(freqMhz: Double): Boolean =
        SUBGHZ_BANDS.any { freqMhz in it.first..it.second }

    fun subghzNearestPreset(freqMhz: Double): Double =
        SUBGHZ_PRESETS.minByOrNull { kotlin.math.abs(it - freqMhz) } ?: freqMhz

    // ---------- HEX ----------
    fun hexDump(bytes: ByteArray, width: Int = 16): String {
        val sb = StringBuilder()
        var off = 0
        while (off < bytes.size) {
            val chunk = bytes.copyOfRange(off, minOf(off + width, bytes.size))
            sb.append("%08X  ".format(off))
            for (i in 0 until width) {
                if (i < chunk.size) sb.append("%02X ".format(chunk[i])) else sb.append("   ")
                if (i == 7) sb.append(" ")
            }
            sb.append(" |")
            for (b in chunk) {
                val c = b.toInt() and 0xFF
                sb.append(if (c in 32..126) c.toChar() else '.')
            }
            sb.append("|\n")
            off += width
        }
        return sb.toString()
    }

    fun hexToBytes(hex: String): ByteArray? {
        val clean = hex.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (clean.isEmpty() || clean.length % 2 != 0) return null
        return try {
            ByteArray(clean.length / 2) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        } catch (_: Exception) { null }
    }

    // ---------- Wake-on-LAN ----------
    fun wolMagicPacket(mac: String): ByteArray? {
        val clean = mac.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (clean.length != 12) return null
        return try {
            val macBytes = ByteArray(6) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
            val pkt = ByteArray(6 + 16 * 6)
            for (i in 0 until 6) pkt[i] = 0xFF.toByte()
            for (rep in 0 until 16) macBytes.copyInto(pkt, 6 + rep * 6)
            pkt
        } catch (_: Exception) { null }
    }

    fun macNormalize(input: String): String? {
        val clean = input.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (clean.length != 12) return null
        return clean.uppercase(Locale.US).chunked(2).joinToString(":")
    }

    // ---------- NMEA ----------
    fun nmeaChecksum(sentence: String): String {
        var cs = 0
        for (c in sentence) cs = cs xor c.code
        return "%02X".format(cs)
    }

    // ---------- WiFi QR ----------
    fun wifiQrString(ssid: String, password: String, auth: String = "WPA"): String {
        fun esc(s: String) = s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace(":", "\\:")
        return "WIFI:T:$auth;S:${esc(ssid)};P:${esc(password)};;"
    }
}
