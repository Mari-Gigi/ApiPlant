package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.CuidadoInDto;
import com.svalero.ApiPlant.domain.dto.CuidadoOutDto;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.CuidadoConflictException;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuidadoService {

    @Autowired
    private CuidadoRepository cuidadoRepository;

    @Autowired
    private PlantaRepository plantaRepository;

    @Autowired
    private ModelMapper modelMapper;

    // MUESTRA CUIDADOS CON FILTROS ***********************
    public List<CuidadoOutDto> getAll(String riego, String sustrato, Boolean esInterior) {
        List<Cuidado> cuidadoList;

        boolean riegoVacio = (riego == null || riego.isEmpty());
        boolean sustratoVacio = (sustrato == null || sustrato.isEmpty());

        if (riegoVacio && sustratoVacio && esInterior == null) {
            cuidadoList = cuidadoRepository.findAll();
        } else if (!riegoVacio && sustratoVacio && esInterior == null) {
            cuidadoList = cuidadoRepository.findByRiegoContainingIgnoreCase(riego);
        } else if (riegoVacio && !sustratoVacio && esInterior == null) {
            cuidadoList = cuidadoRepository.findBySustratoContainingIgnoreCase(sustrato);
        } else if (riegoVacio && sustratoVacio) {
            // Solo filtro por esInterior
            cuidadoList = cuidadoRepository.findByEsInterior(esInterior);
        } else if (!riegoVacio && sustratoVacio) {
            cuidadoList = cuidadoRepository.findByRiegoContainingIgnoreCaseAndEsInterior(riego, esInterior);
        } else if (riegoVacio) {
            cuidadoList = cuidadoRepository.findBySustratoContainingIgnoreCaseAndEsInterior(sustrato, esInterior);
        } else if (esInterior != null) {
            cuidadoList = cuidadoRepository.findByRiegoContainingIgnoreCaseAndSustratoContainingIgnoreCaseAndEsInterior(riego, sustrato, esInterior);
        } else {
            // Caso: riego y sustrato definidos pero esInterior == null
            cuidadoList = cuidadoRepository.findAll().stream()
                    .filter(c -> c.getRiego().toLowerCase().contains(riego.toLowerCase()))
                    .filter(c -> c.getSustrato().toLowerCase().contains(sustrato.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return modelMapper.map(cuidadoList, new TypeToken<List<CuidadoOutDto>>() {}.getType());
    }

    // MUESTRA CUIDADOS POR ID ***********************
    public Cuidado get(long idCuidado)throws CuidadoNotFoundException{
        return cuidadoRepository.findById(idCuidado)
                .orElseThrow(CuidadoNotFoundException::new);
    }

    // AÑADE CUIDADO CON INDTO ***********************
    public Cuidado add(Cuidado cuidado) {
        cuidado.setFechaRegistro(LocalDate.now());
        return cuidadoRepository.save(cuidado);

    }

    // MODIFICA CUIDADO POR ID  **********************   REVISAR ******************************
    public CuidadoOutDto modify(long idCuidado, CuidadoInDto cuidadoInDto) throws CuidadoNotFoundException {
        Cuidado cuidado = cuidadoRepository.findById(idCuidado)
                .orElseThrow(CuidadoNotFoundException::new);

        modelMapper.map(cuidadoInDto, cuidado);
        cuidadoRepository.save(cuidado);

        return modelMapper.map(cuidado, CuidadoOutDto.class);
    }

    //BORRA CUIDADO POR ID CON REVISION DE CONFLICTO CON PLANTA **********************
    public void remove(Long idCuidado) throws CuidadoNotFoundException, CuidadoConflictException {
        cuidadoRepository.findById(idCuidado)
                .orElseThrow(CuidadoNotFoundException::new);
        List<Planta> plantasConCuidado = plantaRepository.findByCuidado_IdCuidado(idCuidado);

        if (!plantasConCuidado.isEmpty()) {
            throw new CuidadoConflictException("plant-associated care");
        }

        cuidadoRepository.deleteById(idCuidado);
    }



}
