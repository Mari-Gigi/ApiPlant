package com.svalero.ApiPlant.service;

import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.CategoriaOutDto;
import com.svalero.ApiPlant.exception.CategoriaConflictException;
import com.svalero.ApiPlant.exception.CategoriaNotFoundException;
import com.svalero.ApiPlant.exception.CuidadoConflictException;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.repository.CategoriaRepository;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class CategoriaService {

    @Autowired
    CategoriaRepository categoriaRepository;
    @Autowired
    private CuidadoRepository cuidadoRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<CategoriaOutDto> getAll(String nombre, Float nivelDificultad, Boolean paraPrincipiantes) {
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

        return modelMapper.map(categoriaList, new TypeToken<List<CategoriaOutDto>>() {}.getType());
    }

    public Categoria get(long idCategoria)throws CategoriaNotFoundException {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(CategoriaNotFoundException::new);
    }

    public Categoria add(Categoria categoria) {
        categoria.setFechaRegistro(LocalDate.now());
        return categoriaRepository.save(categoria);

    }

    // MODIFICA CUIDADO POR ID
    public CategoriaOutDto modify(long idCategoria, CategoriaInDto categoriaInDto) throws CategoriaNotFoundException {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(CategoriaNotFoundException::new);

        modelMapper.map(categoriaInDto, categoria);
        categoriaRepository.save(categoria);

        return modelMapper.map(categoria, CategoriaOutDto.class);
    }


    public void remove(Long idCategoria) throws CategoriaNotFoundException, CategoriaConflictException {
        categoriaRepository.findById(idCategoria)
                .orElseThrow(CategoriaNotFoundException::new);

        List<Planta> plantasConCategoria = plantaRepository.findByCategoria_IdCategoria(idCategoria);

        if (!plantasConCategoria.isEmpty()) {
            throw new CategoriaConflictException("No se puede eliminar la categoria, está en uso por una o más plantas.");
        }

        categoriaRepository.deleteById(idCategoria);
    }

}
