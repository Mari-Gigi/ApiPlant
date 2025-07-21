//es la capa de la logica del proyecto

package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlantaService {

    @Autowired
    private PlantaRepository plantaRepository; //para que pueda acceder a Repository
    @Autowired
    private CuidadoRepository cuidadoRepository;
    @Autowired
    private ModelMapper modelMapper;  //para los Dto

// MUESTRA LAS PLANTAS CON DTOS
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

    return modelMapper.map(plantaList, new TypeToken<List<PlantaOutDto>>() {}.getType());
}


    //MUESTRA LA PLANTA POR ID
    public Planta get(long id_planta)throws PlantaNotFoundException{ //Busca el id_planta. Si esta, muéstralo, y si no, lanza excepción.
        return plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
    }


    public PlantaOutDto add(PlantaInDto plantaInDto) throws CuidadoNotFoundException {
        Long cuidadoId = plantaInDto.getCuidadoId();

        if (cuidadoId == null) {
            throw new CuidadoNotFoundException("El cuidadoId es obligatorio");
        }

        Cuidado cuidado = cuidadoRepository.findById(cuidadoId)
                .orElseThrow(CuidadoNotFoundException::new);

        Planta planta = modelMapper.map(plantaInDto, Planta.class);
        planta.setFechaRegistro(LocalDate.now());
        planta.setCuidado(cuidado);

        Planta newPlanta = plantaRepository.save(planta);
        return modelMapper.map(newPlanta, PlantaOutDto.class);
    }


    //MODIFICA PLANTA POR ID
    public PlantaOutDto modify (long plantaId, PlantaInDto plantaInDto) throws PlantaNotFoundException {
        Planta planta = plantaRepository.findById(plantaId)
            .orElseThrow(PlantaNotFoundException::new);

        modelMapper.map(plantaInDto, planta);
        plantaRepository.save(planta);

        return modelMapper.map(planta, PlantaOutDto.class);

}

    //BORRA PLANTA POR ID
    public void remove (long id_planta) throws PlantaNotFoundException{  //Busca el id_planta. Si esta, bórralo, y si no, lanza excepción.
        plantaRepository.findById(id_planta)
                .orElseThrow(PlantaNotFoundException::new);
        plantaRepository.deleteById(id_planta);
    }


}
