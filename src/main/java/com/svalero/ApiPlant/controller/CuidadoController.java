package com.svalero.ApiPlant.controller;
import com.svalero.ApiPlant.domain.Cuidado;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.CuidadoInDto;
import com.svalero.ApiPlant.domain.dto.CuidadoOutDto;
import com.svalero.ApiPlant.domain.dto.ErrorResponse;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
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


 /*  @GetMapping("/cuidados") //esto es una Operacion o endpoint, y cada uno tiene una url
    public ResponseEntity<List<Cuidado>> getAll() {
        return new ResponseEntity<>(cuidadoService.getAll(), HttpStatus.OK); //Devuelve la lista de Cuidados seguido del código de estado 200
    } */

    @GetMapping("/cuidados")
    public ResponseEntity<List<CuidadoOutDto>> getAll(
            @RequestParam(value = "riego", defaultValue = "") String riego,
            @RequestParam(value = "sustrato", defaultValue = "") String sustrato,
            @RequestParam(value = "esInterior", required = false) Boolean esInterior) {

        return new ResponseEntity<>(cuidadoService.getAll(riego, sustrato, esInterior), HttpStatus.OK);
    }


    @GetMapping("/cuidados/:cuidadoId")
    public ResponseEntity <Cuidado> getCuidado(long cuidadoId) throws CuidadoNotFoundException {
        Cuidado cuidado = cuidadoService.get(cuidadoId);
        return new ResponseEntity<>(cuidado, HttpStatus.OK);
    }


    @PostMapping ("/cuidados")
    public ResponseEntity <Cuidado> addCuidado (@RequestBody Cuidado cuidado) {
        Cuidado newCuidado = cuidadoService.add(cuidado);
        return new ResponseEntity<>(newCuidado, HttpStatus.CREATED); //código de estado 201
    }

//REVISAR ********************
    @PutMapping("/cuidados/:cuidadoId")
    public ResponseEntity<CuidadoOutDto> modifyCuidado (long cuidadoId, @Valid @RequestBody CuidadoInDto cuidado) throws  CuidadoNotFoundException {
        CuidadoOutDto modifiedCuidado = cuidadoService.modify(cuidadoId, cuidado);
        return new ResponseEntity<>(modifiedCuidado, HttpStatus.NOT_FOUND);
    }

//revisar ********************
    @DeleteMapping ("/cuidados/:cuidadoId")
    public ResponseEntity <Void> removeCuidado (@PathVariable long cuidadoId) throws CuidadoConflictException {
        cuidadoService.remove(cuidadoId);
        return ResponseEntity.noContent().build();  //Estado 204
    }


//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(CuidadoNotFoundException.class)
    public ResponseEntity<String> handleCuidadoNotFoundException(CuidadoNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    //ESTE NO SALTA *****************************
    @ExceptionHandler(CuidadoConflictException.class)
    public ResponseEntity<String> handleCuidadoAlreadyInUse(CuidadoConflictException ex) {
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





