package com.svalero.ApiPlant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data  //genera getters y setters
@NoArgsConstructor  //genera metodo constructor vacio
@AllArgsConstructor  //genera constructor con argumentos para todos los campos de la clase
@Entity
@Table(name="plagas") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce

public class Plaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idPlaga;
    @Column
    private String nombre;
    @Column
    private String sintomas;
    @Column
    private float riesgo;
    @Column (name="letalidad")
    private boolean esLetal;
    @Column
    private String tratamiento;
   @Column (name="fecha_registro")
    private LocalDate fechaRegistro;

    @ManyToMany(mappedBy = "plagas")
    @JsonBackReference
    @ToString.Exclude
    private List<Planta> plantas;

}

