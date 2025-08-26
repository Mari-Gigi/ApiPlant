package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

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

    //AÑADE CUIDADO CON INDTO *****************
    //añade sin plantaIds o revisando si la plantaId indicada existe
    public CuidadoOutDto addCuidado(CuidadoInDto dto) throws PlantaNotFoundException {
        Cuidado cuidado = modelMapper.map(dto, Cuidado.class);
        cuidado.setFechaRegistro(LocalDate.now());

        if (dto.getPlantaIds() != null && !dto.getPlantaIds().isEmpty()) {
            List<Planta> plantas = StreamSupport.stream(plantaRepository.findAllById(dto.getPlantaIds()).spliterator(), false)
                    .collect(Collectors.toList());

            if (plantas.size() != dto.getPlantaIds().size()) {
                throw new PlantaNotFoundException("PlantaIds indicado inexistente");
            }

            cuidado.setPlantas(plantas);
        }

        Cuidado saved = cuidadoRepository.save(cuidado);
        CuidadoOutDto dtoOut = modelMapper.map(saved, CuidadoOutDto.class);

        if (saved.getPlantas() != null && !saved.getPlantas().isEmpty()) {
            dtoOut.setPlantaIds(saved.getPlantas().stream()
                    .map(Planta::getId_planta)
                    .collect(Collectors.toList()));
        }

        return dtoOut;
    }


    // MODIFICA CUIDADO POR ID  CON REVISION DE OCNFLICTO POR PLANTA  **********************
                // puedo modificar cualquier campo menos los plantasIds. Si el cuidado ya est aasociado a plantas, modifica los campos
    public CuidadoOutDto modify(long idCuidado, CuidadoInDto cuidadoInDto) throws CuidadoNotFoundException, CuidadoConflictException {

        //buscame el idCuidado qeu te doy para modificar y sino  esta, lanza excep
       Cuidado cuidado = cuidadoRepository.findById(idCuidado)
               .orElseThrow(CuidadoNotFoundException::new);

       //recupero los actuales plantaIds asociados al idCuidado seleccionado
       List<Long> actualesPlantaIds = Optional.ofNullable(cuidado.getPlantas())
               .orElseGet(Collections::emptyList)
               .stream()
               .map(Planta::getId_planta)
               .toList();
       //agrupo los nuevos plantaIds (en el caso de que se hayan modificado (añadido) el en json de entrada)
       List<Long> nuevosPlantaIds = Optional.ofNullable(cuidadoInDto.getPlantaIds())
               .orElseGet(Collections::emptyList);

       // Reviso los nuevos plantaIds para ver si se ha quitado/añadido alguna para que salte excep (cuidados solo modificables desde PLanta)
        if (!actualesPlantaIds.equals(nuevosPlantaIds)) {
            throw new CuidadoConflictException();
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
