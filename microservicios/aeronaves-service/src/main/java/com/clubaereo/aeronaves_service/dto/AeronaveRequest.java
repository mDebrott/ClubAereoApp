package com.clubaereo.aeronaves_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AeronaveRequest(
    @NotBlank @Size(max = 10) String matricula,
    @NotBlank @Size(max = 50) String modelo,
    @NotNull @Min(1) @Max(10) Integer capacidad
) {

}
