package com.svalero.ApiPlant.controller;
import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.service.CuidadoService;
import com.svalero.ApiPlant.service.PlantaService;

import jakarta.validation.Valid;
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
public class CuidadoController {

    @Autowired
    private CuidadoService cuidadoService;
    @Autowired
    private CuidadoRepository cuidadoRepository;
    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private PlantaService plantaService;


    @GetMapping("/cuidados")
    public ResponseEntity<List<Cuidado>> getAll(
            @RequestParam(value = "riego", defaultValue = "") String riego,
            @RequestParam(value = "sustrato", defaultValue = "") String sustrato,
            @RequestParam(value = "esInterior", required = false) Boolean esInterior,
            @RequestParam Map<String, String> allParams) throws CuidadoNotFoundException {

        Set<String> validParams = Set.of("riego", "sustrato", "esInterior");
        for (String param : allParams.keySet()) {
            if (!validParams.contains(param)) {
                throw new InvalidParameterException("Parámetro inválido: " + param);
            }
        }

        return new ResponseEntity<>(cuidadoService.getAll(riego, sustrato, esInterior), HttpStatus.OK);
    }


    @GetMapping("/cuidados/:cuidadoId")
    public ResponseEntity<CuidadoOutDto> getCuidado(long cuidadoId) throws CuidadoNotFoundException {
        CuidadoOutDto cuidadoOutDto = cuidadoService.get(cuidadoId);
        return new ResponseEntity<>(cuidadoOutDto, HttpStatus.OK);
    }


    @PostMapping("/cuidados")
    public ResponseEntity<CuidadoOutDto> addCuidado(@Valid @RequestBody CuidadoInDto cuidadoInDto) throws PlantaNotFoundException {
        CuidadoOutDto nuevoCuidado = cuidadoService.addCuidado(cuidadoInDto);
        return new ResponseEntity<>(nuevoCuidado, HttpStatus.CREATED);
    }


    @PutMapping("/cuidados/:cuidadoId")
    public ResponseEntity<CuidadoOutDto> modifyCuidado(long cuidadoId, @Valid @RequestBody CuidadoInDto cuidado) throws CuidadoNotFoundException, CuidadoConflictException {
        CuidadoOutDto modifiedCuidado = cuidadoService.modify(cuidadoId, cuidado);
        return new ResponseEntity<>(modifiedCuidado, HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/cuidados/{cuidadoId}")
    public ResponseEntity<Void> deleteCuidado(Long cuidadoId) throws CuidadoNotFoundException, CuidadoConflictException {
        cuidadoService.remove(cuidadoId);
        return ResponseEntity.noContent().build();
    }




//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler  (CuidadoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCuidadoNotFoundException(CuidadoNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, "Cuidado no encontrada con esos parámetros.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(CuidadoConflictException.class)
    public ResponseEntity<ErrorResponse> handleCuidadoConflictException(CuidadoConflictException ex) {
        ErrorResponse error = ErrorResponse.generalError(409, "Cuidado asociado a una planta.");
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





