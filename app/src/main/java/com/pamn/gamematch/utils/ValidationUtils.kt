package com.pamn.gamematch.utils

object ValidationUtils {
    /**
     * Validación estricta para guardar datos: exige que el año no esté vacío,
     * tenga formato numérico de 4 dígitos y se encuentre dentro del rango [minValidYear, maxValidYear].
     */
    fun isBirthYearValid(birthYear: String, minValidYear: Int, maxValidYear: Int): Boolean {
        val trimmed = birthYear.trim()
        if (trimmed.length != 4) return false
        val yearNum = trimmed.toIntOrNull() ?: return false
        return yearNum in minValidYear..maxValidYear
    }

    /**
     * Validación de prefijo para el feedback visual en pantalla mientras el usuario escribe:
     * Si está vacío es válido (true). Si tiene menos de 4 dígitos, valida si coincide con el prefijo de min/max.
     * Si tiene 4 o más dígitos, valida el rango completo.
     */
    fun isBirthYearPrefixValid(birthYear: String, minValidYear: Int, maxValidYear: Int): Boolean {
        val trimmed = birthYear.trim()
        if (trimmed.isEmpty()) return true
        val yearNum = trimmed.toIntOrNull() ?: return false
        return if (trimmed.length < 4) {
            val minPrefix = minValidYear.toString().take(trimmed.length).toIntOrNull() ?: return false
            val maxPrefix = maxValidYear.toString().take(trimmed.length).toIntOrNull() ?: return false
            yearNum in minPrefix..maxPrefix
        } else {
            yearNum in minValidYear..maxValidYear
        }
    }

    fun isValidUsername(username: String): Boolean {
        return username.trim().isNotBlank()
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return false
        val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return regex.matches(trimmed)
    }

    fun calculatePasswordStrength(pass: String): Float {
        if (pass.isEmpty()) return 0f
        var score = 0f
        if (pass.length >= 6) score += 0.35f
        if (pass.length >= 8) score += 0.25f
        if (pass.any { it.isDigit() }) score += 0.2f
        if (pass.any { !it.isLetterOrDigit() }) score += 0.2f
        return score.coerceAtMost(1f)
    }
}
