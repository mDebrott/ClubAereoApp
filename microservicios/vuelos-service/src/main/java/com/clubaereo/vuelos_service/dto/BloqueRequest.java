package com.clubaereo.vuelos_service.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record BloqueRequest(
    @NotNull LocalTime horaInicio,
    @NotNull LocalTime horaFin
) {

}
