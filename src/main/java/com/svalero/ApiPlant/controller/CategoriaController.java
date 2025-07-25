package com.svalero.ApiPlant.controller;
import com.svalero.ApiPlant.domain.Categoria;
import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CategoriaRepository;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.service.CategoriaService;
import com.svalero.ApiPlant.service.CuidadoService;
import com.svalero.ApiPlant.service.PlantaService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private PlantaService plantaService;


    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getAll(
            @RequestParam(value = "nombre", defaultValue = "") String nombre,
            @RequestParam(value = "nivelDificultad", required = false) Float nivelDificultad,
            @RequestParam(value = "paraPrincipiantes", required = false) Boolean paraPrincipiantes) {

        return new ResponseEntity<>(categoriaService.getAll(nombre, nivelDificultad, paraPrincipiantes), HttpStatus.OK);
    }


    @GetMapping("/categorias/:categoriaId")
    public ResponseEntity <CategoriaOutDto> getCategoria(long categoriaId) throws CategoriaNotFoundException {
        CategoriaOutDto categoriaOutDto = categoriaService.get(categoriaId);
        return new ResponseEntity<>(categoriaOutDto, HttpStatus.OK);
    }


    @PostMapping ("/categorias")
    public ResponseEntity <CategoriaOutDto> addCategoria (@RequestBody CategoriaInDto categoriaInDto) {
        CategoriaOutDto newCategoria = categoriaService.addCategoria(categoriaInDto);
        return new ResponseEntity<>(newCategoria, HttpStatus.CREATED);
    }


    @PutMapping ("/categorias/:categoriaId")
    public ResponseEntity<CategoriaOutDto> modifyCategoria (long categoriaId, @Valid @RequestBody CategoriaInDto categoria)
            throws  CategoriaNotFoundException, CategoriaConflictException {
        CategoriaOutDto modifiedCategoria = categoriaService.modify(categoriaId, categoria);
        return new ResponseEntity<>(modifiedCategoria, HttpStatus.NOT_FOUND);
    }


    @DeleteMapping ("/categorias/:categoriaId")
    public ResponseEntity<Void> deleteCategoria(long categoriaId) {
        try {
            categoriaService.remove(categoriaId);
            return ResponseEntity.noContent().build();
        } catch (CategoriaConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (CategoriaNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(CategoriaNotFoundException.class)
    public ResponseEntity<String> handleCategoriaNotFoundException(CategoriaNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoriaConflictException.class)
    public ResponseEntity<String> handleCategoriaConflictException(CategoriaConflictException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // ← Aquí el 409
    }

    @ExceptionHandler(PlantaNotFoundException.class)
    public ResponseEntity<String> handlePlantaNotFoundException(PlantaNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> MethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return new ResponseEntity<>(ErrorResponse.validationError(errors), HttpStatus.BAD_REQUEST);
    }

}


