package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.ConsejoService;
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
public class ConsejoController {

    @Autowired
    private ConsejoService consejoService;
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(ConsejoController.class);

    @GetMapping("/consejos")
    public ResponseEntity<List<ConsejoOutDto>> getAll(
            @RequestParam(value = "titulo", defaultValue = "") String titulo,
            @RequestParam(value = "verificado", required = false) Boolean verificado,
            @RequestParam(value = "importancia", required = false) Float importancia,  /*la Exc que devuelve es de tipo 404 xq no convierte bien el float*/
            @RequestParam Map<String, String> allParams) throws ConsejoNotFoundException {

        logger.info("BEGIN getAll");

            Set<String> validParams = Set.of("titulo", "verificado", "importancia");
            for (String param : allParams.keySet()) {
                if (!validParams.contains(param)) {
                    throw new InvalidParameterException("Parámetro inválido: " + param);
                }
            }

        logger.info("END getAll");
            return new ResponseEntity<>(consejoService.getAll(titulo, verificado, importancia), HttpStatus.OK);
        }


    @GetMapping("/consejos/:consejoId")
    public ResponseEntity <ConsejoOutDto> getConsejo(long consejoId) throws ConsejoNotFoundException {
        logger.info("BEGIN getById");
        ConsejoOutDto consejoOutDto = consejoService.get(consejoId);
        logger.info("END getById");
        return new ResponseEntity<>(consejoOutDto, HttpStatus.OK);
    }


    @PostMapping("/consejos")
    public ResponseEntity <Consejo> addConsejo (@Valid @RequestBody ConsejoInDto consejoInDto) {
        logger.info("BEGIN postConsejo");
        Consejo consejo = consejoService.add(consejoInDto);
        logger.info("END postConsejo");
        return ResponseEntity.status(HttpStatus.CREATED).body(consejoService.add(consejoInDto));
    }


    @PutMapping("/consejos/:consejoId")
    public ResponseEntity<ConsejoOutDto> modifyConsejo(long consejoId, @Valid @RequestBody ConsejoInDto consejo)
            throws  ConsejoNotFoundException, PlantaNotFoundException {
        logger.info("BEGIN putConsejo");
        ConsejoOutDto modifiedConsejo = consejoService.modify(consejoId, consejo);
        logger.info("END putConsejo");
        return new ResponseEntity<>(modifiedConsejo, HttpStatus.OK);
    }


    @DeleteMapping("/consejos/:consejoId")
    public ResponseEntity<Void> removeConsejo(long consejoId) throws ConsejoNotFoundException, ConsejoConflictException {
        logger.info("BEGIN deleteConsejo");
        consejoService.remove(consejoId);
        logger.info("END deleteConsejo");
        return ResponseEntity.noContent().build();
    }


//CONTROL DE EXCEPCIONES ******************

    @ExceptionHandler(ConsejoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsejoNotFoundException(ConsejoNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, "Consejo no encontrado con esos parámetros");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(ConsejoConflictException.class)
    public ResponseEntity<ErrorResponse> handleConsejoNotFoundException(ConsejoConflictException ex) {
        ErrorResponse error = ErrorResponse.generalError(409, "Consejo asociado a una planta.");
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




