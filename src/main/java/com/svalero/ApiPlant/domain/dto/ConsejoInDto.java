package com.svalero.ApiPlant.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ConsejoInDto {

    @NotNull(message = "El título es obligado")
    private String titulo;
    private String explicacion;
    @NotNull (message = "Explicación obligatoria")
    private Boolean verificado;
    @Min(value = 0, message = "Debe estar entre 0 y 10")
    @Max(value = 10, message = "Debe estar entre 0 y 10")
    private Float importancia;
    private List<Long> plantaIds;

}



