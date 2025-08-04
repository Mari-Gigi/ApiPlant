package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.CuidadoConflictException;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlagaNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CuidadoService {

    @Autowired
    private CuidadoRepository cuidadoRepository;

    @Autowired
    private PlantaRepository plantaRepository;

    @Autowired
    private ModelMapper modelMapper;

    // MUESTRA CUIDADOS CON FILTROS ***********************
    public List<Cuidado> getAll(String riego, String sustrato, Boolean esInterior) throws CuidadoNotFoundException {
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

        if (cuidadoList.isEmpty()) { throw new CuidadoNotFoundException(); }
        return modelMapper.map(cuidadoList, new TypeToken<List<Cuidado>>() {}.getType());
    }


    //MUESTRA CUIDADOS POR ID CON OUTDTO ********************************
    public CuidadoOutDto get(long idCuidado) throws CuidadoNotFoundException {
        Cuidado cuidado = cuidadoRepository.findById(idCuidado).orElseThrow(CuidadoNotFoundException::new);

        CuidadoOutDto dto = new CuidadoOutDto();
        dto.setIdCuidado(cuidado.getIdCuidado());
        dto.setEsInterior(cuidado.isEsInterior());
        dto.setRiego(cuidado.getRiego());
        dto.setSustrato(cuidado.getSustrato());
        dto.setHumedad(cuidado.getHumedad());

        // Aquí obtienes los IDs de plantas asociadas
        List<Long> plantaIds = cuidado.getPlantas().stream()
                .map(Planta::getId_planta)
                .collect(Collectors.toList());

        dto.setPlantaIds(plantaIds);

        return dto;
    }

    public Cuidado  addCuidado(CuidadoInDto dto) {
        Cuidado cuidado = modelMapper.map(dto, Cuidado.class);
        cuidado.setFechaRegistro(LocalDate.now());
        // Aquí puedes manejar relaciones con plantas, validaciones, etc.
        Cuidado saved = cuidadoRepository.save(cuidado);
        return modelMapper.map(saved, Cuidado.class);
    }


    // MODIFICA CUIDADO POR ID  CON REVISION DE OCNFLICTO POR PLANTA**********************
   public CuidadoOutDto modify(long idCuidado, CuidadoInDto cuidadoInDto) throws CuidadoNotFoundException, CuidadoConflictException {
       Cuidado cuidado = cuidadoRepository.findById(idCuidado)
               .orElseThrow(CuidadoNotFoundException::new);
       //recupero los actuales plantaIds asociados
       List<Long> actualesIds = Optional.ofNullable(cuidado.getPlantas())
               .orElseGet(Collections::emptyList)
               .stream()
               .map(Planta::getId_planta)
               .toList();
       //agrupo los nuevos plantaIds
       List<Long> nuevosIds = Optional.ofNullable(cuidadoInDto.getPlantaIds())
               .orElseGet(Collections::emptyList);

       // Reviso los nuevos plantaIds para ver si hay conflicto
       if (actualesIds.stream().anyMatch(id -> !nuevosIds.contains(id))) {throw new CuidadoConflictException();}

       // Incluye los nuevos plantaIds a ese cuidado
       List<Long> idsParaAnadir = nuevosIds.stream()
               .filter(id -> !actualesIds.contains(id))
               .toList();

       if (!idsParaAnadir.isEmpty()) {
           List<Planta> plantasParaAnadir = StreamSupport.stream(plantaRepository.findAllById(idsParaAnadir).spliterator(), false)
                   .toList();
           if (cuidado.getPlantas() == null) cuidado.setPlantas(new ArrayList<>());
           cuidado.getPlantas().addAll(plantasParaAnadir);
       }

       modelMapper.map(cuidadoInDto, cuidado);
       cuidadoRepository.save(cuidado);

       CuidadoOutDto outDto = modelMapper.map(cuidado, CuidadoOutDto.class);
       outDto.setPlantaIds(Optional.ofNullable(cuidado.getPlantas())
               .orElseGet(Collections::emptyList)
               .stream()
               .map(Planta::getId_planta)
               .toList());

       return outDto;
   }


    //BORRA CUIDADO POR ID CON REVISION DE CONFLICTO CON PLANTA **********************
    public void remove(Long idCuidado) throws CuidadoNotFoundException, CuidadoConflictException {
        cuidadoRepository.findById(idCuidado).orElseThrow(CuidadoNotFoundException::new);
        List<Planta> plantasConCuidado = plantaRepository.findByCuidado_IdCuidado(idCuidado);

        if (!plantasConCuidado.isEmpty()) {
            throw new CuidadoConflictException();
        }

        cuidadoRepository.deleteById(idCuidado);
    }



}
