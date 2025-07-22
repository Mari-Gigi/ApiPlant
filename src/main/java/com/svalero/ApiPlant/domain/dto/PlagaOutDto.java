package com.svalero.ApiPlant.domain.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PlagaOutDto {

    private long idPlaga;
    private String nombre;
    private String sintomas;
    private float riesgo;
    private boolean esLetal;
    private String tratamiento;
    private LocalDate fechaRegistro;
    private List<Long> plantaIds;

}



