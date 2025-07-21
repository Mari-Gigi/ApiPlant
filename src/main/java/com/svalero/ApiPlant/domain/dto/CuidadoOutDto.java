package com.svalero.ApiPlant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CuidadoOutDto {

    private long id_cuidado;
    private boolean esInterior;
    private String riego;
    private String sustrato;
    private float humedad;

}
