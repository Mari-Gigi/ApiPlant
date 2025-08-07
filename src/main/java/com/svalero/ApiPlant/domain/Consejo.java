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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="consejos") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", si no no reconoce


public class Consejo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idConsejo;
    @Column
    private String titulo;
    @Column
    private String explicacion;
    @Column
    private boolean verificado;
    @Column
    private float importancia;
    @Column (name="fecha_registro")
    private LocalDate fechaRegistro;

    public Consejo(long idConsejo) {    //para que funcione el idConsejo en los test de modifyPLanta
        this.idConsejo = idConsejo;
    }

    @ManyToMany(mappedBy = "consejos")
    @JsonBackReference
    @ToString.Exclude
    private List<Planta> plantas;


}
