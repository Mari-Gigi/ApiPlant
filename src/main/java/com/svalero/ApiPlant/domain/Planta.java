package com.svalero.ApiPlant.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data  //genera getters y setters
@NoArgsConstructor  //genera metodo constructor vacio
@AllArgsConstructor  //genera constructor con argumentos para todos los campos de la clase
@Entity (name="plantas") //nombre de la tabla //Para añadir en H2 tengo introducir el nombre de la tabla y los atributos con "", sino no reconoce

public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // para que sea autonumerico
    private long id_planta;
    @Column
    private String genero;
    @Column
    private String especie;
    @Column(name = "altura_maxima", nullable = true) // o false si quieres que sea obligatorio //nullable xq al no rellenar el campo, devolvia null y daba fallo
    private Float alturaMaxima;
    @Column (name="tipo-crecimiento")
    private String tipoCrecimiento;
    @Column (name="fecha_registro")  //normalizacion del nombre de la columna de la tabla
    private LocalDate fechaRegistro;
    @Column (name="toxicidad")
    private Boolean esToxica; //con mayuscula consigo que el valor del booleano pueda ser tambien  null

    //RELACIONES ENTRE ENTIDADES **********************
    @ManyToOne // relacion 1:1 - una planta tiene un cuidado
    @JoinColumn(name = "cuidado_id")
    @ToString.Exclude  // EVITAR BUCLES AL APUNTAR A VARIAS CLASES
    private Cuidado cuidado;

    @ManyToOne // relacion 1:1 - muchas plantas pueden pertenecer a uan categoria
    @JoinColumn(name = "categoria_id")
    @ToString.Exclude
    private Categoria categoria;

    @ManyToMany // relacion N:N - muchas plantas pueden tener muchas plagas
    @JoinTable
            (name = "planta_plaga",
            joinColumns = @JoinColumn(name = "planta_id"),
            inverseJoinColumns = @JoinColumn(name = "plaga_id"))
    @JsonBackReference
    @ToString.Exclude
    private List<Plaga> plagas;


  /*  @ManyToMany // relacion N:N - muchas plantas pueden tener muchos consejos
    @JoinTable
            (name = "planta_consejo",
            joinColumns = @JoinColumn(name = "planta_id"),
            inverseJoinColumns = @JoinColumn(name = "consejo_id"))
    private List<Consejo> consejos;*/


}

