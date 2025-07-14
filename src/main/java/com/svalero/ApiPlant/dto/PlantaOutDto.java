package com.svalero.ApiPlant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlantaOutDto {

    private long id_planta;
    private String nombre;
    private String imagenPlanta;
    private boolean esToxica;
    private float alturaMaxima;
    private String imagen;
    private long cuidadoId;
    private long plagaId;



}
