package com.svalero.ApiPlant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CategoriaOutDto {

    private long idCategoria;
    private String nombre;
    private String descripcion;
    private float nivelDificultad;
    private boolean paraPrincipiantes;
    private List<Long> plantaIds; //para poder ver los ids de plantas asociados

}
