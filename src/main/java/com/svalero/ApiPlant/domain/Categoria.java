package com.svalero.ApiPlant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data  //genera getters y setters
@NoArgsConstructor  //genera metodo constructor vacio
@AllArgsConstructor  //genera constructor con argumentos para todos los campos de la clase
@Entity (name="categorias") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce


public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCategoria;
    @Column
    private String nombre;
    @Column
    private String descripcion;
    @Column (name="nivel_dificultad")
    private float nivelDificultad;
    @Column
    private boolean paraPrincipiantes;
    @Column (name="fecha_registro")
    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value = "categoria_plantas")
    @ToString.Exclude
    private List<Planta> plantas;

}


