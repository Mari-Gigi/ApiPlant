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
  /* private Cuidado cuidado; asi devolveria el objeto completo cuidado con todo su desarrollo*/
    private long cuidadoId;
    private long categoriaId;
    private List<PlagaOutDto> plagas;


}
