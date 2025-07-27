package com.svalero.ApiPlant.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class CuidadoInDto {

    @NotNull(message = "Ubicación es obligatoria")
    private Boolean esInterior;
    @NotNull(message = "Frecuencia de riego obligatoria")
    private String riego;
    @NotNull(message = "Sustrato obligatorio")
    private String sustrato;
    @Max(value = 100, message = "Debe estar entre 0 y 100")
    private Float humedad;
    private List<Long> plantaIds;

}

