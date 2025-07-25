package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.service.PlantaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PlantaController {


    @Autowired
    private PlantaService plantaService;
   /* @Autowired
    private final Logger logger = LoggerFactory.getLogger(PlantaController.class);*/
    /*añadir en cada enppoint ellogger.*/

    @GetMapping("/plantas")
    public ResponseEntity<List<PlantaOutDto>> getAll(
            @RequestParam(value = "genero", defaultValue = "") String genero,
            @RequestParam(value = "especie", defaultValue = "") String especie,
            @RequestParam(value = "esToxica", required = false) Boolean esToxica) {

            return new ResponseEntity<>(plantaService.getAll(genero, especie, esToxica), HttpStatus.OK);
     }

    @GetMapping("/plantas/:plantaId")  //recoge la planta por id y devuelve Ok si la encuentra
    public ResponseEntity <PlantaOutDto> getPlanta(long plantaId) throws PlantaNotFoundException {
        PlantaOutDto plantaOutDto = plantaService.get(plantaId);
        return new ResponseEntity<>(plantaOutDto, HttpStatus.OK);
    }


    @PostMapping("/plantas")
    public ResponseEntity<PlantaOutDto> addPlanta(@Valid @RequestBody PlantaInDto plantaInDto)
            throws CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
        PlantaOutDto newPlanta = plantaService.add(plantaInDto);
        return new ResponseEntity<>(newPlanta, HttpStatus.CREATED);
    }


    @PutMapping("/plantas/:plantaId")
    public ResponseEntity<PlantaOutDto> modifyPlanta(long plantaId, @RequestBody @Valid PlantaInDto plantaInDto)
            throws PlantaNotFoundException, CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {
        PlantaOutDto updatedPlanta = plantaService.modify(plantaId, plantaInDto);
        return ResponseEntity.ok(updatedPlanta);
    }



    @DeleteMapping ("/plantas/:plantaId")
    public ResponseEntity <Void> removePlanta (long plantaId) throws PlantaNotFoundException {
        plantaService.remove(plantaId);
        return ResponseEntity.noContent().build();
    }


//CONTROL DE EXCEPCIONES ****************

    @ExceptionHandler (PlantaNotFoundException.class) //Es un gestor de excepciones. Si encuentra PlantaNotFoundException generado por el removePlanta, lanzará un Not Found
    public ResponseEntity<ErrorResponse> handlePlantaNotFoundException(PlantaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (CuidadoNotFoundException.class)//Es un gestor de excepciones. Si encuentra CuidadoNotFoundException generado por el removePlanta, lanzará un Not Found
    public ResponseEntity<ErrorResponse> handleCuidadoNotFoundException(CuidadoNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (CategoriaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoriaNotFoundException(CategoriaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (PlagaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlagaNotFoundException(PlagaNotFoundException exception) {
        ErrorResponse error = ErrorResponse.generalError(404, exception.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler  (ConsejoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsejoNotFoundException(ConsejoNotFoundException exception) {
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

}
