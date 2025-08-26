package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.PlagaService;
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
public class PlagaController {

    @Autowired
    private PlagaService plagaService;

    private static final Logger logger = LoggerFactory.getLogger(PlagaController.class);


    @GetMapping("/plagas")
    public ResponseEntity<List<Plaga>> getAll(
            @RequestParam(value = "nombre", defaultValue = "") String nombre,
            @RequestParam(value = "riesgo", required = false) Float riesgo,
            @RequestParam(value = "esLetal", required = false) Boolean esLetal,  /*la Exc que devuelve es de tipo 404 xq no convierte bien el float*/
            @RequestParam Map<String, String> allParams) throws PlagaNotFoundException {

        logger.info("BEGIN getAll");
        Set<String> validParams = Set.of("nombre", "riesgo", "esLetal");
        for (String param : allParams.keySet()) {
            if (!validParams.contains(param)) {
                throw new InvalidParameterException("Parámetro inválido: " + param);
            }
        }

        logger.info("END getAll");
        return new ResponseEntity<>(plagaService.getAll(nombre, riesgo, esLetal), HttpStatus.OK);
    }

    @GetMapping("/plagas/{plagaId}")
    public ResponseEntity <PlagaOutDto> getPlaga(@PathVariable long plagaId) throws PlagaNotFoundException {
        logger.info("BEGIN getById");
        PlagaOutDto plagaOutDto = plagaService.get(plagaId);
        logger.info("END getById");
        return new ResponseEntity<>(plagaOutDto, HttpStatus.OK);
    }


    @PostMapping("/plagas")
    public ResponseEntity<Plaga> addPlaga(@Valid @RequestBody PlagaInDto plagaInDto) throws PlantaNotFoundException {
        logger.info("BEGIN addPlaga");
        Plaga plaga = plagaService.add(plagaInDto);
        logger.info("END addPlaga");
        return ResponseEntity.status(HttpStatus.CREATED).body(plagaService.add(plagaInDto));

    }


    @PutMapping("/plagas/{plagaId}")
    public ResponseEntity<PlagaOutDto> modifyPlaga(@PathVariable long plagaId, @Valid @RequestBody PlagaInDto plaga) throws  PlagaNotFoundException, PlantaNotFoundException {
        logger.info("BEGIN putPlaga");
        PlagaOutDto modifiedPlaga = plagaService.modify(plagaId, plaga);
        logger.info("END putPlaga");
        return new ResponseEntity<>(modifiedPlaga, HttpStatus.OK);
    }

    @DeleteMapping("/plagas/{plagaId}")
    public ResponseEntity<Void> removePlaga(@PathVariable long plagaId) throws PlagaConflictException, PlagaNotFoundException {
        logger.info("BEGIN deletePlaga");
        plagaService.remove(plagaId);
        logger.info("END deletePlaga");
        return ResponseEntity.noContent().build();
    }


    //CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler  (PlagaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlagaNotFoundException(PlagaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Plaga no encontrada con esos parámetros");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlagaConflictException.class)
    public ResponseEntity<ErrorResponse> handlePlagaNotFoundException(PlagaConflictException ex) {
        ErrorResponse error = ErrorResponse.generalError(409, "Plaga asociada a una planta.");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler (PlantaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlantaNotFoundException(PlantaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
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
