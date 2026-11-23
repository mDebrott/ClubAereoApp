package com.example.clubaereoapp.data.util

object RutValidator {
    fun isValidRut(rut: String): Boolean {
        val clean = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (clean.length < 8) return false
        val dv = clean.last()
        val numStr = clean.substring(0, clean.length - 1)
        val num = numStr.toIntOrNull() ?: return false

        var m = 2
        var sum = 0
        for (i in numStr.length - 1 downTo 0) {
            sum += (numStr[i] - '0') * m
            m = if (m == 7) 2 else m + 1
        }
        val res = 11 - (sum % 11)
        val expectedDv = when (res) {
            11 -> '0'
            10 -> 'K'
            else -> res.toString()[0]
        }
        return dv == expectedDv
    }
}
