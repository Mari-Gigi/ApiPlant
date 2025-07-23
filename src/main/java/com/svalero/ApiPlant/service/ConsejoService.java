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
import java.util.List;
import java.util.stream.Collectors;


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




    // MODIFICA CATEGORIA POR ID ************************** REVISAR *********************************
    public ConsejoOutDto modify(long idConsejo, ConsejoInDto consejoInDto) throws ConsejoNotFoundException {
        Consejo consejo = consejoRepository.findById(idConsejo)
                .orElseThrow(ConsejoNotFoundException::new);

        modelMapper.map(consejoInDto, consejo);
        consejoRepository.save(consejo);

        return modelMapper.map(consejo, ConsejoOutDto.class);
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
