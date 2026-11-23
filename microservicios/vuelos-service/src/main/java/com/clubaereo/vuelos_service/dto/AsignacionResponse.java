package com.clubaereo.vuelos_service.dto;

import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;

public record AsignacionResponse(
    Long id,
    Long pasajeroId,
    Long bloqueId,
    Long aeronaveId,
    Long pilotoId,
    EstadoAsignacion estado
) {
    public static AsignacionResponse from(Asignacion a) {
        return new AsignacionResponse(
            a.getId(), a.getPasajeroId(), a.getBloque().getId(),
            a.getAeronaveId(), a.getPilotoId(), a.getEstado());
    }
}
