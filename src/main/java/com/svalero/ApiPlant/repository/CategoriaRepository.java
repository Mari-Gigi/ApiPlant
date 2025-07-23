package com.svalero.ApiPlant.repository;

import com.svalero.ApiPlant.domain.Categoria;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoriaRepository  extends CrudRepository<Categoria,Long>{

    List<Categoria> findAll();

    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
    List<Categoria> findByNivelDificultad(Float nivelDificultad);
    List<Categoria> findByParaPrincipiantes(Boolean paraPrincipiantes);

    List<Categoria> findByNombreContainingIgnoreCaseAndNivelDificultad(String nombre, Float nivelDificultad);
    List<Categoria> findByNombreContainingIgnoreCaseAndParaPrincipiantes(String nombre, Boolean paraPrincipiantes);
    List<Categoria> findByNivelDificultadAndParaPrincipiantes(Float nivelDificultad, Boolean paraPrincipiantes);
    List<Categoria> findByNombreContainingIgnoreCaseAndNivelDificultadAndParaPrincipiantes(String nombre, Float nivelDificultad, Boolean paraPrincipiantes);

}
