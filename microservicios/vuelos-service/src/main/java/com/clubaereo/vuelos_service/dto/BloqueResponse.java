package com.clubaereo.vuelos_service.dto;

import java.time.LocalTime;
import java.util.List;

public record BloqueResponse(
    Long id,
    LocalTime horaInicio,
    LocalTime horaFin,
    List<AsignacionResponse> asignaciones
) {

}
