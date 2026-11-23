package com.clubaereo.aeronaves_service.dto;

import com.clubaereo.aeronaves_service.entities.Aeronave;

public record AeronaveResponse(
    Long id,
    String matricula,
    String modelo,
    Integer capacidad,
    Boolean disponible
) {
    public static AeronaveResponse from(Aeronave a) {
        return new AeronaveResponse(a.getId(), a.getMatricula(), a.getModelo(),
                a.getCapacidad(), a.getDisponible());
    }
}
