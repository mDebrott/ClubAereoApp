package com.clubaereo.pasajero_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record PesoRequest(
    @NotNull @DecimalMin("20.0") @DecimalMax("250.0") @Digits(integer = 3, fraction = 1) BigDecimal pesoKg
) {}
