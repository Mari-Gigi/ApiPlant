package com.svalero.ApiPlant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.ApiPlant.controller.PlagaController;
import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.PlagaInDto;
import com.svalero.ApiPlant.domain.dto.PlagaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.PlagaService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PlagaController.class)
public class PlagaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PlagaService plagaService;
    @MockBean
    private PlantaService plantaService;
    @BeforeEach
    void setUp() throws PlantaNotFoundException{
        when(plantaService.getAll(any(), any(), any())).thenReturn(Collections.emptyList());
    }


    private final List<Plaga> mockPlagaList = List.of(
            new Plaga(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
                    6.5f, true, "jabón potasico", null, List.of(new Planta(50L))),
            new Plaga(2, "Araña roja", "Punteado amarillento o blanquecino en las hojas",
                    7.5f, true, "acaricida", null, List.of(new Planta(30L)))
    );

    private final List<PlagaOutDto> mockPlagaOutDtoList = List.of(
            new PlagaOutDto(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
                    6.5f, true, "jabón potasico",  List.of(50L)),
            new PlagaOutDto(2, "Araña roja", "Punteado amarillento o blanquecino en las hojas",
                    7.5f, true, "acaricida",List.of(30L))
    );


    //GetAll ***************************************************
    //Response 200
    @Test
    void getAll_sinFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el
        when(plagaService.getAll("", null, null)).thenReturn(mockPlagaList); //"" porque espera cadena vacia, si dejo null falla porqeu nunca recibe nulo

        mockMvc.perform(MockMvcRequestBuilders.get("/plagas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Cochinilla algodonosa"))
                .andExpect(jsonPath("$[0].sintomas").value("Presencia de masas blancas y algodonosas con melaza"))
                .andExpect(jsonPath("$[0].riesgo").value(6.5f))
                .andExpect(jsonPath("$[1].esLetal").value(true));


        verify(plagaService).getAll("", null, null);
    }

    @Test
    void getAll_byNombre_Ok() throws Exception {
        when(plagaService.getAll("Cochinilla algodonosa", null, null)).thenReturn(mockPlagaList);

        mockMvc.perform(get("/plagas")
                        .queryParam("nombre", "Cochinilla algodonosa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Cochinilla algodonosa"));

        verify(plagaService).getAll("Cochinilla algodonosa", null, null);

    }

    @Test
    void getAll_byRiesgo_Ok() throws Exception {
        when(plagaService.getAll("", 6.5f, null)).thenReturn(mockPlagaList);

        mockMvc.perform(get("/plagas")
                        .queryParam("riesgo", "6.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].riesgo").value(6.5f)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(plagaService).getAll("", 6.5f, null);
    }

    @Test
    void getAll_byEsLetal_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(plagaService.getAll("", null, true)).thenReturn(mockPlagaList);

        mockMvc.perform(get("/plagas")
                        .queryParam("esLetal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].esLetal").value(true)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(plagaService).getAll("", null, true);
    }

    @Test
    void getAll_allFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros
        when(plagaService.getAll("Cochinilla algodonosa", 6.5f, true)).thenReturn(mockPlagaList);

        mockMvc.perform(get("/plagas")
                        .queryParam("nombre", "Cochinilla algodonosa")
                        .queryParam("riesgo", "6.5")
                        .queryParam("esLetal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Cochinilla algodonosa"))
                .andExpect(jsonPath("$[0].riesgo").value("6.5"))
                .andExpect(jsonPath("$[0].esLetal").value(true)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(plagaService).getAll("Cochinilla algodonosa", 6.5f, true);
    }



    //GetById **********************************************************
    //Response 200
    @Test
    void getById_Ok() throws Exception {

        when(plagaService.get(1)).thenReturn(mockPlagaOutDtoList.get(0));

        mockMvc.perform(get("/plagas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPlaga", is(1)));

        verify(plagaService).get(1);
    }

    //Response 400
    @Test
    void getById_invalidPathVariable_400() throws Exception {
        mockMvc.perform(get("/plagas/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());

    }

    //Response 404
    @Test
    void getById_PlagaNotFound_404() throws Exception {
        // Mockear que el servicio lanza PlantaNotFoundException
        when(plagaService.get(9999L)).thenThrow(new PlagaNotFoundException());

        mockMvc.perform(get("/plagas/9999"))
                .andExpect(status().isNotFound());


    }


    //Add**********************************************************
    //Response 201
    @Test
    void Add_Ok() throws Exception {

        Plaga plaga = new Plaga(1L, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
                6.5f, true, "jabón potasico", LocalDate.now(), null);

        String requestBody = """
                {
                    "nombre": "Cochinilla algodonosa",
                    "sintomas": "Presencia de masas blancas y algodonosas con melaza",
                    "riesgo": 6.5,
                    "esLetal": true
                }
                """;

        when(plagaService.add(any(PlagaInDto.class))).thenReturn(plaga);

        mockMvc.perform(post("/plagas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cochinilla algodonosa"))
                .andExpect(jsonPath("$.sintomas").value("Presencia de masas blancas y algodonosas con melaza"));


        verify(plagaService, times(2)).add(any(PlagaInDto.class));


    }

    //Response 400
   @Test
    void Add_400() throws Exception {

       Plaga plaga = new Plaga(1L, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
               6.5f, true, "jabón potasico", LocalDate.now(), null);


       String requestBody = """
                {
                    "nombre": null,
                    "sintomas": "Presencia de masas blancas y algodonosas con melaza",
                    "riesgo": 6.5,
                    "esLetal": true
                }
                """;

        mockMvc.perform(post("/plagas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(plagaService, never()).add(any(PlagaInDto.class));
    }

    //Response 404
    @Test
    void
    Add_PlantaNotFound404() throws Exception {

        String invalidRequestBody = """
            {
              "nombre": "Cochinilla algodonosa",
              "sintomas": "Presencia de masas blancas y algodonosas con melaza",
              "riesgo": 6.5,
              "esLetal": true,
              "plantaIds": [999]
            }
        """;

        given(plagaService.add(any(PlagaInDto.class)))
                .willThrow(new PlantaNotFoundException());

        mockMvc.perform(post("/plagas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isNotFound());

    }



    //Modify **********************************************************
    //Response 201
    @Test
    void Modify_Ok() throws Exception {

        long idPlaga = 1L;

        Plaga plaga = new Plaga(1L, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
                6.5f, true, "jabón potasico", LocalDate.now(), null);
        PlagaOutDto plagaOutDto = new PlagaOutDto(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza",
                7.0f, true, "jabón potasico",  null);

        when(plagaService.modify(eq(idPlaga), any(PlagaInDto.class)))
                .thenReturn(plagaOutDto);

        String RequestBody = """
                    {
                    "nombre": "Cochinilla algodonosa",
                    "sintomas": "Presencia de masas blancas y algodonosas con melaza",
                    "riesgo": 7.0,
                    "esLetal": true,
                    "tratamiento": "jabón potasico"
                    }
        """;

        mockMvc.perform(put("/plagas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cochinilla algodonosa"))
                .andExpect(jsonPath("$.sintomas").value("Presencia de masas blancas y algodonosas con melaza"))
                .andExpect(jsonPath("$.idPlaga").value(1));

        verify(plagaService).modify(eq(idPlaga), any(PlagaInDto.class));

    }

    //Response 400
    @Test
    void Modify_invalidPathVariable_400() throws Exception {
        // No necesito mockear el servicio porqeu nunca llegar, el error se provoca antes

        String requestBody = """
            {
                "nombre": "Cochinilla algodonosa",
                "sintomas": "Presencia de masas blancas y algodonosas con melaza",
                "riesgo": 7.0,
                "esLetal": true,
                "tratamiento": "jabón potasico"
            }
            """;

        mockMvc.perform(put("/plagas/abc") // id inválido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void
    Modify_PlagaNotFound_404() throws Exception{

        long idPlaga = 1L;

        String requestBody = """
            {
                "nombre": "Cochinilla algodonosa",
                "sintomas": "Presencia de masas blancas y algodonosas con melaza",
                "riesgo": 7.0,
                "esLetal": true,
                "tratamiento": "jabón potasico"
            }
            """;

        // simulo que el servicio lanza el 404
        when(plagaService.modify(eq(idPlaga), any(PlagaInDto.class)))
                .thenThrow(new PlagaNotFoundException());

        mockMvc.perform(put("/plagas/{plagaId}", idPlaga)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(plagaService).modify(eq(idPlaga), any(PlagaInDto.class));
    }



    //Delete **********************************************************
    //Response 204
    @Test
    void Delete_Ok() throws Exception {

        mockMvc.perform(delete("/plagas/1"))
                .andExpect(status().isNoContent());

        verify(plagaService).remove(1L);
    }

    //Response 400
    @Test
    void Delete_invalidPathVariable_400() throws Exception {

        mockMvc.perform(delete("/plagas/abc"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Delete_PlagaNotFound404() throws Exception {

        // Simula que al intentar borrar la categoria con id 1 lanza CategoriaNotFoundException
        doThrow(new PlagaNotFoundException()).when(plagaService).remove(99999L);

        mockMvc.perform(delete("/plagas/99999"))
                .andExpect(status().isNotFound());
    }

    //Response 409
    @Test
    void Delete_PlagaConflict409() throws Exception {

        long idPlaga = 1L;
        // Simula que al intentar borrar el cuidado con id 1 lanza cuidadoNotFoundException
        doThrow(new PlagaConflictException()).when(plagaService).remove(idPlaga);

        mockMvc.perform(delete("/plagas/{idPlaga}", idPlaga))
                .andExpect(status().isConflict());

        verify(plagaService).remove(idPlaga);


    }


}




