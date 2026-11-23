package com.example.clubaereoapp

import com.example.clubaereoapp.data.util.RutValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutValidatorTest {

    @Test
    fun testValidRutFormats() {
        assertTrue(RutValidator.isValidRut("11.111.111-1"))
        assertTrue(RutValidator.isValidRut("111111111"))
    }

    @Test
    fun testInvalidRutFormats() {
        assertFalse(RutValidator.isValidRut("123"))
        assertFalse(RutValidator.isValidRut(""))
        assertFalse(RutValidator.isValidRut("abc-d"))
    }
}
