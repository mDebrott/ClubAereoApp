package com.clubaereo.pasajero_service.util;

public final class RutValidator {

    private RutValidator() {}

    // "12.345.678-5" → "12345678-5" ; "123456785" → "12345678-5"
    public static String normalizar(String rut) {
        String limpio = rut.replace(".", "").replace(" ", "").toUpperCase();
        if (!limpio.contains("-") && limpio.length() > 1)
            limpio = limpio.substring(0, limpio.length() - 1) + "-" + limpio.substring(limpio.length() - 1);
        return limpio;
    }

    public static boolean esValido(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[0-9K]$")) return false;
        String cuerpo = rut.substring(0, rut.length() - 2);
        char dv = rut.charAt(rut.length() - 1);

        int suma = 0, factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }
}