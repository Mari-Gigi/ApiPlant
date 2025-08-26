package com.svalero.ApiPlant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.ApiPlant.controller.CuidadoController;
import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.CuidadoInDto;
import com.svalero.ApiPlant.domain.dto.CuidadoOutDto;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.exception.CuidadoConflictException;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlantaConflictException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.service.CuidadoService;
import com.svalero.ApiPlant.service.PlantaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CuidadoController.class)
public class CuidadoControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CuidadoService cuidadoService;
    @MockBean
    private PlantaService plantaService;
    @BeforeEach
    void setUp() throws PlantaNotFoundException{
        when(plantaService.getAll(any(), any(), any())).thenReturn(Collections.emptyList());
    }


    private final List<Cuidado> mockCuidadoList = List.of(
            new Cuidado(1L, true, "frecuente", "Tierra ácida", 80.0f, null, List.of(new Planta(1L))),
            new Cuidado(2L, false, "moderado", "Tierra neutra", 60.0f, null, null)
    );

    private final List<CuidadoOutDto> mockCuidadoOutDtoList = List.of(
            new CuidadoOutDto(1L, true, "frecuente", "Tierra ácida", 80.0f, List.of(99L)),
            new CuidadoOutDto (2L, false, "moderado", "Tierra neutra", 60.0f, List.of(99L))
    );



    //GetAll ***************************************************
    //Response 200
    @Test
    void getAll_sinFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el
        when(cuidadoService.getAll("", "", null)).thenReturn(mockCuidadoList); //"" porque espera cadena vacia, si dejo null falla porqeu nunca recibe nulo

        mockMvc.perform(MockMvcRequestBuilders.get("/cuidados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].riego").value("frecuente"))
                .andExpect(jsonPath("$[0].sustrato").value("Tierra ácida"))
                .andExpect(jsonPath("$[0].esInterior").value(true))
                .andExpect(jsonPath("$[1].riego").value("moderado"))
                .andExpect(jsonPath("$[1].sustrato").value("Tierra neutra"))
                .andExpect(jsonPath("$[1].esInterior").value(false));

        verify(cuidadoService).getAll("", "", null);
    }

   @Test
    void getAll_byRiego_Ok() throws Exception {
        when(cuidadoService.getAll("frecuente", "", null)).thenReturn(mockCuidadoList);

        mockMvc.perform(get("/cuidados")
                        .queryParam("riego", "frecuente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].riego").value("frecuente"));

        verify(cuidadoService).getAll("frecuente", "", null);

    }

    @Test
    void getAll_bySustrato_Ok() throws Exception {
        when(cuidadoService.getAll("", "Tierra ácida", null)).thenReturn(mockCuidadoList);

        mockMvc.perform(get("/cuidados")
                        .queryParam("sustrato", "Tierra ácida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sustrato").value("Tierra ácida")); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(cuidadoService).getAll("", "Tierra ácida", null);
    }

    @Test
    void getAll_byToxicaTrue_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(cuidadoService.getAll("", "", true)).thenReturn(mockCuidadoList);

        mockMvc.perform(get("/cuidados")
                        .queryParam("esInterior", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].esInterior").value(true)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(cuidadoService).getAll("", "", true);
    }

   @Test
    void getAll_allFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros
       when(cuidadoService.getAll("frecuente", "Tierra ácida", true)).thenReturn(mockCuidadoList);

       mockMvc.perform(get("/cuidados")
                       .queryParam("riego", "frecuente")
                       .queryParam("sustrato", "Tierra ácida")
                       .queryParam("esInterior", "true"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].riego").value("frecuente"))
               .andExpect(jsonPath("$[0].sustrato").value("Tierra ácida"))
               .andExpect(jsonPath("$[0].esInterior").value(true)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

       verify(cuidadoService).getAll("frecuente", "Tierra ácida", true);
    }

    //Response 400
    @Test
    void getAll_invalidPathVariable_shouldReturn400() throws Exception {
        mockMvc.perform(get("/cuidados")
                        .queryParam("esInterior", "ghfghfs"))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void getAll_byRiegoNotFound_404() throws Exception {
        when(cuidadoService.getAll(eq("riegoInexistente"), anyString(), any()))
                .thenThrow(new CuidadoNotFoundException());

        mockMvc.perform(get("/cuidados")
                        .queryParam("riego", "riegoInexistente"))
                .andExpect(status().isNotFound());
    }


    //GetById **********************************************************
    //Response 200
  @Test
    void getById_Ok() throws Exception {

        when(cuidadoService.get(1)).thenReturn(mockCuidadoOutDtoList.get(0));

        mockMvc.perform(get("/cuidados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCuidado", is(1)));

        verify(cuidadoService).get(1);
    }

    //Response 400
    @Test
    void getById_invalidPathVariable_shouldReturn400() throws Exception {
        mockMvc.perform(get("/cuidados/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());

    }

    //Response 404
    @Test
    void getById_CuidadoNotFound_shouldReturn404() throws Exception {
        // Mockear que el servicio lanza PlantaNotFoundException
        when(cuidadoService.get(9999L)).thenThrow(new CuidadoNotFoundException());

        mockMvc.perform(get("/cuidado/9999"))
                .andExpect(status().isNotFound());


    }


    //Add**********************************************************
    //Response 201
    @Test
    void Add_Ok() throws Exception {

        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Tierra ácida", 80.0f, List.of(99L));
        CuidadoOutDto cuidadoOutDto = new CuidadoOutDto(1L, true, "frecuente", "Tierra ácida", 80.0f, List.of(99L));

        String requestBody = """
                {
                    "esInterior": true,
                    "riego": "frecuente",
                    "sustrato": "Tierra ácida",
                    "humedad": 80.0,
                    "plantaIds": [99]
                }
                """;

        when(cuidadoService.addCuidado(any(CuidadoInDto.class))).thenReturn(cuidadoOutDto);

        mockMvc.perform(post("/cuidados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riego").value("frecuente"))
                .andExpect(jsonPath("$.sustrato").value("Tierra ácida"))
                .andExpect(jsonPath("$.plantaIds").value(99));


        verify(cuidadoService).addCuidado(any(CuidadoInDto.class));

    }

    //Response 400
    @Test
    void Add_400() throws Exception {

        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Tierra ácida", 80.0f, List.of(99L));

        String invalidRequestBody = """
                {
                   "esInterior": true,
                    "riego": null,
                    "sustrato": "Tierra ácida",
                    "humedad": 80.0,
                    "plantaIds": [99]
                }
                """;

        mockMvc.perform(post("/cuidados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(cuidadoService, never()).addCuidado(any(CuidadoInDto.class));
    }
    //Responde 404
    @Test
    void
    Add_PlantaNotFound404() throws Exception {

        String invalidRequestBody = """
            {
              "esInterior": true,
              "riego": "frecuente",
              "sustrato": "Tierra ácida",
              "humedad": 80.0,
              "plantaIds": [9999]
            }
        """;

        given(cuidadoService.addCuidado(any(CuidadoInDto.class)))
                .willThrow(new PlantaNotFoundException());

        mockMvc.perform(post("/cuidados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isNotFound());


    }


    //Modify **********************************************************
    //Response 201
    @Test
    void Modify_Ok() throws Exception {

        long idCuidado = 1L;
        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Tierra ácida", 80.0f, List.of(99L));
        CuidadoOutDto cuidadoOutDto = new CuidadoOutDto(1L, true, "frecuente", "Orgánico", 80.0f, List.of(99L));

        when(cuidadoService.modify(eq(idCuidado), any(CuidadoInDto.class))).thenReturn(cuidadoOutDto);

        String RequestBody = """
                {
                    "esInterior": true,
                    "riego": "frecuente",
                    "sustrato": "Orgánico",
                    "humedad": 80.0,
                    "plantaIds": [99]
                }
                """;

        mockMvc.perform(put("/cuidados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riego").value("frecuente"))
                .andExpect(jsonPath("$.sustrato").value("Orgánico"))
                .andExpect(jsonPath("$.idCuidado").value(1));

        verify(cuidadoService).modify(eq(idCuidado), any(CuidadoInDto.class));
    }

    //Response 400
    @Test
    void Modify_invalidPathVariable_400() throws Exception {
        // No necesito mockear el servicio porqeu nunca llegar, el error se provoca antes

        String requestBody = """
            {
                "esInterior": true,
                "riego": "frecuente",
                "sustrato": "Orgánico",
                "humedad": 80.0,
                "plantaIds": [99]
            }
            """;

        mockMvc.perform(put("/cuidados/abc") // id inválido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Modify_cuidadoNotFound_404() throws Exception{

        long idCuidado = 1L;

        String requestBody = """
            {
                "esInterior": true,
                "riego": "frecuente",
                "sustrato": "Orgánico",
                "humedad": 80.0,
                "plantaIds": [99]
            }
            """;

        // simulo que el servicio lanza el 404
        when(cuidadoService.modify(eq(idCuidado), any(CuidadoInDto.class)))
                .thenThrow(new CuidadoNotFoundException());

        mockMvc.perform(put("/cuidados/{cuidadoId}", idCuidado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(cuidadoService).modify(eq(idCuidado), any(CuidadoInDto.class));
    }

    //Response 409
    @Test
    void Modify_plantaConflict_409() throws Exception{

        long idCuidado = 1L;

        String requestBody = """
            {
                "esInterior": true,
                "riego": "frecuente",
                "sustrato": "Orgánico",
                "humedad": 80.0,
                "plantaIds": [99]
            }
            """;

        // simulo que el servicio lanza el 409
        when(cuidadoService.modify(eq(idCuidado), any(CuidadoInDto.class)))
                .thenThrow(new CuidadoConflictException());

        mockMvc.perform(put("/cuidados/{cuidadoId}", idCuidado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());

        verify(cuidadoService).modify(eq(idCuidado), any(CuidadoInDto.class));
    }


    //Delete **********************************************************
    //Response 204
    @Test
    void Delete_Ok() throws Exception {

        mockMvc.perform(delete("/cuidados/1"))
                .andExpect(status().isNoContent());

        verify(cuidadoService).remove(1L);
    }

    //Response 400
    @Test
    void Delete_invalidPathVariable_400() throws Exception {

        mockMvc.perform(delete("/cuidados/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Delete_CuidadoNotFound404() throws Exception {

        // Simula que al intentar borrar el cuidado con id 1 lanza cuidadoNotFoundException
        doThrow(new CuidadoNotFoundException()).when(cuidadoService).remove(99999L);

        mockMvc.perform(delete("/cuidados/99999"))
                .andExpect(status().isNotFound());
    }

    //Response 409
    @Test
    void Delete_CuidadoConflict409() throws Exception {

        long idCuidado = 1L;
        // Simula que al intentar borrar el cuidado con id 1 lanza cuidadoNotFoundException
        doThrow(new CuidadoConflictException()).when(cuidadoService).remove(idCuidado);

        mockMvc.perform(delete("/cuidados/{idCuidado}", idCuidado))
                .andExpect(status().isConflict());

        verify(cuidadoService).remove(idCuidado);


    }



}
