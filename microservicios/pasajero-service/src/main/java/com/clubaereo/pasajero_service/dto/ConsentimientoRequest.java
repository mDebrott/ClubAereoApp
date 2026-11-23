package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsentimientoRequest(
    @NotBlank @Size(max = 500_000, message = "La firma es demasiado grande") String firmaBase64
) {}
