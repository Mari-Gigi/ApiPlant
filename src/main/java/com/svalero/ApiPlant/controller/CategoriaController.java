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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(CategoriaController.class);


    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> getAll(
            @RequestParam(value = "nombre", defaultValue = "") String nombre,
            @RequestParam(value = "nivelDificultad", required = false) Float nivelDificultad, /*la Exc que devuelve es de tipo 404 xq no convierte bien el float*/
            @RequestParam(value = "paraPrincipiantes", required = false) Boolean paraPrincipiantes,
            @RequestParam Map<String, String> allParams) throws CategoriaNotFoundException {

        logger.info("BEGIN getAll");

        Set<String> validParams = Set.of("nombre", "nivelDificultad", "paraPrincipiantes");
        for (String param : allParams.keySet()) {
            if (!validParams.contains(param)) {
                throw new InvalidParameterException("Parámetro inválido: " + param);
            }
        }
        logger.info("END getAll");
        return new ResponseEntity<>(categoriaService.getAll(nombre, nivelDificultad, paraPrincipiantes), HttpStatus.OK);
    }


    @GetMapping("/categorias/:categoriaId")
    public ResponseEntity <CategoriaOutDto> getCategoria(long categoriaId) throws CategoriaNotFoundException {
        logger.info("BEGIN getById");
        CategoriaOutDto categoriaOutDto = categoriaService.get(categoriaId);
        logger.info("END getById");
        return new ResponseEntity<>(categoriaOutDto, HttpStatus.OK);
    }


    @PostMapping ("/categorias")
    public ResponseEntity <CategoriaOutDto> addCategoria (@Valid @RequestBody CategoriaInDto categoriaInDto) {
        logger.info("BEGIN postCategoria");
        CategoriaOutDto newCategoria = categoriaService.addCategoria(categoriaInDto);
        logger.info("END postCategoria");
        return new ResponseEntity<>(newCategoria, HttpStatus.CREATED);
    }


    @PutMapping ("/categorias/:categoriaId")
    public ResponseEntity<CategoriaOutDto> modifyCategoria (long categoriaId, @Valid @RequestBody CategoriaInDto categoria)
            throws  CategoriaNotFoundException, CategoriaConflictException {
        logger.info("BEGIN putCategoria");
        CategoriaOutDto modifiedCategoria = categoriaService.modify(categoriaId, categoria);
        logger.info("END putCategoria");
        return new ResponseEntity<>(modifiedCategoria, HttpStatus.NOT_FOUND);
    }


    @DeleteMapping ("/categorias/:categoriaId")
    public ResponseEntity<Void> deleteCategoria(long categoriaId) throws CategoriaNotFoundException, CategoriaConflictException {
        logger.info("BEGIN deleteCategoria");
       categoriaService.remove(categoriaId);
        logger.info("END deleteCategoria");
       return  ResponseEntity.noContent().build();
    }




//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler  (CategoriaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoriaNotFoundException(CategoriaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404,"Categoría no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoriaConflictException.class)
    public ResponseEntity<ErrorResponse> handleCategoriaConflictException(CategoriaConflictException ex) {
        ErrorResponse error = ErrorResponse.generalError(409, "Categoría asociada a una planta.");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler (PlantaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlantaNotFoundException(PlantaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Planta no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
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

    // 400 para cuerpos (json) mal especificados
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.generalError(400, "Json inválido o mal especificado.");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 400 para parametros mal especificados (query)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String message = String.format("El parámetro '%s' tiene un valor inválido: %s", paramName, ex.getValue());
        ErrorResponse error = ErrorResponse.generalError(400, message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 500 (meter el nombre del query parametro mal y peta)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        ErrorResponse error = ErrorResponse.generalError(500, "Error interno del servidor.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}


