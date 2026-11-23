package com.clubaereo.pasajero_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.clubaereo.pasajero_service.util.RutValidator;

class RutValidatorTest {

    @Test
    void normalizar_quitaPuntosYAgregaGuion() {
        assertEquals("12345678-5", RutValidator.normalizar("12.345.678-5"));
        assertEquals("12345678-5", RutValidator.normalizar("123456785"));
        assertEquals("11111111-1", RutValidator.normalizar(" 11.111.111-1 "));
    }

    @Test
    void rutValidos() {
        assertTrue(RutValidator.esValido("12345678-5"));
        assertTrue(RutValidator.esValido("11111111-1"));
        assertTrue(RutValidator.esValido("7654321-6"));
    }

    @Test
    void digitoVerificadorK() {
        assertTrue(RutValidator.esValido("10000013-K"));
    }

    @Test
    void rutInvalidos() {
        assertFalse(RutValidator.esValido("12345678-9"));  // DV incorrecto
        assertFalse(RutValidator.esValido("abc"));
        assertFalse(RutValidator.esValido("123-4"));        // muy corto
        assertFalse(RutValidator.esValido(null));
    }
}
