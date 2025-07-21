package com.svalero.ApiPlant.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class CuidadoInDto {

    @NotNull(message = "Location is required")
    private boolean esInterior;
    @NotNull(message = "Watering frecuency is required")
    private String riego;
    @NotNull(message = " Recommended substrate type is required")
    private String sustrato;
    @Max(value = 100)
    private float humedad;

}

