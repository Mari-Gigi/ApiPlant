package com.svalero.ApiPlant.domain.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


public class CategoriaInDto {

    @NotNull(message = "Nombre is required")
    private String nombre;
    @NotNull(message = "Description is required")
    private String descripcion;
    @Min(value = 1, message = "Must be between 1 and 10")
    @Max(value = 10, message = "Must be between 1 and 10")
    private float nivelDificultad;
    private boolean paraPincipiantes;


}
