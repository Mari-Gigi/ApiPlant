package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.PlagaService;
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
public class PlagaController {

    @Autowired
    private PlagaService plagaService;


    @GetMapping("/plagas")
    public ResponseEntity<List<Plaga>> getAll(
            @RequestParam(value = "nombre", defaultValue = "") String nombre,
            @RequestParam(value = "riesgo", required = false) Float riesgo,
            @RequestParam(value = "esLetal", required = false) Boolean esLetal) {

        return new ResponseEntity<>(plagaService.getAll(nombre, riesgo, esLetal), HttpStatus.OK);
    }

    @GetMapping("/plagas/:plagaId")
    public ResponseEntity <PlagaOutDto> getPlaga(long plagaId) throws PlagaNotFoundException {
        PlagaOutDto plagaOutDto = plagaService.get(plagaId);
        return new ResponseEntity<>(plagaOutDto, HttpStatus.OK);
    }


    @PostMapping("/plagas")
    public ResponseEntity<Plaga> addPlaga(@RequestBody PlagaInDto plagaInDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plagaService.add(plagaInDto));
    }

  /*  //REVISAR ********************
    @PutMapping("/cuidados/:cuidadoId")
    public ResponseEntity<CuidadoOutDto> modifyCuidado (long cuidadoId, @Valid @RequestBody CuidadoInDto cuidado) throws CuidadoNotFoundException {
        CuidadoOutDto modifiedCuidado = cuidadoService.modify(cuidadoId, cuidado);
        return new ResponseEntity<>(modifiedCuidado, HttpStatus.NOT_FOUND);
    }*/

    @DeleteMapping("/plagas/:plagaId")
    public ResponseEntity<Void> removePlaga(long plagaId) {
        try {
            plagaService.remove(plagaId);
            return ResponseEntity.noContent().build();
        }catch (PlagaConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PlagaNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        }

    }


    //CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(PlagaNotFoundException.class)
    public ResponseEntity<String> handlePlagaNotFoundException(PlagaNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
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
