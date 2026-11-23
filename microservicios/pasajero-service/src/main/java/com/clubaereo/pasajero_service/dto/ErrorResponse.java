package com.clubaereo.pasajero_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Mismo formato de error que los otros microservicios. */
public record ErrorResponse(
    int status,
    String error,
    String mensaje,
    Map<String, String> campos,
    LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(status, error, mensaje, null, LocalDateTime.now());
    }

    public static ErrorResponse conCampos(int status, String error, String mensaje,
                                          Map<String, String> campos) {
        return new ErrorResponse(status, error, mensaje, campos, LocalDateTime.now());
    }
}
