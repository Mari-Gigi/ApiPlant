package com.svalero.ApiPlant.repository;

import com.svalero.ApiPlant.domain.Cuidado;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuidadoRepository extends CrudRepository<Cuidado,Long> {

    List<Cuidado> findAll();

    List<Cuidado> findByRiegoContainingIgnoreCase(String riego);
    List<Cuidado> findBySustratoContainingIgnoreCase(String sustrato);
    List<Cuidado> findByEsInterior(boolean esInterior);

    List<Cuidado> findByRiegoContainingIgnoreCaseAndSustratoContainingIgnoreCase(String riego, String sustrato);
    List<Cuidado> findByRiegoContainingIgnoreCaseAndEsInterior(String riego, boolean esInterior);
    List<Cuidado> findBySustratoContainingIgnoreCaseAndEsInterior(String sustrato, boolean esInterior);

    List<Cuidado> findByRiegoContainingIgnoreCaseAndSustratoContainingIgnoreCaseAndEsInterior(String riego, String sustrato, boolean esInterior);


}