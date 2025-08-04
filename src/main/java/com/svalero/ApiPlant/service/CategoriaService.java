package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.CategoriaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CategoriaRepository;
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

import static java.util.stream.StreamSupport.*;


@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private ModelMapper modelMapper;


    //MUESTRA CATEGORIAS CON FILTROS SIN OUTDTO************************
    public List<Categoria> getAll(String nombre, Float nivelDificultad, Boolean paraPrincipiantes) throws CategoriaNotFoundException {
        List<Categoria> categoriaList;

        boolean nombreVacio = (nombre == null || nombre.isEmpty());

        if (nombreVacio && nivelDificultad == null && paraPrincipiantes == null) {
            categoriaList = categoriaRepository.findAll();
        } else if (!nombreVacio && nivelDificultad == null && paraPrincipiantes == null) {
            categoriaList = categoriaRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (nombreVacio && nivelDificultad != null && paraPrincipiantes == null) {
            categoriaList = categoriaRepository.findByNivelDificultad(nivelDificultad);
        } else if (nombreVacio && nivelDificultad == null) {
            categoriaList = categoriaRepository.findByParaPrincipiantes(paraPrincipiantes);
        } else if (!nombreVacio && nivelDificultad != null && paraPrincipiantes == null) {
            categoriaList = categoriaRepository.findByNombreContainingIgnoreCaseAndNivelDificultad(nombre, nivelDificultad);
        } else if (!nombreVacio && nivelDificultad == null) {
            categoriaList = categoriaRepository.findByNombreContainingIgnoreCaseAndParaPrincipiantes(nombre, paraPrincipiantes);
        } else if (!nombreVacio) {
            categoriaList = categoriaRepository.findByNombreContainingIgnoreCaseAndNivelDificultadAndParaPrincipiantes(
                    nombre, nivelDificultad, paraPrincipiantes);
        } else {
            categoriaList = categoriaRepository.findByNivelDificultadAndParaPrincipiantes(nivelDificultad, paraPrincipiantes);
        }

        if (categoriaList.isEmpty()) { throw new CategoriaNotFoundException(); }
        return modelMapper.map(categoriaList, new TypeToken<List<Categoria>>() {}.getType());
    }


    //MUESTRA CATEGORIA POR ID CON OUTDTO *****************
    public CategoriaOutDto get(long idCategoria)throws CategoriaNotFoundException {
       Categoria categoria = categoriaRepository.findById(idCategoria)
               .orElseThrow(CategoriaNotFoundException::new);

       CategoriaOutDto dto = new CategoriaOutDto();
       dto.setIdCategoria(categoria.getIdCategoria());
       dto.setNombre(categoria.getNombre());
       dto.setDescripcion(categoria.getDescripcion());
       dto.setNivelDificultad(categoria.getNivelDificultad());
       dto.setParaPrincipiantes(categoria.isParaPrincipiantes());

        // Aquí obtienes los IDs de plantas asociadas
        List<Long> plantaIds = categoria.getPlantas().stream()
                .map(Planta::getId_planta)
                .collect(Collectors.toList());

        dto.setPlantaIds(plantaIds);

        return dto;

    }


    //AÑADE CATEGORIA CON INDTO *****************
    public CategoriaOutDto addCategoria(CategoriaInDto dto) {
        Categoria categoria = modelMapper.map(dto, Categoria.class);
        categoria.setFechaRegistro(LocalDate.now());

        Categoria saved = categoriaRepository.save(categoria);
        return modelMapper.map(saved, CategoriaOutDto.class);

    }


    // MODIFICA CATEGORIA POR ID  CON REVISION DE OCNFLICTO POR PLANTA**********************
    public CategoriaOutDto modify(long idCategoria, CategoriaInDto categoriaInDto) throws CategoriaNotFoundException, CategoriaConflictException {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(CategoriaNotFoundException::new);

        //recupero los actuales plantaIds asociados
        List<Long> actualesIds = Optional.ofNullable(categoria.getPlantas())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Planta::getId_planta)
                .toList();

        //agrupo los nuevos plantaIds
        List<Long> nuevosIds = Optional.ofNullable(categoriaInDto.getPlantaIds())
                .orElseGet(Collections::emptyList);

        // Reviso los nuevos plantaIds para ver si hay conflicto y lanzo Excep
        if (actualesIds.stream().anyMatch(id -> !nuevosIds.contains(id))) { throw new CategoriaConflictException(); }

        // Incluye los nuevos plantaIds a esa cetagoria
        List<Long> idsParaAnadir = nuevosIds.stream()
                .filter(id -> !actualesIds.contains(id))
                .toList();

        if (!idsParaAnadir.isEmpty()) {
            List<Planta> plantasParaAnadir = stream(plantaRepository.findAllById(idsParaAnadir).spliterator(), false)
                    .toList();
            if (categoria.getPlantas() == null) categoria.setPlantas(new ArrayList<>());
            categoria.getPlantas().addAll(plantasParaAnadir);
        }

        modelMapper.map(categoriaInDto, categoria);
        categoriaRepository.save(categoria);

        CategoriaOutDto outDto = modelMapper.map(categoria, CategoriaOutDto.class);
        outDto.setPlantaIds(Optional.ofNullable(categoria.getPlantas())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Planta::getId_planta)
                .toList());

        return outDto;
    }


    //BORRA CATEGORIA CON REVISION DE CONFLICTO CON PLANTA ****************************
    public void remove(Long idCategoria) throws CategoriaNotFoundException, CategoriaConflictException {

        categoriaRepository.findById(idCategoria).orElseThrow(CategoriaNotFoundException::new);
        List<Planta> plantasConCategoria = plantaRepository.findByCategoria_IdCategoria(idCategoria);

        if (!plantasConCategoria.isEmpty()) { throw new CategoriaConflictException(); }

        categoriaRepository.deleteById(idCategoria);
    }


}
