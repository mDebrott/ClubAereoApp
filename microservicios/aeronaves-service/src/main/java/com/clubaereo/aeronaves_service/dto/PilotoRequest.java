package com.clubaereo.aeronaves_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PilotoRequest(
    @NotBlank @Size(max = 50) String nombre,
    Boolean licenciaDgacVigente
) {

}
