package com.svalero.ApiPlant.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data  //genera getters y setters
@NoArgsConstructor  //genera metodo constructor vacio
@AllArgsConstructor  //genera constructor con argumentos para todos los campos de la clase
@Entity (name="plagas") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce

public class Plaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_plaga;
    @Column (name="nombre_plaga")  //normalizacion del nombre de la columna de la tabla
    private String nombrePlaga;
    @Column (name="url_imagen_plaga")
    private String imagenPlaga;
    @Column
    private float riesgo;
    @Column (name="letalidad")
    private boolean esLetal;
    @Column (name="fecha_aparicion")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaAparicion;

    // N:N → Plantas (relación inversa)
    @ManyToMany(mappedBy = "plagas")
    private List<Planta> plantas;
}


