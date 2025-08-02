package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
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
public class PlantaController {


    @Autowired
    private PlantaService plantaService;
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(PlantaController.class);

    @GetMapping("/plantas")
    public ResponseEntity<List<PlantaOutDto>> getAll(
            @RequestParam(value = "genero", defaultValue = "") String genero,
            @RequestParam(value = "especie", defaultValue = "") String especie,
            @RequestParam(value = "esToxica", required = false) Boolean esToxica,
            @RequestParam Map<String, String> allParams) throws PlantaNotFoundException {

        logger.info("BEGIN getAll");

            Set<String> validParams = Set.of("genero", "especie", "esToxica");
            for (String param : allParams.keySet()) {
                if (!validParams.contains(param)) {
                    throw new InvalidParameterException("Parámetro inválido: " + param);
                }
            }

        logger.info("END getAll");
        return new ResponseEntity<>(plantaService.getAll(genero, especie, esToxica), HttpStatus.OK);
    }



    @GetMapping("/plantas/:plantaId")
    public ResponseEntity <PlantaOutDto> getPlanta(long plantaId) throws PlantaNotFoundException {
        logger.info("BEGIN getById");
        PlantaOutDto plantaOutDto = plantaService.get(plantaId);
        logger.info("END getById");
        return new ResponseEntity<>(plantaOutDto, HttpStatus.OK);
    }


    @PostMapping("/plantas")
    public ResponseEntity<PlantaOutDto> addPlanta(@Valid @RequestBody PlantaInDto plantaInDto)
            throws CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
        logger.info("BEGIN addPlanta");
        PlantaOutDto newPlanta = plantaService.add(plantaInDto);
        logger.info("END addPlanta");
        return new ResponseEntity<>(newPlanta, HttpStatus.CREATED);
    }


    @PutMapping("/plantas/:plantaId")
    public ResponseEntity<PlantaOutDto> modifyPlanta(long plantaId, @Valid @RequestBody PlantaInDto plantaInDto)
            throws PlantaNotFoundException, CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
        logger.info("BEGIN putPlanta");
        PlantaOutDto updatedPlanta = plantaService.modify(plantaId, plantaInDto);
        logger.info("END putPlanta");
        return ResponseEntity.ok(updatedPlanta);
    }


    @DeleteMapping ("/plantas/:plantaId")
    public ResponseEntity <Void> removePlanta (long plantaId) throws PlantaNotFoundException {
        logger.info("BEGIN deletePlanta");
        plantaService.remove(plantaId);
        logger.info("END deletePlanta");
        return ResponseEntity.noContent().build();
    }


//CONTROL DE EXCEPCIONES ****************

    @ExceptionHandler (PlantaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlantaNotFoundException(PlantaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Planta no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (CuidadoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCuidadoNotFoundException(CuidadoNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Cuidado no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (CategoriaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoriaNotFoundException(CategoriaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404,"Categoría no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (PlagaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlagaNotFoundException(PlagaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Plaga no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (ConsejoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsejoNotFoundException(ConsejoNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 400 para especificaciones que no se cumplen en los dto (los @NotNull)
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
        String message = String.format("El parámetro '%s' tiene un valor inválido: %s", paramName, ex.getValue()); //si hay booleano y pongo kjsdhfjk
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
