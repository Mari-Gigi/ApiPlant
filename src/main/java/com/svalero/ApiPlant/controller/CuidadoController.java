package com.svalero.ApiPlant.controller;
import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlagaNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.repository.CuidadoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.service.CuidadoService;
import com.svalero.ApiPlant.exception.CuidadoConflictException;
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
            @RequestParam(value = "esInterior", required = false) Boolean esInterior) {

        return new ResponseEntity<>(cuidadoService.getAll(riego, sustrato, esInterior), HttpStatus.OK);
    }


    @GetMapping("/cuidados/:cuidadoId")
    public ResponseEntity <CuidadoOutDto> getCuidado(long cuidadoId) throws CuidadoNotFoundException {
        CuidadoOutDto cuidadoOutDto = cuidadoService.get(cuidadoId);
        return new ResponseEntity<>(cuidadoOutDto, HttpStatus.OK);
    }


    @PostMapping ("/cuidados")
    public ResponseEntity <Cuidado> addCuidado (@RequestBody Cuidado cuidado) {
        Cuidado newCuidado = cuidadoService.add(cuidado);
        return new ResponseEntity<>(newCuidado, HttpStatus.CREATED);
    }

//REVISAR ********************
    @PutMapping("/cuidados/:cuidadoId")
    public ResponseEntity<CuidadoOutDto> modifyCuidado (long cuidadoId, @Valid @RequestBody CuidadoInDto cuidado) throws  CuidadoNotFoundException {
        CuidadoOutDto modifiedCuidado = cuidadoService.modify(cuidadoId, cuidado);
        return new ResponseEntity<>(modifiedCuidado, HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/cuidados/:cuidadoId")
    public ResponseEntity<Void> deleteCuidado(long cuidadoId) {
        try {
            cuidadoService.remove(cuidadoId);
            return ResponseEntity.noContent().build();
        } catch (CuidadoConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        } catch (CuidadoNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }



//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(CuidadoNotFoundException.class)
    public ResponseEntity<String> handleCuidadoNotFoundException(CuidadoNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CuidadoConflictException.class)
    public ResponseEntity<String> handleCuidadoConflictException(CuidadoConflictException ex) {
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





