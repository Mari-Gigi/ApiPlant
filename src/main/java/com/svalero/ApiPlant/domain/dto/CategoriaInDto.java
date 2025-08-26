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

public class CategoriaInDto {

    @NotNull(message = "El nombre es obligatorio")
    private String nombre;
    @NotNull(message = "Descripción obligatoria")
    private String descripcion;
    @Min(value = 0, message = "Debe estar entre 0 y 10")
    @Max(value = 10, message = "Debe estar entre 0 y 10")
    private Float nivelDificultad;
    private Boolean paraPrincipiantes;
    private List<Long> plantaIds;

}
