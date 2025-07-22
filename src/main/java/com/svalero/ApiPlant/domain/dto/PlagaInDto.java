package com.svalero.ApiPlant.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlagaInDto {

    @NotNull(message = "Name is required")
    private String nombre;
    private String sintomas;
    @Min(value = 1, message = "Must be between 1 and 10")
    @Max(value = 10, message = "Must be between 1 and 10")
    private float riesgo;
    @NotNull (message = "Letality is required")
    private boolean esLetal;
    private String tratamiento;

}
