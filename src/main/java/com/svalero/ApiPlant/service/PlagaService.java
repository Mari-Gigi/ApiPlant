package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.CategoriaOutDto;
import com.svalero.ApiPlant.domain.dto.PlagaInDto;
import com.svalero.ApiPlant.domain.dto.PlagaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CategoriaRepository;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlagaRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PlagaService {

    @Autowired
    private PlagaRepository plagaRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private ModelMapper modelMapper;

    //MUESTRA PLAGAS CON FILTROS **************************
    public List<PlagaOutDto> getAll(String nombre, Float riesgo, Boolean esLetal) {
        List<Plaga> plagaList;

        boolean nombreVacio = (nombre == null || nombre.isEmpty());

        if (nombreVacio && riesgo == null && esLetal == null) {
            plagaList = plagaRepository.findAll();

        } else if (!nombreVacio && riesgo == null && esLetal == null) {
            plagaList = plagaRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (nombreVacio && riesgo != null && esLetal == null) {
            plagaList = plagaRepository.findByRiesgo(riesgo);
        } else if (nombreVacio && riesgo == null) {
            plagaList = plagaRepository.findByEsLetal(esLetal);
        } else if (!nombreVacio && riesgo != null && esLetal == null) {
            plagaList = plagaRepository.findByNombreContainingIgnoreCaseAndRiesgo(nombre, riesgo);
        } else if (!nombreVacio && riesgo == null) {
            plagaList = plagaRepository.findByNombreContainingIgnoreCaseAndEsLetal(nombre, esLetal);
        } else if (nombreVacio) {
            plagaList = plagaRepository.findByRiesgoAndEsLetal(riesgo, esLetal);
        } else {
            // Todos los filtros aplicados
            plagaList = plagaRepository.findByNombreContainingIgnoreCaseAndRiesgoAndEsLetal(nombre, riesgo, esLetal);
        }

        return modelMapper.map(plagaList, new TypeToken<List<PlagaOutDto>>() {
        }.getType());
    }

    //MUESTRA PLAGAS POR ID CON OUTDTO ********************************
    public PlagaOutDto get(long idPlaga) throws PlagaNotFoundException {
        Plaga plaga = plagaRepository.findById(idPlaga)
                .orElseThrow(PlagaNotFoundException::new);

        PlagaOutDto dto = new PlagaOutDto();
        dto.setIdPlaga(plaga.getIdPlaga());
        dto.setNombre(plaga.getNombre());
        dto.setSintomas(plaga.getSintomas());
        dto.setRiesgo(plaga.getRiesgo());
        dto.setEsLetal(plaga.isEsLetal());
        dto.setTratamiento(plaga.getTratamiento());
        dto.setFechaRegistro(plaga.getFechaRegistro());


        // Aquí obtienes los IDs de plantas asociadas
        List<Long> plantaIds = plaga.getPlantas().stream()
                .map(Planta::getId_planta)
                .collect(Collectors.toList());

        dto.setPlantaIds(plantaIds);

        return dto;
    }


    //AÑADE PLAGA CON INDTO *******************************
    public PlagaOutDto add(PlagaInDto plagaInDto) {
        Plaga plaga= modelMapper.map(plagaInDto, Plaga.class);
        plaga.setFechaRegistro(LocalDate.now());

        Plaga nuevaPlaga = plagaRepository.save(plaga);

        return modelMapper.map (nuevaPlaga, PlagaOutDto.class);

    }

    //MODIFICA PLAGA POR ID **************** REVISAR ***************************
    public PlagaOutDto modify(long idPlaga, PlagaInDto plagaInDto) throws PlagaNotFoundException {
        Plaga plaga = plagaRepository.findById(idPlaga)
                .orElseThrow(PlagaNotFoundException::new);

        modelMapper.map(plagaInDto, plaga);
        plagaRepository.save(plaga);

        return modelMapper.map(plaga, PlagaOutDto.class);
    }

    //BORRA PLAGA POR ID ********************************
    public void remove (long idPlaga) throws PlagaNotFoundException, PlagaConflictException {
        plagaRepository.findById(idPlaga)
                .orElseThrow(PlagaNotFoundException::new);
        List<Planta> plantasConPlaga = plantaRepository.findByPlagas_IdPlaga(idPlaga);

        if (!plantasConPlaga.isEmpty()) {
            throw new PlagaConflictException("plant-associated pest");
        }

        plagaRepository.deleteById(idPlaga);
    }


}

