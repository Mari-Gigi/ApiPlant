package com.svalero.ApiPlant.repository;
import com.svalero.ApiPlant.domain.Plaga;
import java.util.List;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PlagaRepository extends CrudRepository<Plaga,Long>  {

    List<Plaga> findAll();

    List<Plaga> findByNombreContainingIgnoreCase(String nombre);
    List<Plaga> findByRiesgo(Float riesgo);
    List<Plaga> findByEsLetal(Boolean esLetal);

    List<Plaga> findByNombreContainingIgnoreCaseAndRiesgo(String nombre, Float riesgo);
    List<Plaga> findByNombreContainingIgnoreCaseAndEsLetal(String nombre, Boolean esLetal);
    List<Plaga> findByRiesgoAndEsLetal(Float riesgo, Boolean esLetal);
    List<Plaga> findByNombreContainingIgnoreCaseAndRiesgoAndEsLetal(String nombre, Float riesgo, Boolean esLetal);

}
