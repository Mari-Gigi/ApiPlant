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
    public PlantaOutDto add(PlantaInDto plantaInDto) throws CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
        // Extraer IDs
        Long cuidadoId = plantaInDto.getCuidadoId();
        Long categoriaId = plantaInDto.getCategoriaId();
        List<Long> plagaIds = plantaInDto.getPlagaIds();
        List<Long> consejoIds = plantaInDto.getConsejoIds();

        // Obtener entidades obligatorias
        Cuidado cuidado = cuidadoRepository.findById(cuidadoId).orElseThrow(CuidadoNotFoundException::new);
        Categoria categoria = categoriaRepository.findById(categoriaId).orElseThrow(CategoriaNotFoundException::new);

        // Crear entidad Planta
        Planta planta = modelMapper.map(plantaInDto, Planta.class);
        planta.setFechaRegistro(LocalDate.now());
        planta.setCuidado(cuidado);
        planta.setCategoria(categoria);

        // Plagas: si se especificaron
        if (plagaIds != null && !plagaIds.isEmpty()) {
            Iterable<Plaga> iterablePlagas = plagaRepository.findAllById(plagaIds);
            List<Plaga> plagas = StreamSupport.stream(iterablePlagas.spliterator(), false)
                    .collect(Collectors.toList());

            if (plagas.size() != plagaIds.size()) {
                throw new PlagaNotFoundException();
            }

            planta.setPlagas(plagas);
        } else {
            planta.setPlagas(new ArrayList<>());
        }

        // Consejos: si se especificaron
        if (consejoIds != null && !consejoIds.isEmpty()) {
            Iterable<Consejo> iterableConsejos = consejoRepository.findAllById(consejoIds);
            List<Consejo> consejos = StreamSupport.stream(iterableConsejos.spliterator(), false)
                    .collect(Collectors.toList());

            if (consejos.size() != consejoIds.size()) {
                throw new ConsejoNotFoundException();
            }

            planta.setConsejos(consejos);
        } else {
            planta.setConsejos(new ArrayList<>());
        }

        // Guardar entidad
        Planta newPlanta = plantaRepository.save(planta);

        // Mapear manualmente a PlantaOutDto
        PlantaOutDto plantaOutDto = new PlantaOutDto();
        plantaOutDto.setId_planta(newPlanta.getId_planta());
        plantaOutDto.setGenero(newPlanta.getGenero());
        plantaOutDto.setEspecie(newPlanta.getEspecie());
        plantaOutDto.setEsToxica(newPlanta.getEsToxica());
        plantaOutDto.setAlturaMaxima(newPlanta.getAlturaMaxima());
        plantaOutDto.setTipoCrecimiento(newPlanta.getTipoCrecimiento());
        plantaOutDto.setCuidadoId(newPlanta.getCuidado().getIdCuidado());
        plantaOutDto.setCategoriaId(newPlanta.getCategoria().getIdCategoria());

        // Mapear plagaIds
        List<Long> mappedPlagaIds = newPlanta.getPlagas().stream()
                .map(Plaga::getIdPlaga)
                .collect(Collectors.toList());
        plantaOutDto.setPlagaIds(mappedPlagaIds);

        // Mapear consejoIds
        List<Long> mappedConsejoIds = newPlanta.getConsejos().stream()
                .map(Consejo::getIdConsejo)
                .collect(Collectors.toList());
        plantaOutDto.setConsejoIds(mappedConsejoIds);

        return plantaOutDto;
    }



  /*  //MODIFICA PLANTA***********  REVISAR ***************************************************
    public PlantaOutDto modify (long plantaId, PlantaInDto plantaInDto) throws PlantaNotFoundException {
        Planta planta = plantaRepository.findById(plantaId)
            .orElseThrow(PlantaNotFoundException::new);

        modelMapper.map(plantaInDto, planta);
        plantaRepository.save(planta);

        return modelMapper.map(planta, PlantaOutDto.class);

}*/

    public PlantaOutDto modify(long id, PlantaInDto plantaInDto)
            throws PlantaNotFoundException, CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {

        Planta existingPlanta = plantaRepository.findById(id)
                .orElseThrow(PlantaNotFoundException::new);

        // Map fields from DTO
        existingPlanta.setGenero(plantaInDto.getGenero());
        existingPlanta.setEspecie(plantaInDto.getEspecie());
        existingPlanta.setAlturaMaxima(plantaInDto.getAlturaMaxima());
        existingPlanta.setTipoCrecimiento(plantaInDto.getTipoCrecimiento());
        existingPlanta.setEsToxica(plantaInDto.getEsToxica());

        // Relaciones
        Cuidado cuidado = cuidadoRepository.findById(plantaInDto.getCuidadoId())
                .orElseThrow(CuidadoNotFoundException::new);
        existingPlanta.setCuidado(cuidado);

        Categoria categoria = categoriaRepository.findById(plantaInDto.getCategoriaId())
                .orElseThrow(CategoriaNotFoundException::new);
        existingPlanta.setCategoria(categoria);

        // Plagas
        if (plantaInDto.getPlagaIds() != null) {
            List<Plaga> plagas = StreamSupport.stream(
                    plagaRepository.findAllById(plantaInDto.getPlagaIds()).spliterator(), false
            ).collect(Collectors.toList());

            if (plagas.size() != plantaInDto.getPlagaIds().size()) {
                throw new PlagaNotFoundException();
            }
            existingPlanta.setPlagas(plagas);
        } else {
            existingPlanta.setPlagas(new ArrayList<>());
        }

        // Consejos
        if (plantaInDto.getConsejoIds() != null) {
            List<Consejo> consejos = StreamSupport.stream(
                    consejoRepository.findAllById(plantaInDto.getConsejoIds()).spliterator(), false
            ).collect(Collectors.toList());

            if (consejos.size() != plantaInDto.getConsejoIds().size()) {
                throw new ConsejoNotFoundException();
            }
            existingPlanta.setConsejos(consejos);
        } else {
            existingPlanta.setConsejos(new ArrayList<>());
        }

        // Guardar cambios
        plantaRepository.save(existingPlanta);

        return convertToDto(existingPlanta); // usa el mismo método que en get
    }



    //BORRA PLANTA POR ID ***********************
    public void remove (long id_planta) throws PlantaNotFoundException{
        plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
        plantaRepository.deleteById(id_planta);
    }


}