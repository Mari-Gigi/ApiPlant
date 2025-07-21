/*package com.svalero.ApiPlant.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity (name="consejos") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", si no no reconoce


public class Consejo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_consejo;
    @Column
    private String titulo;
    @Column
    private String explicacion;
    @Column
    private boolean verificado;
    @Column
    private float importancia;
   @Column (name="fecha_registro")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaRegistro;

    @ManyToMany(mappedBy = "consejos")
    private List<Planta> plantas;


}
*/