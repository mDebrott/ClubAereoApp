package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PasajeroRequest(
    @NotBlank @Size(max = 14) String rut,
    @NotBlank @Size(max = 255) String nombreCompleto,
    @NotNull Long jornadaId
) {}
