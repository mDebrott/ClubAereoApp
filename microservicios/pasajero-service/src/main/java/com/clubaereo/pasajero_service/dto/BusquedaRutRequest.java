package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** El RUT va en el cuerpo (no en la URL) para que no quede en logs ni en el historial. */
public record BusquedaRutRequest(
    @NotBlank String rut,
    @NotNull Long jornadaId
) {}
