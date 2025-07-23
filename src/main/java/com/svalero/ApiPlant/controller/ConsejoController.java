package com.svalero.ApiPlant.controller;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.ConsejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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


//put******************************

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


}

