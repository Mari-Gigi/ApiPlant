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
        boolean riegoVacio = (riego == null || riego.isEmpty());
        boolean sustratoVacio = (sustrato == null || sustrato.isEmpty());

        List<Cuidado> cuidadoList;

        if (riegoVacio && sustratoVacio && esInterior == null) {
            cuidadoList = cuidadoRepository.findAll();
        } else if (!riegoVacio && !sustratoVacio && esInterior != null) {
            cuidadoList = cuidadoRepository.findByRiegoContainingIgnoreCaseAndSustratoContainingIgnoreCaseAndEsInterior(riego, sustrato, esInterior);
        } else if (!riegoVacio && !sustratoVacio) {
            cuidadoList = cuidadoRepository.findByRiegoContainingIgnoreCaseAndSustratoContainingIgnoreCase(riego, sustrato);
        } else if (!riegoVacio) {
            cuidadoList = (esInterior == null)
                    ? cuidadoRepository.findByRiegoContainingIgnoreCase(riego)
                    : cuidadoRepository.findByRiegoContainingIgnoreCaseAndEsInterior(riego, esInterior);
        } else if (!sustratoVacio) {
            cuidadoList = (esInterior == null)
                    ? cuidadoRepository.findBySustratoContainingIgnoreCase(sustrato)
                    : cuidadoRepository.findBySustratoContainingIgnoreCaseAndEsInterior(sustrato, esInterior);
        } else { // solo esInterior no nulo
            cuidadoList = cuidadoRepository.findByEsInterior(esInterior);
        }

        if (cuidadoList.isEmpty()) throw new CuidadoNotFoundException();

        return cuidadoList;
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

    public CuidadoOutDto  addCuidado(CuidadoInDto dto) throws PlantaNotFoundException {
        Cuidado cuidado = modelMapper.map(dto, Cuidado.class);
        cuidado.setFechaRegistro(LocalDate.now());

        // Si plantaIds no es null ni vacío, carga las plantas
        if (dto.getPlantaIds() != null && !dto.getPlantaIds().isEmpty()) {
            Iterable<Planta> iterablePlantas = plantaRepository.findAllById(dto.getPlantaIds());
            List<Planta> plantas = StreamSupport.stream(iterablePlantas.spliterator(), false)
                    .collect(Collectors.toList());
            if (plantas.isEmpty()) {
                throw new PlantaNotFoundException();
            }
            cuidado.setPlantas(plantas);
        }

        Cuidado saved = cuidadoRepository.save(cuidado);

        //mapeado manual porque sino devuelve null es plantaIds
        CuidadoOutDto dtoOut = modelMapper.map(saved, CuidadoOutDto.class);

            List<Long> plantaIds = saved.getPlantas().stream()
                    .map(Planta::getId_planta)
                    .collect(Collectors.toList());
            dtoOut.setPlantaIds(plantaIds);

        return dtoOut;
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
