package com.svalero.ApiPlant.repository;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.Plaga;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface ConsejoRepository extends CrudRepository<Consejo,Long> {

    List<Consejo> findAll();

    List<Consejo> findByTituloContainingIgnoreCase(String titulo);
    List<Consejo> findByImportancia(Float importancia);
    List<Consejo> findByVerificado(Boolean verificado);
    List<Consejo> findByTituloContainingIgnoreCaseAndImportancia(String titulo, Float importancia);
    List<Consejo> findByTituloContainingIgnoreCaseAndVerificado(String titulo, Boolean verificado);
    List<Consejo> findByTituloContainingIgnoreCaseAndImportanciaAndVerificado(String titulo, Float importancia, Boolean verificado);
    List<Consejo> findByImportanciaAndVerificado(Float importancia, Boolean verificado);

}
