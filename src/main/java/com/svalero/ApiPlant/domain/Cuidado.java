package com.svalero.ApiPlant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity (name="cuidados") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", si no no reconoce

public class Cuidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_cuidado;
    @Column
    private int plantaId;
    @Column
    private boolean esInterior;
    @Column
    private String riego;
    @Column
    private String sustrato;
    @Column
    private float humedad;
    @Column(name = "fecha_abono")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaAbono;

    // N:1 → Planta
    @ManyToOne
    @JoinColumn(name = "planta_id")
    private Planta planta;

}

