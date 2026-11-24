package com.pamn.gamematch.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    private val minYear = 1906
    private val maxYear = 2018

    @Test
    fun testIsBirthYearValid_strictValidation() {
        // Casos válidos de 4 dígitos dentro del rango
        assertTrue(ValidationUtils.isBirthYearValid("1906", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearValid("1995", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearValid("2018", minYear, maxYear))

        // Prefijos no deben ser válidos en la validación estricta
        assertFalse(ValidationUtils.isBirthYearValid("19", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearValid("199", minYear, maxYear))

        // Casos fuera de rango (límite min-1, max+1)
        assertFalse(ValidationUtils.isBirthYearValid("1905", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearValid("2019", minYear, maxYear))

        // Inválidos: vacíos, no numéricos, más de 4 dígitos
        assertFalse(ValidationUtils.isBirthYearValid("", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearValid("   ", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearValid("abcd", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearValid("19950", minYear, maxYear))
    }

    @Test
    fun testIsBirthYearPrefixValid_prefixValidation() {
        // Vacío es válido para la UI antes de tocar el campo
        assertTrue(ValidationUtils.isBirthYearPrefixValid("", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("   ", minYear, maxYear))

        // Prefijos válidos
        assertTrue(ValidationUtils.isBirthYearPrefixValid("1", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("19", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("199", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("20", minYear, maxYear))

        // Años de 4 dígitos dentro de rango
        assertTrue(ValidationUtils.isBirthYearPrefixValid("1906", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("1995", minYear, maxYear))
        assertTrue(ValidationUtils.isBirthYearPrefixValid("2018", minYear, maxYear))

        // Prefijos inválidos
        assertFalse(ValidationUtils.isBirthYearPrefixValid("3", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearPrefixValid("18", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearPrefixValid("21", minYear, maxYear))

        // Años de 4 dígitos fuera de rango o no numéricos
        assertFalse(ValidationUtils.isBirthYearPrefixValid("1905", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearPrefixValid("2019", minYear, maxYear))
        assertFalse(ValidationUtils.isBirthYearPrefixValid("abc", minYear, maxYear))
    }

    @Test
    fun testIsValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("Gamer123"))
        assertFalse(ValidationUtils.isValidUsername(""))
        assertFalse(ValidationUtils.isValidUsername("   "))
    }

    @Test
    fun testCalculatePasswordStrength() {
        assertEquals(0f, ValidationUtils.calculatePasswordStrength(""), 0.01f)
        assertEquals(0.35f, ValidationUtils.calculatePasswordStrength("abcdef"), 0.01f)
        assertEquals(0.80f, ValidationUtils.calculatePasswordStrength("abcdef12"), 0.01f)
        assertEquals(1.0f, ValidationUtils.calculatePasswordStrength("Abcdef12!"), 0.01f)
    }
}
