package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CategoriaRepository;
import com.svalero.ApiPlant.repository.ConsejoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class ConsejoService {

    @Autowired
    private ConsejoRepository consejoRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private ModelMapper modelMapper;


    //MUESTRA CONSEJOS CON FILTROS SIN OUTDTO************************
    public List<Consejo> getAll(String titulo, Boolean verificado, Float importancia) {
        List<Consejo> consejoList;

        boolean tituloVacio = (titulo == null || titulo.isEmpty());

        if (tituloVacio && importancia == null && verificado == null) {
            consejoList = consejoRepository.findAll();
        } else if (!tituloVacio && importancia == null && verificado == null) {
            consejoList = consejoRepository.findByTituloContainingIgnoreCase(titulo);
        } else if (tituloVacio && importancia != null && verificado == null) {
            consejoList = consejoRepository.findByImportancia(importancia);
        } else if (tituloVacio && importancia == null) {
            consejoList = consejoRepository.findByVerificado(verificado);
        } else if (!tituloVacio && importancia != null && verificado == null) {
            consejoList = consejoRepository.findByTituloContainingIgnoreCaseAndImportancia(titulo, importancia);
        } else if (!tituloVacio && importancia == null) {
            consejoList = consejoRepository.findByTituloContainingIgnoreCaseAndVerificado(titulo, verificado);
        } else if (!tituloVacio) {
            consejoList = consejoRepository.findByTituloContainingIgnoreCaseAndImportanciaAndVerificado(
                    titulo, importancia, verificado);
        } else {
            consejoList = consejoRepository.findByImportanciaAndVerificado(importancia, verificado);
        }

        return consejoList;
    }


    //MUESTRA CONSEJO POR ID CON OUTDTO *****************
    public ConsejoOutDto get(long idConsejo)throws ConsejoNotFoundException {
        Consejo consejo = consejoRepository.findById(idConsejo)
                .orElseThrow(ConsejoNotFoundException::new);

        ConsejoOutDto dto = new ConsejoOutDto();
        dto.setIdConsejo(consejo.getIdConsejo());
        dto.setTitulo(consejo.getTitulo());
        dto.setExplicacion(consejo.getExplicacion());
        dto.setVerificado(consejo.isVerificado());
        dto.setImportancia(consejo.getImportancia());

        // Aquí obtienes los IDs de plantas asociadas
        List<Long> plantaIds = consejo.getPlantas().stream()
                .map(Planta::getId_planta)
                .collect(Collectors.toList());

        dto.setPlantaIds(plantaIds);

        return dto;

    }


    //AÑADE CONSEJO CON INDTO *****************
    public Consejo add(ConsejoInDto consejoInDto) {

        Consejo consejo= modelMapper.map(consejoInDto, Consejo.class);
        consejo.setFechaRegistro(LocalDate.now());

        return consejoRepository.save(consejo);

    }


    // MODIFICA CONSEJO POR ID CON OUTDTO *******************
    public ConsejoOutDto modify(long idConsejo, ConsejoInDto consejoInDto) throws ConsejoNotFoundException {
        Consejo consejo = consejoRepository.findById(idConsejo)
                .orElseThrow(ConsejoNotFoundException::new);

        modelMapper.map(consejoInDto, consejo);

        // Actualización de la lista de plantas (añadir o eliminar)
        if (consejoInDto.getPlantaIds() != null && !consejoInDto.getPlantaIds().isEmpty()) {
            List<Planta> plantas = StreamSupport
                    .stream(plantaRepository.findAllById(consejoInDto.getPlantaIds()).spliterator(), false)
                    .collect(Collectors.toList());
            consejo.setPlantas(plantas);
        } else {
            consejo.setPlantas(new ArrayList<>()); // elimina asociaciones si se envía vacío
        }

        consejoRepository.save(consejo);

        // Convertir a OutDto y rellenar IDs
        ConsejoOutDto consejoOutDto = modelMapper.map(consejo, ConsejoOutDto.class);

        if (consejo.getPlantas() != null) {
            List<Long> plantaIds = consejo.getPlantas().stream()
                    .map(Planta::getId_planta)
                    .collect(Collectors.toList());
            consejoOutDto.setPlantaIds(plantaIds);
        } else {
            consejoOutDto.setPlantaIds(new ArrayList<>());
        }

        return consejoOutDto;
    }



    //BORRA CATEGORIA CON REVISION DE CONFLICTO CON PLANTA ****************************
    public void remove(Long idConsejo) throws ConsejoNotFoundException, ConsejoConflictException {
        consejoRepository.findById(idConsejo)
                .orElseThrow(ConsejoNotFoundException::new);

        List<Planta> plantasConConsejo = plantaRepository.findByConsejos_IdConsejo(idConsejo);

        if (!plantasConConsejo.isEmpty()) {
            throw new ConsejoConflictException();
        }

        consejoRepository.deleteById(idConsejo);
    }

}
