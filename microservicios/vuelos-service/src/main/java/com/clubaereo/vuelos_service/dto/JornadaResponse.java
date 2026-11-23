package com.clubaereo.vuelos_service.dto;

import java.time.LocalDate;

import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.Jornada;

public record JornadaResponse(Long id, LocalDate fecha, Base base) {

    public static JornadaResponse from(Jornada j) {
        return new JornadaResponse(j.getId(), j.getFecha(), j.getBase());
    }
}
