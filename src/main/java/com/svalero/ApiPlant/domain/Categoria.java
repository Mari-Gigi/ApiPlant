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
@Entity (name="categorias") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce


public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_categoria;
    @Column (name="nombre_categoria")  //normalizacion del nombre de la columna de la tabla
    private String nombreCategoria;
    @Column
    private String descripcion;
    @Column (name="nivel_dificultad")
    private float nivelDificultad;
    @Column
    private boolean paraPrincipiantes;
    @Column (name="fecha_creacion")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaCreacion;

    // 1:N → Plantas
    @OneToMany(mappedBy = "categoria")
    private List<Planta> plantas;

    // 1:N → Consejos (opcional)
    @OneToMany(mappedBy = "categoria")
    private List<Consejo> consejos;
}
