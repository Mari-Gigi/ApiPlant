package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.ConsejoService;
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
public class ConsejoController {

    @Autowired
    private ConsejoService consejoService;

    @GetMapping("/consejos")
    public ResponseEntity<List<Consejo>> getAll(
            @RequestParam(value = "titulo", defaultValue = "") String titulo,
            @RequestParam(value = "verificado", required = false) Boolean verificado,
            @RequestParam(value = "importancia", required = false) Float importancia) {

        return new ResponseEntity<>(consejoService.getAll(titulo, verificado, importancia), HttpStatus.OK);
    }

    @GetMapping("/consejos/:consejoId")
    public ResponseEntity <ConsejoOutDto> getConsejo(long consejoId) throws ConsejoNotFoundException {
        ConsejoOutDto consejoOutDto = consejoService.get(consejoId);
        return new ResponseEntity<>(consejoOutDto, HttpStatus.OK);
    }


    @PostMapping("/consejos")
    public ResponseEntity <Consejo> addConsejo (@RequestBody ConsejoInDto consejoInDto) {
        Consejo consejo = consejoService.add(consejoInDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(consejoService.add(consejoInDto));
    }

    @PutMapping("/consejos/:consejoId")
    public ResponseEntity<ConsejoOutDto> modifyConsejo(long consejoId, @Valid @RequestBody ConsejoInDto consejo) throws  ConsejoNotFoundException {
        ConsejoOutDto modifiedConsejo = consejoService.modify(consejoId, consejo);
        return new ResponseEntity<>(modifiedConsejo, HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/consejos/:consejoId")
    public ResponseEntity<Void> removeConsejo(long consejoId) {
        try {
            consejoService.remove(consejoId);
            return ResponseEntity.noContent().build();
        } catch (ConsejoConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ConsejoNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(ConsejoNotFoundException.class)
    public ResponseEntity<String> handleConsejoNotFoundException(ConsejoNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConsejoConflictException.class)
    public ResponseEntity<String> handleConsejoNotFoundException(ConsejoConflictException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
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




