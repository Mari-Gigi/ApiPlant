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
@Entity (name="plantas") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce

public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_planta;
    @Column
    private String nombre;
    @Column (name="altura_maxima")
    private float alturaMaxima;
    @Column
    private String imagen;
    @Column (name="fecha_registro")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaRegistro;
    @Column (name="toxicidad")
    private boolean esToxica;


    // N:1 → Categoria
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // 1:N → Cuidados
    @OneToMany(mappedBy = "planta", cascade = CascadeType.ALL)
    private List<Cuidado> cuidados;

    // N:N → Plagas
    @ManyToMany
    @JoinTable(
            name = "planta_plaga",
            joinColumns = @JoinColumn(name = "planta_id"),
            inverseJoinColumns = @JoinColumn(name = "plaga_id")
    )
    private List<Plaga> plagas;

    // 1:N → Consejos (opcional)
    @OneToMany(mappedBy = "planta")
    private List<Consejo> consejos;

}

