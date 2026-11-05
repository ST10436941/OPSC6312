package com.vetqueue.app.util

object InputFormatting {

    /** "20260825" -> "2026-08-25". Accepts and ignores any non-digit characters typed in between. */
    fun formatDate(raw: String): String {
        val digits = raw.filter { it.isDigit() }.take(8) // YYYYMMDD
        val sb = StringBuilder()
        digits.forEachIndexed { i, c ->
            if (i == 4 || i == 6) sb.append('-')
            sb.append(c)
        }
        return sb.toString()
    }

    /** "1030" -> "10:30". Accepts and ignores any non-digit characters typed in between. */
    fun formatTime(raw: String): String {
        val digits = raw.filter { it.isDigit() }.take(4) // HHMM
        val sb = StringBuilder()
        digits.forEachIndexed { i, c ->
            if (i == 2) sb.append(':')
            sb.append(c)
        }
        return sb.toString()
    }
}