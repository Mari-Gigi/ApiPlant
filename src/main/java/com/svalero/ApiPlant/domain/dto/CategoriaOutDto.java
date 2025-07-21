package com.svalero.ApiPlant.domain.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CategoriaOutDto {

    private long idCategoria;
    private String nombre;
    private String descripcion;
    private float nivelDificultad;
    private boolean paraPrincipiantes;

}
