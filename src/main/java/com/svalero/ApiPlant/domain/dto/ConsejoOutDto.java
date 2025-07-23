package com.svalero.ApiPlant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ConsejoOutDto {

    private long idConsejo;
    private String titulo;
    private String explicacion;
    private boolean verificado;
    private float importancia;
    private List<Long> plantaIds;

}







