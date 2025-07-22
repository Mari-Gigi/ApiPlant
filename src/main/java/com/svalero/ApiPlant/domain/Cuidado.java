package com.svalero.ApiPlant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity (name="cuidados") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", si no no reconoce

public class Cuidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCuidado;
    @Column
    private boolean esInterior;
    @Column
    private String riego;
    @Column
    private String sustrato;
    @Column
    private float humedad;
    @Column (name="fecha_registro")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaRegistro;


   /* @OneToMany(mappedBy = "cuidado")*/
    @OneToMany(mappedBy = "cuidado", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference (value = "cuidados_plantas") //para el bucle que se genera xq el cuidado apunta a una planta, qeu apunta a un cuidado...
    @ToString.Exclude
    private List<Planta> plantas;

}

