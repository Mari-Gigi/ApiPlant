//seria el dao: acceso a la base de datos

package com.svalero.ApiPlant.repository;

import com.svalero.ApiPlant.domain.Planta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantaRepository extends CrudRepository<Planta,Long> {

    List<Planta> findAll();
    List<Planta> findByCuidado_IdCuidado(Long idCuidado);
    List<Planta> findByCategoria_IdCategoria(Long idCategoria);


    // Filtros individuales
    List<Planta> findByGeneroContainingIgnoreCase(String genero);
    List<Planta> findByEspecieContainingIgnoreCase(String especie);
    List<Planta> findByEsToxicaTrue();
    List<Planta> findByEsToxicaFalse();

    // Combinaciones de filtros
    List<Planta> findByGeneroContainingIgnoreCaseAndEsToxicaTrue(String genero);
    List<Planta> findByGeneroContainingIgnoreCaseAndEsToxicaFalse(String genero);

    List<Planta> findByEspecieContainingIgnoreCaseAndEsToxicaTrue(String especie);
    List<Planta> findByEspecieContainingIgnoreCaseAndEsToxicaFalse(String especie);

    // (Opcional) Combinación triple
    List<Planta> findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaTrue(String genero, String especie);
    List<Planta> findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaFalse(String genero, String especie);











}





