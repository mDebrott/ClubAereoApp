package com.clubaereo.aeronaves_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CerrarVueloRequest(
    @NotNull @DecimalMin("0.0") @Digits(integer = 9, fraction = 1) BigDecimal horometroFinal,
    @NotNull @Min(1) Integer nTripulantes
) {

}
