package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.PlagaRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class PlagaService {

    @Autowired
    private PlagaRepository plagaRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private ModelMapper modelMapper;

    //MUESTRA PLAGAS CON FILTROS **************************
    public List<Plaga> getAll(String nombre, Float riesgo, Boolean esLetal) throws PlagaNotFoundException {
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

        if (plagaList.isEmpty()) throw new PlagaNotFoundException();

        return plagaList;

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


        // Aquí obtienes los IDs de plantas asociadas
        List<Long> plantaIds = plaga.getPlantas().stream()
                .map(Planta::getId_planta)
                .collect(Collectors.toList());

        dto.setPlantaIds(plantaIds);

        return dto;
    }


    //AÑADE PLAGA CON INDTO *******************************
    public Plaga add(PlagaInDto plagaInDto) {

        Plaga plaga = modelMapper.map(plagaInDto, Plaga.class);
        plaga.setFechaRegistro(LocalDate.now());

        return plagaRepository.save(plaga);

    }


    //MODIFICA PLAGA POR ID CON OUTDTO *******************

    public PlagaOutDto modify(long idPlaga, PlagaInDto dto) throws PlagaNotFoundException, PlantaNotFoundException {
        // Buscar la plaga por ID, lanzar excepción si no existe
        Plaga plaga = plagaRepository.findById(idPlaga).orElseThrow(PlagaNotFoundException::new);

        // Copiar los campos del DTO a la entidad Plaga
        modelMapper.map(dto, plaga);

        // Eliminar la relación actual con las plantas (en ambos sentidos)
        if (plaga.getPlantas() != null)
            plaga.getPlantas().forEach(p -> p.getPlagas().remove(plaga));

        // Obtener la lista de IDs enviada (si hay)
        List<Long> plantaIds = Optional.ofNullable(dto.getPlantaIds()).orElse(List.of());

        // Cargar las nuevas plantas desde la base de datos y asociarlas a la plaga
        List<Planta> nuevasPlantas = StreamSupport
                .stream(plantaRepository.findAllById(plantaIds).spliterator(), false)
                .peek(p -> p.getPlagas().add(plaga))  // Relación bidireccional
                .collect(Collectors.toList());

        // Verificar si todos los IDs de plantas enviados existen
        if (nuevasPlantas.size() != plantaIds.size()) {
            List<Long> encontrados = nuevasPlantas.stream().map(Planta::getId_planta).toList();
            List<Long> faltantes = plantaIds.stream().filter(id -> !encontrados.contains(id)).toList();
            throw new PlantaNotFoundException("No se encontraron plantas con IDs: " + faltantes);
        }

        // Asignar las nuevas plantas a la plaga
        plaga.setPlantas(nuevasPlantas);

        // Guardar los cambios en la base de datos
        plagaRepository.save(plaga);

        // Convertir la plaga modificada a DTO de salida
        PlagaOutDto out = modelMapper.map(plaga, PlagaOutDto.class);
        out.setPlantaIds(nuevasPlantas.stream().map(Planta::getId_planta).toList());

        return out;
    }


    //BORRA PLAGA POR ID ********************************
    public void remove(long idPlaga) throws PlagaNotFoundException, PlagaConflictException {

        plagaRepository.findById(idPlaga).orElseThrow(PlagaNotFoundException::new);
        List<Planta> plantasConPlaga = plantaRepository.findByPlagas_IdPlaga(idPlaga);

        if (!plantasConPlaga.isEmpty()) { throw new PlagaConflictException();}

        plagaRepository.deleteById(idPlaga);
    }

}


