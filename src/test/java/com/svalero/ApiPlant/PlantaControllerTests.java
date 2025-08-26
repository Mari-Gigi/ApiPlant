package com.svalero.ApiPlant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.ApiPlant.controller.PlantaController;
import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.CuidadoNotFoundException;
import com.svalero.ApiPlant.exception.PlantaNotFoundException;
import com.svalero.ApiPlant.service.PlantaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PlantaController.class)
public class PlantaControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PlantaService plantaService;

    private final List<Planta> mockPlantaList = List.of(
            new Planta(1, "Rosa", "Indica", 1.5f, "Herbácea", null, true, new Cuidado(1), new Categoria(1), List.of(new Plaga(1L)), List.of(new Consejo(1L))),
            new Planta(2, "Tulipa", "Gesneriana", 0.8f, "Bulbosa", null, false, new Cuidado(1), new Categoria(2), null, null),
            new Planta(3, "Lirio", "Niger", 1.0f, "Perenne", null, false, new Cuidado(2), new Categoria(3), null, null)
    );

    private final List<PlantaOutDto> mockPlantaOutDtoList = List.of(
            new PlantaOutDto(1, "Rosa", "Indica", true, 1.5f, "Herbácea", 1, 1, List.of(1L), List.of(1L)),
            new PlantaOutDto(2, "Tulipa", "Gesneriana", false, 0.8f, "Bulbosa", 1, 2, List.of(), List.of()),
            new PlantaOutDto(3, "Lirio", "Niger", false, 1.0f, "Perenne", 2, 3, List.of(), List.of())
    );

    //GetAll ***************************************************
    //Response 200
    @Test
    void getAll_sinFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el
        when(plantaService.getAll("", "", null)).thenReturn(mockPlantaOutDtoList); //"" porque espera cadena vacia, si dejo null falla porqeu nunca recibe nulo

        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/plantas"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        List<PlantaOutDto> plantaListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertNotNull(plantaListResponse);
        assertEquals(3, plantaListResponse.size());
        assertEquals("Rosa", plantaListResponse.get(0).getGenero());
        assertEquals("Indica", plantaListResponse.get(0).getEspecie());
    }

    @Test
    void getAll_byGenero_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(plantaService.getAll("Rosa", "", null)).thenReturn(mockPlantaOutDtoList);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/plantas")
                        .queryParam("genero", "Rosa"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        List<PlantaOutDto> plantaListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertNotNull(plantaListResponse);
        assertEquals(3, plantaListResponse.size());
        assertEquals("Rosa", plantaListResponse.get(0).getGenero());
        assertEquals("Indica", plantaListResponse.get(0).getEspecie());
    }

    @Test
    void getAll_byEspecie_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(plantaService.getAll("", "Indica", null)).thenReturn(mockPlantaOutDtoList);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/plantas")
                        .queryParam("especie", "Indica"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        List<PlantaOutDto> plantaListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertNotNull(plantaListResponse);
        assertEquals(3, plantaListResponse.size());
        assertEquals("Rosa", plantaListResponse.get(0).getGenero());
        assertEquals("Indica", plantaListResponse.get(0).getEspecie());
    }

    @Test
    void getAll_byToxicaTrue_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(plantaService.getAll("", "", true)).thenReturn(mockPlantaOutDtoList);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/plantas")
                        .queryParam("esToxica", "true"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        List<PlantaOutDto> plantaListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertNotNull(plantaListResponse);
        assertEquals(3, plantaListResponse.size());
        assertEquals("Rosa", plantaListResponse.get(0).getGenero());
        assertEquals("Indica", plantaListResponse.get(0).getEspecie());
    }

    @Test
    void getAll_allFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros
        when(plantaService.getAll("Rosa", "Indica", true)).thenReturn(mockPlantaOutDtoList);

        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/plantas")
                        .queryParam("genero", "Rosa")
                        .queryParam("especie", "Indica")
                        .queryParam("esToxica", "true"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        List<PlantaOutDto> plantaListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertNotNull(plantaListResponse);
        assertEquals(3, plantaListResponse.size());
        assertEquals("Rosa", plantaListResponse.get(0).getGenero());
        assertEquals("Indica", plantaListResponse.get(0).getEspecie());
        assertTrue(plantaListResponse.get(0).getEsToxica());
    }

    //Response 400
    @Test
    void getAll_invalidPathVariable_shouldReturn400() throws Exception {
        mockMvc.perform(get("/plantas")
                        .queryParam("esToxica", "ghfghfs"))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void getAll_byGeneroNotFound_404() throws Exception {
        when(plantaService.getAll(eq("plantaInexistente"), anyString(), any()))
                .thenThrow(new PlantaNotFoundException());

        mockMvc.perform(get("/plantas")
                        .queryParam("genero", "plantaInexistente"))
                .andExpect(status().isNotFound());
    }

    //Response 500
    // Este test "fuerza que la respuesta sea 500", pero previamente ha revisado que las capas funcionan.
    // Que las capas funcionan ya se revisa en los test anteriores cuyas excep si saltan manipulando los datos reales
    @Test
    void getAll_errorQueryParam_500() throws Exception {
        // Simular que el servicio lanza una RuntimeException
        when(plantaService.getAll(anyString(), anyString(), any())).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/plantas")
                        .queryParam("genero", "algo"))
                .andExpect(status().isInternalServerError());
    }


    //GetById **********************************************************
    //Response 200
    @Test
    void getById_Ok() throws Exception {


        when(plantaService.get(1)).thenReturn(mockPlantaOutDtoList.get(0));

        mockMvc.perform(get("/plantas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_planta", is(1)));

        verify(plantaService).get(1);
    }

    //Response 400
    @Test
    void getById_invalidPathVariable_shouldReturn400() throws Exception {
        mockMvc.perform(get("/plantas/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void getById_PlantaNotFound_shouldReturn404() throws Exception {
        // Mockear que el servicio lanza PlantaNotFoundException
        when(plantaService.get(9999L)).thenThrow(new PlantaNotFoundException());

        mockMvc.perform(get("/plantas/9999"))
                .andExpect(status().isNotFound());
    }


    //Add**********************************************************
    //Response 201
    @Test
    void Add_Ok() throws Exception {

        PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f,
                "Herbácea", 1, 1, List.of(1L), List.of(1L));
        PlantaOutDto plantaOutDto = new PlantaOutDto(1L, "Rosa", "Indica", true, 1.5f,
                "Herbácea", 1, 1, List.of(1L), List.of(1L));

        String requestBody = """
                {
                    "genero": "Rosa",
                    "especie": "Indica",
                    "esToxica": true,
                    "alturaMaxima": 1.5,
                    "tipoCrecimiento": "Herbácea",
                    "cuidadoId": 1,
                    "categoriaId": 1,
                    "plagaIds": [1],
                    "consejoIds": [1]
                }
                """;

        when(plantaService.add(any(PlantaInDto.class))).thenReturn(plantaOutDto);

        mockMvc.perform(post("/plantas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.genero").value("Rosa"))
                .andExpect(jsonPath("$.especie").value("Indica"))
                .andExpect(jsonPath("$.cuidadoId").value(1))
                .andExpect(jsonPath("$.categoriaId").value(1))
                .andExpect(jsonPath("$.plagaIds").isArray())
                .andExpect(jsonPath("$.consejoIds").isArray());

        verify(plantaService).add(any(PlantaInDto.class));

    }

    //Response 400
    @Test
    void Add_400() throws Exception {

        PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f,
                "Herbácea", 1, 1, List.of(1L), List.of(1L));

        String invalidRequestBody = """
                {
                    "genero": null,
                    "especie": "Indica",
                    "esToxica": true,
                    "alturaMaxima": -1.5,
                    "tipoCrecimiento": "Herbácea",
                    "cuidadoId": 1,
                    "categoriaId": 1,
                    "plagaIds": [1],
                    "consejoIds": [1]
                }
                """;

        mockMvc.perform(post("/plantas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(plantaService, never()).add(any(PlantaInDto.class));
    }

    //Responde 404
    @Test
    void
    Add_CuidadoNotFound404() throws Exception {

        String invalidRequestBody = """
            {
              "genero": "Rosa",
              "especie": "Indica",
              "esToxica": true,
              "alturaMaxima": 1.5,
              "tipoCrecimiento": "Herbácea",
              "cuidadoId": 99999,
              "categoriaId": 1,
              "plagaIds": [1],
              "consejoIds": [1]
            }
        """;

        given(plantaService.add(any(PlantaInDto.class)))
                .willThrow(new CuidadoNotFoundException());

        mockMvc.perform(post("/plantas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isNotFound());


    }


    //Modify **********************************************************
    //Response 201
    @Test
    void Modify_Ok() throws Exception {

        long id_planta = 1L;
        PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f,
                "Herbácea", 1, 1, List.of(1L), List.of(1L));

        PlantaOutDto plantaOutDto = new PlantaOutDto(1L, "Tulipan", "Holandes", true, 1.5f,
                "Herbácea", 1, 1, List.of(1L), List.of(1L));

        when(plantaService.modify(eq(id_planta), any(PlantaInDto.class))).thenReturn(plantaOutDto);

        String RequestBody = """
                {
                    "genero": "Tulipan",
                    "especie": "Holandes",
                    "esToxica": true,
                    "alturaMaxima": 1.5,
                    "tipoCrecimiento": "Herbácea",
                    "cuidadoId": 1,
                    "categoriaId": 1,
                    "plagaIds": [1],
                    "consejoIds": [1]
                }
                """;

        mockMvc.perform(put("/plantas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.genero").value("Tulipan"))
                .andExpect(jsonPath("$.especie").value("Holandes"))
                .andExpect(jsonPath("$.id_planta").value(1));

        verify(plantaService).modify(eq(id_planta), any(PlantaInDto.class));
    }

    //Response 400
    @Test
    void Modify_invalidPathVariable_400() throws Exception {
        // No necesito mockear el servicio porqeu nunca llegar, el error se provoca antes

        String requestBody = """
            {
                "genero": "Rosa",
                "especie": "Indica",
                "esToxica": true,
                "alturaMaxima": 1.5,
                "tipoCrecimiento": "Herbácea",
                "cuidadoId": 1,
                "categoriaId": 1,
                "plagaIds": [1],
                "consejoIds": [1]
            }
            """;

        mockMvc.perform(put("/plantas/abc") // id inválido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Modify_plantaNotFound_404() throws Exception{

        long idPlanta = 1L;

        String requestBody = """
            {
                "genero": "Rosa",
                "especie": "Indica",
                "esToxica": true,
                "alturaMaxima": 1.5,
                "tipoCrecimiento": "Herbácea",
                "cuidadoId": 1,
                "categoriaId": 1,
                "plagaIds": [1],
                "consejoIds": [1]
            }
            """;

        // simulo que el servicio lanza el 404
        when(plantaService.modify(eq(idPlanta), any(PlantaInDto.class)))
                .thenThrow(new PlantaNotFoundException());

        mockMvc.perform(put("/plantas/{plantaId}", idPlanta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(plantaService).modify(eq(idPlanta), any(PlantaInDto.class));
    }


    //Delete **********************************************************
    //Response 204
    @Test
    void Delete_Ok() throws Exception {

        mockMvc.perform(delete("/plantas/1"))
                .andExpect(status().isNoContent());

        verify(plantaService).remove(1);
    }

    //Response 400
    @Test
    void Delete_invalidPathVariable_400() throws Exception {

        mockMvc.perform(delete("/plantas/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Delete_PlantaNotFound404() throws Exception {

        // Simula que al intentar borrar la planta con id 1 lanza PlantaNotFoundException
        doThrow(new PlantaNotFoundException()).when(plantaService).remove(99999L);

        mockMvc.perform(delete("/plantas/99999"))
                .andExpect(status().isNotFound());




    }
}





