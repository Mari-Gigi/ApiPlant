package com.svalero.ApiPlant.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsejoInDto {

    @NotNull(message = "titulo is required")
    private String titulo;
    private String explicacion;
    @NotNull (message = "Verification is required")
    private boolean verificado;
    @Min(value = 1, message = "Must be between 1 and 10")
    @Max(value = 10, message = "Must be between 1 and 10")
    private float importancia;

}



