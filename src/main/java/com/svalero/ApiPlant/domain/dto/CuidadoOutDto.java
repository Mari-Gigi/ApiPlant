package com.svalero.ApiPlant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CuidadoOutDto {

    private long idCuidado;
    private boolean esInterior;
    private String riego;
    private String sustrato;
    private float humedad;
    private List<Long> plantaIds; //para poder ver los ids de plantas asociados

}
