package com.clubaereo.aeronaves_service.dto;

import java.math.BigDecimal;

public record EstadisticasVueloDTO(
    Long totalVuelos,
    Long pilotosDistintos,
    Long aeronavesDistintas,
    Long totalTripulantes,
    BigDecimal horasTotales
) {
    
}
