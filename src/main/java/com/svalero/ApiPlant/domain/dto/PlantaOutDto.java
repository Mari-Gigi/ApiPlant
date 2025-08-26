package com.svalero.ApiPlant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlantaOutDto {

    private long id_planta;
    private String genero;
    private String especie;
    private Boolean esToxica;
    private float alturaMaxima;
    private String tipoCrecimiento;
    private long cuidadoId;
    private long categoriaId;
    private List<Long> plagaIds;
    private List<Long> consejoIds;


}
