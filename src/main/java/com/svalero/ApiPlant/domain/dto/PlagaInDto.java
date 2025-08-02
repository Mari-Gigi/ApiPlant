package com.svalero.ApiPlant.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlagaInDto {

    @NotNull(message = "Nombre obligatorio")
    private String nombre;
    private String sintomas;
    @Min(value = 0, message = "Debe estar entre 0 y 10")
    @Max(value = 10, message = "Debe estar entre 0 y 10")
    private Float riesgo;
    @NotNull (message = "Letalidad obligatoria")
    private Boolean esLetal;
    private String tratamiento;
    private List<Long> plantaIds;

}
