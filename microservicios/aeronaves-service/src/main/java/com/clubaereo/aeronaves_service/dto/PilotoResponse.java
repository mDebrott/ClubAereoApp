package com.clubaereo.aeronaves_service.dto;

import com.clubaereo.aeronaves_service.entities.Piloto;

public record PilotoResponse(
    Long id,
    String nombre,
    Boolean licenciaDgacVigente
) {
    public static PilotoResponse from(Piloto p) {
        return new PilotoResponse(p.getId(), p.getNombre(), p.getLicenciaDgacVigente());
    }
}
