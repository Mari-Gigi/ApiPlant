package com.svalero.ApiPlant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.ApiPlant.controller.ConsejoController;
import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.ConsejoInDto;
import com.svalero.ApiPlant.domain.dto.ConsejoOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.ConsejoService;
import com.svalero.ApiPlant.service.PlantaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsejoController.class)
public class ConsejoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ConsejoService consejoService;
    @MockBean
    private PlantaService plantaService;
    @BeforeEach
    void setUp() throws PlantaNotFoundException {
        when(plantaService.getAll(any(), any(), any())).thenReturn(Collections.emptyList());
    }

    private final List<Consejo> mockConsejoList = List.of(
            new Consejo(1, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                     false, 6.5f, null, List.of(new Planta(30L))),
            new Consejo(2, "Luz indirecta", "Coloca la planta alejada de luz directa",
                    true, 8.0f, null, List.of(new Planta(35L)))
    );

    private final List<ConsejoOutDto> mockConsejoOutDtoList = List.of(
            new ConsejoOutDto(1, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                    false, 6.5f, List.of(30L)),
            new ConsejoOutDto(2, "Luz indirecta", "Coloca la planta alejada de luz directa",
                    true, 8.0f,  List.of(35L))
    );

    //GetAll ***************************************************
    //Response 200
    @Test
    void getAll_sinFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el
        when(consejoService.getAll("", null, null)).thenReturn(mockConsejoList); //"" porque espera cadena vacia, si dejo null falla porqeu nunca recibe nulo

        mockMvc.perform(MockMvcRequestBuilders.get("/consejos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Luz directa"))
                .andExpect(jsonPath("$[0].explicacion").value("Coloca la planta con un mínimo de 2 horas de sol directo"))
                .andExpect(jsonPath("$[0].verificado").value(false))
                .andExpect(jsonPath("$[1].importancia").value(8.0f));


        verify(consejoService).getAll("", null, null);
    }

    @Test
    void getAll_byTitulo_Ok() throws Exception {
        when(consejoService.getAll("Luz directa", null, null)).thenReturn(mockConsejoList);

        mockMvc.perform(get("/consejos")
                        .queryParam("titulo", "Luz directa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Luz directa"));

        verify(consejoService).getAll("Luz directa", null, null);

    }

    @Test
    void getAll_byImportancia_Ok() throws Exception {
        when(consejoService.getAll("", null, 6.5f)).thenReturn(mockConsejoList);

        mockMvc.perform(get("/consejos")
                        .queryParam("importancia", "6.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].importancia").value(6.5f)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(consejoService).getAll("", null, 6.5f);
    }

    @Test
    void getAll_byVerificado_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(consejoService.getAll("", false, null)).thenReturn(mockConsejoList);

        mockMvc.perform(get("/consejos")
                        .queryParam("verificado", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].verificado").value(false)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(consejoService).getAll("", false, null);
    }

    @Test
    void getAll_allFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros
        when(consejoService.getAll("Luz directa", false, 6.5f)).thenReturn(mockConsejoList);

        mockMvc.perform(get("/consejos")
                        .queryParam("titulo", "Luz directa")
                        .queryParam("verificado", "false")
                        .queryParam("importancia", "6.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("Luz directa"))
                .andExpect(jsonPath("$[0].verificado").value(false))
                .andExpect(jsonPath("$[0].importancia").value("6.5")); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(consejoService).getAll("Luz directa", false, 6.5f);
    }


    //GetById **********************************************************
    //Response 200
    @Test
    void getById_Ok() throws Exception {

        when(consejoService.get(1)).thenReturn(mockConsejoOutDtoList.get(0));

        mockMvc.perform(get("/consejos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idConsejo", is(1)));

        verify(consejoService).get(1);
    }

    //Response 400
    @Test
    void getById_invalidPathVariable_400() throws Exception {
        mockMvc.perform(get("/consejos/abcd"))  // "abcd" no puede convertirse a long
                .andExpect(status().isBadRequest());

    }

    //Response 404
    @Test
    void getById_ConsejoNotFound_404() throws Exception {
        // Mockear que el servicio lanza PlantaNotFoundException
        when(consejoService.get(9999L)).thenThrow(new ConsejoNotFoundException());

        mockMvc.perform(get("/consejos/9999"))
                .andExpect(status().isNotFound());


    }


    //Add**********************************************************
    //Response 201
    @Test
    void Add_Ok() throws Exception {

        Consejo consejo = new Consejo( 1L, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                false, 6.5f, LocalDate.now(), null);

        String requestBody = """
                {
                    "titulo": "Luz directa",
                     "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo",
                     "verificado": false,
                     "importancia": 6.5
                }
                """;

        when(consejoService.add(any(ConsejoInDto.class))).thenReturn(consejo);

        mockMvc.perform(post("/consejos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Luz directa"))
                .andExpect(jsonPath("$.explicacion").value("Coloca la planta con un mínimo de 2 horas de sol directo"));


        verify(consejoService, times(2)).add(any(ConsejoInDto.class));


    }

    //Response 400
    @Test
    void Add_400() throws Exception {

        Consejo consejo = new Consejo( 1L, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                false, 6.5f, LocalDate.now(), null);


        String requestBody = """
                {
                     "titulo": null,
                     "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo.",
                     "verificado": false,
                     "importancia": 6.5
                }
                """;

        mockMvc.perform(post("/consejos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(consejoService, never()).add(any(ConsejoInDto.class));
    }

    //Response 404
    @Test
    void
    Add_PlantaNotFound404() throws Exception {

        String invalidRequestBody = """
            {
               "titulo": "Luz directa",
               "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo.",
               "verificado": false,
               "importancia": 6.5,
               "plantaIds": [999]
            }
        """;

        given(consejoService.add(any(ConsejoInDto.class)))
                .willThrow(new PlantaNotFoundException());

        mockMvc.perform(post("/consejos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isNotFound());

    }


    //Modify **********************************************************
    //Response 201
    @Test
    void Modify_Ok() throws Exception {

        long idConsejo = 1L;

        Consejo consejo = new Consejo( 1L, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                false, 6.5f, LocalDate.now(), null);
        ConsejoOutDto consejoOutDto = new ConsejoOutDto(1, "Luz directa", "Coloca la planta con un mínimo de 2 horas de sol directo",
                true, 8.0f, List.of(30L));

        when(consejoService.modify(eq(idConsejo), any(ConsejoInDto.class)))
                .thenReturn(consejoOutDto);

        String RequestBody = """
                {
                    "titulo": "Luz directa",
                     "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo",
                     "verificado": true,
                     "importancia": 8.0
                }
        """;

        mockMvc.perform(put("/consejos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Luz directa"))
                .andExpect(jsonPath("$.explicacion").value("Coloca la planta con un mínimo de 2 horas de sol directo"))
                .andExpect(jsonPath("$.idConsejo").value(1));

        verify(consejoService).modify(eq(idConsejo), any(ConsejoInDto.class));

    }

    //Response 400
    @Test
    void Modify_invalidPathVariable_400() throws Exception {
        // No necesito mockear el servicio porqeu nunca llegar, el error se provoca antes

        String requestBody = """
            {
                "titulo": "Luz directa",
                 "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo",
                 "verificado": true,
                 "importancia": 8.0
            }
            """;

        mockMvc.perform(put("/consejos/abc") // id inválido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    //Response 404 - consejoId inexistente en el path
    @Test
    void
    Modify_ConsejoNotFound_404() throws Exception{

        long idConsejo = 1L;

        String requestBody = """
            {
                "titulo": "Luz directa",
                 "explicacion": "Coloca la planta con un mínimo de 2 horas de sol directo",
                 "verificado": true,
                 "importancia": 8.0
            }
            """;

        // simulo que el servicio lanza el 404
        when(consejoService.modify(eq(idConsejo), any(ConsejoInDto.class)))
                .thenThrow(new ConsejoNotFoundException());

        mockMvc.perform(put("/consejos/{consejoId}", idConsejo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(consejoService).modify(eq(idConsejo), any(ConsejoInDto.class));
    }



    //Delete **********************************************************
    //Response 204
    @Test
    void Delete_Ok() throws Exception {

        mockMvc.perform(delete("/consejos/1"))
                .andExpect(status().isNoContent());

        verify(consejoService).remove(1L);
    }

    //Response 400
    @Test
    void Delete_invalidPathVariable_400() throws Exception {

        mockMvc.perform(delete("/consejos/abc"))  // "abc" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Delete_ConsejoNotFound404() throws Exception {

        // Simula que al intentar borrar la categoria con id 1 lanza CategoriaNotFoundException
        doThrow(new ConsejoNotFoundException()).when(consejoService).remove(99999L);

        mockMvc.perform(delete("/consejos/99999"))
                .andExpect(status().isNotFound());
    }

    //Response 409
    @Test
    void Delete_ConsejoConflict409() throws Exception {

        long idConsejo = 1L;
        // Simula que al intentar borrar el cuidado con id 1 lanza cuidadoNotFoundException
        doThrow(new ConsejoConflictException()).when(consejoService).remove(idConsejo);

        mockMvc.perform(delete("/consejos/{idConsejo}", idConsejo))
                .andExpect(status().isConflict());

        verify(consejoService).remove(idConsejo);


    }


}
