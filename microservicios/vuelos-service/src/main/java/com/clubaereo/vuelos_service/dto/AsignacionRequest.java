package com.clubaereo.vuelos_service.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequest(
    @NotNull (message = "El pasajero es obligatorio") Long pasajeroId,
    @NotNull(message = "El bloque es obligatorio")   Long bloqueId,
    @NotNull(message = "La aeronave es obligatoria") Long aeronaveId,
    @NotNull(message = "El piloto es obligatorio")   Long pilotoId
) {

}
