package com.clubaereo.vuelos_service.client;

import java.math.BigDecimal;

/** Lo que vuelos-service necesita saber de pasajero-service. */
public record PasajeroExterno(Long id, Long jornadaId, BigDecimal pesoKg, String consentimiento) {

    public boolean consentimientoFirmado() {
        return "FIRMADO".equals(consentimiento);
    }
}
