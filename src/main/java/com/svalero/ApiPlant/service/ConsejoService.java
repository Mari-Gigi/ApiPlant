package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.ConsejoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    //MUESTRA CONSEJOS CON FILTROS SIN OUTDTO************************
    public List<Consejo> getAll(String titulo, Boolean verificado, Float importancia) throws ConsejoNotFoundException{
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

        if (consejoList.isEmpty()) { throw new ConsejoNotFoundException(); }

        return consejoList;
    }


    //MUESTRA CONSEJO POR ID CON OUTDTO *****************
    public ConsejoOutDto get(long idConsejo)throws ConsejoNotFoundException {
        Consejo consejo = consejoRepository.findById(idConsejo).orElseThrow(ConsejoNotFoundException::new);

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
    public Consejo add(ConsejoInDto consejoInDto) throws PlantaNotFoundException{

        Consejo consejo= modelMapper.map(consejoInDto, Consejo.class);
        consejo.setFechaRegistro(LocalDate.now());

        // Verificar que todas las plantas existen si se enviaron IDs
        if (consejoInDto.getPlantaIds() != null && !consejoInDto.getPlantaIds().isEmpty()) {
            List<Planta> plantas = StreamSupport
                    .stream(plantaRepository.findAllById(consejoInDto.getPlantaIds()).spliterator(), false)
                    .toList();

            if (plantas.size() != consejoInDto.getPlantaIds().size()) {
                // Detectar qué IDs no existen
                List<Long> existentes = plantas.stream().map(Planta::getId_planta).toList();
                List<Long> faltantes = consejoInDto.getPlantaIds().stream()
                        .filter(id -> !existentes.contains(id))
                        .toList();
                throw new PlantaNotFoundException("No se encontraron plantas con IDs: " + faltantes);
            }

            // Asignar plantas a la plaga
            consejo.setPlantas(plantas);
        }
        return consejoRepository.save(consejo);

    }


    // MODIFICA CONSEJO POR ID CON OUTDTO *******************
    public ConsejoOutDto modify(long idConsejo, ConsejoInDto dto) throws ConsejoNotFoundException, PlantaNotFoundException {
        // Buscar el consejo por ID o lanzar excepción
        Consejo consejo = consejoRepository.findById(idConsejo).orElseThrow(ConsejoNotFoundException::new);

        // Copiar los campos del DTO a la entidad Consejo
        modelMapper.map(dto, consejo);

        // Eliminar relaciones antiguas con plantas (bidireccional)
        if (consejo.getPlantas() != null)
            consejo.getPlantas().forEach(p -> p.getConsejos().remove(consejo));

        // Obtener la lista de plantaIds enviada (si existe)
        List<Long> plantaIds = Optional.ofNullable(dto.getPlantaIds()).orElse(List.of());

        // Cargar nuevas plantas desde BD y asociarlas al consejo
        List<Planta> nuevasPlantas = StreamSupport
                .stream(plantaRepository.findAllById(plantaIds).spliterator(), false)
                .peek(p -> p.getConsejos().add(consejo)) // Relación bidireccional
                .collect(Collectors.toList());

        // Verificar si todos los plantaIds existen
        if (nuevasPlantas.size() != plantaIds.size()) {
            List<Long> encontrados = nuevasPlantas.stream().map(Planta::getId_planta).toList();
            List<Long> faltantes = plantaIds.stream().filter(id -> !encontrados.contains(id)).toList();
            throw new PlantaNotFoundException("No se encontraron plantas con IDs: " + faltantes);
        }

        // Asociar las nuevas plantas al consejo
        consejo.setPlantas(nuevasPlantas);

        // Guardar los cambios
        consejoRepository.save(consejo);

        // Convertir a DTO de salida y añadir los IDs de planta
        ConsejoOutDto out = modelMapper.map(consejo, ConsejoOutDto.class);
        out.setPlantaIds(nuevasPlantas.stream().map(Planta::getId_planta).toList());

        return out;
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
