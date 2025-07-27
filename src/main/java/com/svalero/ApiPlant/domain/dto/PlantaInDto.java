package com.svalero.ApiPlant.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlantaInDto {

    @NotNull (message = "El género es obligatorio")
    private String genero;
    @NotNull (message = "La especie es obligatoria")
    private String especie;
    @NotNull (message = "La toxicidad es obligatoria")
    private Boolean esToxica;
    @Min(value = 0, message = "Debe ser mayor que 0.")
    private float alturaMaxima;
    private String tipoCrecimiento;
    @NotNull(message = "cuidadoId es obligatorio")
    private long cuidadoId;
    @NotNull(message = "categoriaId es obligatorio")
    private long categoriaId;
    private List<Long> plagaIds;
    private List<Long> consejoIds;

}


