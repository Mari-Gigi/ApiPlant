package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PlantaService {

    @Autowired
    private PlantaRepository plantaRepository; //para que pueda acceder a Repository
    @Autowired
    private CuidadoRepository cuidadoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private PlagaRepository plagaRepository;
    @Autowired
    private ConsejoRepository consejoRepository;
    @Autowired
    private ModelMapper modelMapper;  //para los Dto

    public void setModelMapper(ModelMapper modelMapper) {this.modelMapper = modelMapper;}

    // MUESTRA PLANTAS CON FILTROS ***********************
    /*devuelve el array de plagas como null xq el model mapper no lo reconoce */
    public List<PlantaOutDto> getAll(String genero, String especie, Boolean esToxica) throws PlantaNotFoundException{
        List<Planta> plantaList; //lista de objetos Planta que contendra la consulta

        boolean generoVacio = (genero == null || genero.isEmpty());
        boolean especieVacio = (especie == null || especie.isEmpty());

        if (generoVacio && especieVacio && esToxica == null) { //sino hay filtro, devuelve todas.
            plantaList = plantaRepository.findAll();
        } else if (!generoVacio && especieVacio && esToxica == null) { //solo se pasa genero
            plantaList = plantaRepository.findByGeneroContainingIgnoreCase(genero);
        } else if (generoVacio && !especieVacio && esToxica == null) { //solo por especie
            plantaList = plantaRepository.findByEspecieContainingIgnoreCase(especie);
        } else if (generoVacio && especieVacio) { //solo por toxica
            plantaList = esToxica ? plantaRepository.findByEsToxicaTrue() : plantaRepository.findByEsToxicaFalse();
        } else if (!generoVacio && especieVacio) { //genero y toicidad
            plantaList = esToxica ? plantaRepository.findByGeneroContainingIgnoreCaseAndEsToxicaTrue(genero)
                    : plantaRepository.findByGeneroContainingIgnoreCaseAndEsToxicaFalse(genero);
        } else if (generoVacio) {
            plantaList = esToxica ? plantaRepository.findByEspecieContainingIgnoreCaseAndEsToxicaTrue(especie)
                    : plantaRepository.findByEspecieContainingIgnoreCaseAndEsToxicaFalse(especie);
        } else if (esToxica != null) {
            plantaList = esToxica ? plantaRepository.findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaTrue(genero, especie)
                    : plantaRepository.findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaFalse(genero, especie);
        } else {
            plantaList = plantaRepository.findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCase(genero, especie);
        }

        List<PlantaOutDto> dtoList = plantaList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        if (dtoList.isEmpty()) {
            throw new PlantaNotFoundException();
        }

        return dtoList;

    }

    //VERIFICA SI HAY REGISTRO DE PLAGASIDS Y CONSEJOSIDS Y LOS DEVUELVE COMO LISTA (O COMO LISTA VACIA)
    private PlantaOutDto convertToDto(Planta planta) {
        PlantaOutDto dto = modelMapper.map(planta, PlantaOutDto.class);

        dto.setCuidadoId(planta.getCuidado().getIdCuidado());
        dto.setCategoriaId(planta.getCategoria().getIdCategoria());
        dto.setPlagaIds(
                planta.getPlagas() != null
                        ? planta.getPlagas().stream()
                        .map(Plaga::getIdPlaga)
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
        dto.setConsejoIds(
                planta.getConsejos() != null
                        ? planta.getConsejos().stream()
                        .map(Consejo::getIdConsejo)
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
        return dto;
    }

    //DEVUELVE PLANTA POR ID CON OUTDTO ******************************
    public PlantaOutDto get(long id_planta) throws PlantaNotFoundException {
        Planta planta = plantaRepository.findById(id_planta).orElseThrow(PlantaNotFoundException::new);
        return convertToDto(planta);
    }

    //ASIGNAR PLAGAS Y CONSEJOS ********************************
    private void asignarPlagasYConsejos(Planta planta, PlantaInDto plantaInDto)
            throws PlagaNotFoundException, ConsejoNotFoundException {


        if (plantaInDto.getPlagaIds() != null && !plantaInDto.getPlagaIds().isEmpty()) {
            List<Plaga> plagas = StreamSupport.stream(
                            plagaRepository.findAllById(plantaInDto.getPlagaIds()).spliterator(), false)
                    .collect(Collectors.toList());

            if (plagas.size() != plantaInDto.getPlagaIds().size()) { throw new PlagaNotFoundException();}
            planta.setPlagas(plagas);
        } else {
            planta.setPlagas(new ArrayList<>());
        }

        if (plantaInDto.getConsejoIds() != null && !plantaInDto.getConsejoIds().isEmpty()) {
            List<Consejo> consejos = StreamSupport.stream(
                            consejoRepository.findAllById(plantaInDto.getConsejoIds()).spliterator(), false)
                    .collect(Collectors.toList());

            if (consejos.size() != plantaInDto.getConsejoIds().size()) {throw new ConsejoNotFoundException();}
            planta.setConsejos(consejos);
        } else {
            planta.setConsejos(new ArrayList<>());
        }
    }

    // AÑADE PLANTA CON INDTO **********************************
    public PlantaOutDto add(PlantaInDto plantaInDto) throws
            CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {

        // obtengo los campos obligatorios y sino estan, excepcion
        Cuidado cuidado = cuidadoRepository.findById(plantaInDto.getCuidadoId()).orElseThrow(CuidadoNotFoundException::new);
        Categoria categoria = categoriaRepository.findById(plantaInDto.getCategoriaId()).orElseThrow(CategoriaNotFoundException::new);

        // Crea planta desde el indto
        Planta planta = modelMapper.map(plantaInDto, Planta.class);
        planta.setFechaRegistro(LocalDate.now());
        planta.setCuidado(cuidado);
        planta.setCategoria(categoria);

        asignarPlagasYConsejos(planta, plantaInDto);

        return convertToDto(plantaRepository.save(planta));
    }

    //MODIFICA PLANTA POR ID ***********
    public PlantaOutDto modify(long plantaId, @Valid @RequestBody PlantaInDto plantaInDto)
            throws PlantaNotFoundException, CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
      //busca la planta especificada en query y sino esta lanza Excep
        Planta planta = plantaRepository.findById(plantaId).orElseThrow(PlantaNotFoundException::new);

        // Validar y asignar cuidado y categoria
        planta.setCuidado(cuidadoRepository.findById(plantaInDto.getCuidadoId()).orElseThrow(CuidadoNotFoundException::new));
        planta.setCategoria(categoriaRepository.findById(plantaInDto.getCategoriaId()).orElseThrow(CategoriaNotFoundException::new));

        modelMapper.map(plantaInDto, planta);

        // Asigna plagas y consejos si los ha recibido
        asignarPlagasYConsejos(planta, plantaInDto);
        plantaRepository.save(planta);

        // Mapear la entidad modificada al DTO de salida
        PlantaOutDto plantaOutDto = modelMapper.map(planta, PlantaOutDto.class);

        // Asegurar que los IDs de plagas y consejos estén presentes en el DTO de salida
        plantaOutDto.setPlagaIds(
                planta.getPlagas() != null ?
                        planta.getPlagas().stream().map(Plaga::getIdPlaga).collect(Collectors.toList()) :
                        new ArrayList<>());

        plantaOutDto.setConsejoIds(
                planta.getConsejos() != null ?
                        planta.getConsejos().stream().map(Consejo::getIdConsejo).collect(Collectors.toList()) :
                        new ArrayList<>());

        return plantaOutDto;
    }

    //BORRA PLANTA POR ID ***********************
    public void remove (long id_planta) throws PlantaNotFoundException{

        plantaRepository.findById(id_planta).orElseThrow(PlantaNotFoundException::new);
        plantaRepository.deleteById(id_planta);
    }

}