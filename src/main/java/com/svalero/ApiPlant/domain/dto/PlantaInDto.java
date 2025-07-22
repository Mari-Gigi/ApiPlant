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

    @NotNull (message = "Genre is required")
    private String genero;
    @NotNull (message = "Species is required")
    private String especie;
    @NotNull (message = "Toxicity is required")
    private Boolean esToxica;
    @Min(value = 0)
    private float alturaMaxima;
    private String tipoCrecimiento;
    private long cuidadoId;
    private long categoriaId;
    private List<Long> plagaIds;

}


