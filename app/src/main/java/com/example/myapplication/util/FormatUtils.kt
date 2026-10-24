package com.example.myapplication.util

import java.text.DecimalFormat
import java.util.Locale

object FormatUtils {
    private val df = DecimalFormat("#,##0.0")
    private val dfInt = DecimalFormat("#,##0")

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        val tb = gb / 1024.0

        return when {
            tb >= 1.0 -> "${df.format(tb)} TB"
            gb >= 1.0 -> "${df.format(gb)} GB"
            mb >= 1.0 -> "${df.format(mb)} MB"
            kb >= 1.0 -> "${df.format(kb)} KB"
            else -> "$bytes B"
        }
    }

    fun formatPercent(percent: Double): String {
        return String.format(Locale.US, "%.1f%%", percent.coerceIn(0.0, 100.0))
    }

    fun formatNumber(num: Long): String {
        return dfInt.format(num)
    }
}
