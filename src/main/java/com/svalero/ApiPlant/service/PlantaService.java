//es la capa de la logica del proyecto

package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.PlagaOutDto;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;

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


    // MUESTRA PLANTAS CON FILTROS ***********************
    /*devuelve el array de plagas como null xq el model mapper no lo reconoce,
     */
    public List<PlantaOutDto> getAll(String genero, String especie, Boolean esToxica) {
        List<Planta> plantaList;

        boolean generoVacio = (genero == null || genero.isEmpty());
        boolean especieVacio = (especie == null || especie.isEmpty());

        if (generoVacio && especieVacio && esToxica == null) {
            plantaList = plantaRepository.findAll();
        } else if (!generoVacio && especieVacio && esToxica == null) {
            plantaList = plantaRepository.findByGeneroContainingIgnoreCase(genero);
        } else if (generoVacio && !especieVacio && esToxica == null) {
            plantaList = plantaRepository.findByEspecieContainingIgnoreCase(especie);
        } else if (generoVacio && especieVacio) {
            plantaList = esToxica ? plantaRepository.findByEsToxicaTrue() : plantaRepository.findByEsToxicaFalse();
        } else if (!generoVacio && especieVacio) {
            plantaList = esToxica ? plantaRepository.findByGeneroContainingIgnoreCaseAndEsToxicaTrue(genero)
                    : plantaRepository.findByGeneroContainingIgnoreCaseAndEsToxicaFalse(genero);
        } else if (generoVacio) {
            plantaList = esToxica ? plantaRepository.findByEspecieContainingIgnoreCaseAndEsToxicaTrue(especie)
                    : plantaRepository.findByEspecieContainingIgnoreCaseAndEsToxicaFalse(especie);
        } else if (esToxica != null) {
            plantaList = esToxica ? plantaRepository.findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaTrue(genero, especie)
                    : plantaRepository.findByGeneroContainingIgnoreCaseAndEspecieContainingIgnoreCaseAndEsToxicaFalse(genero, especie);
        } else {
            // Caso: genero y especie definidos pero esToxica == null
            // Lo manejamos aquí como caso adicional
            plantaList = plantaRepository.findAll().stream()
                    .filter(p -> p.getGenero().toLowerCase().contains(genero.toLowerCase()))
                    .filter(p -> p.getEspecie().toLowerCase().contains(especie.toLowerCase()))
                    .collect(Collectors.toList());
        }

        //para convertir las PlagaIds
        return plantaList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }

    //CONVIERTE LA LISTA DE PLAGAS EN UNA LISTA DE PLAGAIDS
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
        Planta planta = plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);

        PlantaOutDto dto = new PlantaOutDto();
        dto.setId_planta(planta.getId_planta());
        dto.setGenero(planta.getGenero());
        dto.setEspecie(planta.getEspecie());
        dto.setEsToxica(planta.getEsToxica());
        dto.setAlturaMaxima(planta.getAlturaMaxima());
        dto.setTipoCrecimiento(planta.getTipoCrecimiento());

        // Mapear ID de cuidado y categoría
        if (planta.getCuidado() != null) {
            dto.setCuidadoId(planta.getCuidado().getIdCuidado());
        }

        if (planta.getCategoria() != null) {
            dto.setCategoriaId(planta.getCategoria().getIdCategoria());
        }

        // Obtener solo los IDs de las plagas
        List<Long> plagaIds = planta.getPlagas() != null
                ? planta.getPlagas().stream()
                .map(Plaga::getIdPlaga)
                .collect(Collectors.toList())
                : new ArrayList<>();

        dto.setPlagaIds(plagaIds);

        // Obtener solo los IDs de los consejos
        List<Long> consejoIds = planta.getConsejos() != null
                ? planta.getConsejos().stream()
                .map(Consejo::getIdConsejo)
                .collect(Collectors.toList())
                : new ArrayList<>();

        dto.setConsejoIds(consejoIds);

        return dto;
    }


    // AÑADE PLANTA CON INDTO **********************************
    public PlantaOutDto add(PlantaInDto plantaInDto) throws
            CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {

        // Obtener entidades obligatorias (cuidado y categoría)
        Cuidado cuidado = cuidadoRepository.findById(plantaInDto.getCuidadoId()).orElseThrow(CuidadoNotFoundException::new);
        Categoria categoria = categoriaRepository.findById(plantaInDto.getCategoriaId()).orElseThrow(CategoriaNotFoundException::new);

        // Crear entidad planta desde el DTO
        Planta planta = modelMapper.map(plantaInDto, Planta.class);
        planta.setFechaRegistro(LocalDate.now());
        planta.setCuidado(cuidado);
        planta.setCategoria(categoria);

        // Asociar plagas si se especificaron
        if (plantaInDto.getPlagaIds() != null && !plantaInDto.getPlagaIds().isEmpty()) {
            List<Plaga> plagas = StreamSupport.stream(
                            plagaRepository.findAllById(plantaInDto.getPlagaIds()).spliterator(), false)
                    .collect(Collectors.toList());

            if (plagas.size() != plantaInDto.getPlagaIds().size())
                throw new PlagaNotFoundException();

            planta.setPlagas(plagas);
        } else {
            planta.setPlagas(new ArrayList<>());
        }

        // Asociar consejos si se especificaron
        if (plantaInDto.getConsejoIds() != null && !plantaInDto.getConsejoIds().isEmpty()) {
            List<Consejo> consejos = StreamSupport.stream(
                            consejoRepository.findAllById(plantaInDto.getConsejoIds()).spliterator(), false)
                    .collect(Collectors.toList());

            if (consejos.size() != plantaInDto.getConsejoIds().size())
                throw new ConsejoNotFoundException();

            planta.setConsejos(consejos);
        } else {
            planta.setConsejos(new ArrayList<>());
        }

        // Guardar planta y mapear a DTO de salida
        Planta newPlanta = plantaRepository.save(planta);
        PlantaOutDto plantaOutDto = modelMapper.map(newPlanta, PlantaOutDto.class);

        // Asegurar que los IDs de plagas y consejos estén en el DTO de salida
        plantaOutDto.setPlagaIds(
                newPlanta.getPlagas().stream().map(Plaga::getIdPlaga).collect(Collectors.toList()));

        plantaOutDto.setConsejoIds(
                newPlanta.getConsejos().stream().map(Consejo::getIdConsejo).collect(Collectors.toList()));

        return plantaOutDto;
    }


    //MODIFICA PLANTA POR ID ***********
    public PlantaOutDto modify(long plantaId, PlantaInDto plantaInDto) throws PlantaNotFoundException {
        // Buscar planta existente o lanzar excepción si no se encuentra
        Planta planta = plantaRepository.findById(plantaId)
                .orElseThrow(PlantaNotFoundException::new);

        // Mapear campos básicos desde el DTO de entrada a la entidad
        modelMapper.map(plantaInDto, planta);

        // Asignar plagas a la planta (si hay IDs recibidos)
        if (plantaInDto.getPlagaIds() != null) {
            List<Plaga> plagas = StreamSupport.stream(
                            plagaRepository.findAllById(plantaInDto.getPlagaIds()).spliterator(), false)
                    .collect(Collectors.toList());
            planta.setPlagas(plagas);
        }

        // Asignar consejos a la planta (si hay IDs recibidos)
        if (plantaInDto.getConsejoIds() != null) {
            List<Consejo> consejos = StreamSupport.stream(
                            consejoRepository.findAllById(plantaInDto.getConsejoIds()).spliterator(), false)
                    .collect(Collectors.toList());
            planta.setConsejos(consejos);
        }

        // Guardar la planta con las relaciones actualizadas
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
        plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
        plantaRepository.deleteById(id_planta);
    }


}