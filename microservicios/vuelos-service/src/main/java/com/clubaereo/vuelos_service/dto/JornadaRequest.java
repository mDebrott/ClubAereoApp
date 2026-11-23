package com.clubaereo.vuelos_service.dto;

import java.time.LocalDate;

import com.clubaereo.vuelos_service.entities.Base;

import jakarta.validation.constraints.NotNull;

public record JornadaRequest(
    @NotNull LocalDate fecha,
    @NotNull Base base
) {

}
