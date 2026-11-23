package com.clubaereo.pasajero_service.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.clubaereo.pasajero_service.entities.EstadoConsentimiento;
import com.clubaereo.pasajero_service.entities.Pasajero;

public record PasajeroResponse(
    Long id,
    String rut,
    String nombreCompleto,
    Long jornadaId,
    BigDecimal pesoKg,
    EstadoConsentimiento consentimiento,
    LocalDateTime fechaFirma
) {
    public static PasajeroResponse from(Pasajero p) {
        return new PasajeroResponse(
            p.getId(), p.getRut(), p.getNombreCompleto(),
            p.getJornadaId(), p.getPesoKg(), p.getConsentimiento(), p.getFechaFirma()
        );
    }
}