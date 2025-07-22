//es la capa de la logica del proyecto

package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.CategoriaNotFoundException;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlagaNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlagaRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.repository.CategoriaRepository;

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
    private ModelMapper modelMapper;  //para los Dto


    // MUESTRA PLANTAS CON FILTROS ***********************
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

        return modelMapper.map(plantaList, new TypeToken<List<PlantaOutDto>>() {
        }.getType());
    }

    //DEVUELVE PLANTA CON OUTDTO ******************************
    public Planta get(long id_planta) throws PlantaNotFoundException {
        return plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
    }


    //AÑADE PLANTA CON INDTO **********************************
    public PlantaOutDto add(PlantaInDto plantaInDto) throws CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException {
        // Extraer IDs
        Long cuidadoId = plantaInDto.getCuidadoId();
        Long categoriaId = plantaInDto.getCategoriaId();
        List<Long> plagaIds = plantaInDto.getPlagaIds();

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
            planta.setPlagas(new ArrayList<>()); // No se especificaron plagas
        }

        // Guardar y devolver
        Planta newPlanta = plantaRepository.save(planta);
        return modelMapper.map(newPlanta, PlantaOutDto.class);
    }



    //MODIFICA PLANTA***********  REVISAR ***************************************************
    public PlantaOutDto modify (long plantaId, PlantaInDto plantaInDto) throws PlantaNotFoundException {
        Planta planta = plantaRepository.findById(plantaId)
            .orElseThrow(PlantaNotFoundException::new);

        modelMapper.map(plantaInDto, planta);
        plantaRepository.save(planta);

        return modelMapper.map(planta, PlantaOutDto.class);

}

    //BORRA PLANTA POR ID ***********************
    public void remove (long id_planta) throws PlantaNotFoundException{
        plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
        plantaRepository.deleteById(id_planta);
    }


}