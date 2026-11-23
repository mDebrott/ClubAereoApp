package com.clubaereo.aeronaves_service.dto;

import java.math.BigDecimal;

import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;

public record VueloResponse(
    Long id,
        Long jornadaId,
        Long aeronaveId,
        String matricula,
        Long pilotoId,
        String nombrePiloto,
        BigDecimal horometroInicial,
        BigDecimal horometroFinal,
        BigDecimal horasConsumidas,
        Integer nTripulantes,
        EstadoVuelo estado
) {
    public static VueloResponse from(RegistroVuelo r) {
        return new VueloResponse(
                r.getId(), r.getJornadaId(),
                r.getAeronave().getId(), r.getAeronave().getMatricula(),
                r.getPiloto().getId(), r.getPiloto().getNombre(),
                r.getHorometroInicial(), r.getHorometroFinal(),
                r.getHorasConsumidas(), r.getNTripulantes(), r.getEstado());
    }
}
