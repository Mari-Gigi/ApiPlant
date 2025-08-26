package com.svalero.ApiPlant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.ApiPlant.controller.CategoriaController;
import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.CategoriaOutDto;
import com.svalero.ApiPlant.domain.dto.CuidadoInDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.service.CategoriaService;
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


@WebMvcTest(CategoriaController.class)
public class CategoriaControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CategoriaService categoriaService;
    @MockBean
    private PlantaService plantaService;
    @BeforeEach
    void setUp() throws PlantaNotFoundException{
        when(plantaService.getAll(any(), any(), any())).thenReturn(Collections.emptyList());
    }

    private final List<Categoria> mockCategoriaList = List.of(
            new Categoria(1L, "ornamental", "cultivadas por su valor estetico", 3.0f, false, null, List.of(new Planta(1L))),
            new Categoria(2L, "epifitas", "sin sustrato", 3.0f, true, null, List.of(new Planta(1L)))
    );

    private final List<CategoriaOutDto> mockCategoriaOutDtoList = List.of(
            new CategoriaOutDto(1L, "ornamental", "cultivadas por su valor estetico", 3.0f, false, List.of(99L)),
            new CategoriaOutDto(2L, "epifitas", "sin sustrato", 3.0f, true, List.of(1L))
    );

    //GetAll ***************************************************
    //Response 200
    @Test
    void getAll_sinFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el
        when(categoriaService.getAll("", null, null)).thenReturn(mockCategoriaList); //"" porque espera cadena vacia, si dejo null falla porqeu nunca recibe nulo

        mockMvc.perform(MockMvcRequestBuilders.get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("ornamental"))
                .andExpect(jsonPath("$[0].descripcion").value("cultivadas por su valor estetico"))
                .andExpect(jsonPath("$[0].nivelDificultad").value(3.0))
                .andExpect(jsonPath("$[1].paraPrincipiantes").value(true));


        verify(categoriaService).getAll("", null, null);
    }

    @Test
    void getAll_byNombre_Ok() throws Exception {
        when(categoriaService.getAll("ornamental", null, null)).thenReturn(mockCategoriaList);

        mockMvc.perform(get("/categorias")
                        .queryParam("nombre", "ornamental"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("ornamental"));

        verify(categoriaService).getAll("ornamental", null, null);

    }

    @Test
    void getAll_byNivelDificultad_Ok() throws Exception {
        when(categoriaService.getAll("", 3.0f, null)).thenReturn(mockCategoriaList);

        mockMvc.perform(get("/categorias")
                        .queryParam("nivelDificultad", "3.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nivelDificultad").value(3.0f)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(categoriaService).getAll("", 3.0f, null);
    }

    @Test
    void getAll_byParaPrincipiantes_Ok() throws Exception {
        // Mockeo el servicio con parámetros vacios, excepto el filtro que quiero testar
        when(categoriaService.getAll("", null, false)).thenReturn(mockCategoriaList);

        mockMvc.perform(get("/categorias")
                        .queryParam("paraPrincipiantes", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].paraPrincipiantes").value(false)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(categoriaService).getAll("", null, false);
    }

    @Test
    void getAll_allFiltros_Ok() throws Exception {
        // Mockeo el servicio con parámetros
        when(categoriaService.getAll("ornamental", 3.0f, false)).thenReturn(mockCategoriaList);

        mockMvc.perform(get("/categorias")
                        .queryParam("nombre", "ornamental")
                        .queryParam("nivelDificultad", "3.0")
                        .queryParam("paraPrincipiantes", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("ornamental"))
                .andExpect(jsonPath("$[0].nivelDificultad").value("3.0"))
                .andExpect(jsonPath("$[0].paraPrincipiantes").value(false)); //evito poner el assert, le digo la posicion del json qeu tiene que reisar

        verify(categoriaService).getAll("ornamental", 3.0f, false);
    }

    //Response 400
    @Test
    void getAll_invalidPathVariable_400() throws Exception {
        mockMvc.perform(get("/categorias")
                        .queryParam("paraPrincipiantes", "ghfghfs"))
                .andExpect(status().isBadRequest());
    }

    //Response 404
   @Test
    void getAll_byNombreNotFound_404() throws Exception {
        when(categoriaService.getAll(eq("nombreInexistente"),  nullable(Float.class), any())) //pongo nullnable porque sino recibe el null vacio, entiende que no hay filtro y devuelve un 200
                .thenThrow(new CategoriaNotFoundException());

        mockMvc.perform(get("/categorias")
                        .queryParam("nombre", "nombreInexistente"))
                .andExpect(status().isNotFound());

    }


    //GetById **********************************************************
    //Response 200
    @Test
    void getById_Ok() throws Exception {

        when(categoriaService.get(1)).thenReturn(mockCategoriaOutDtoList.get(0));

        mockMvc.perform(get("/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria", is(1)));

        verify(categoriaService).get(1);
    }

    //Response 400
    @Test
    void getById_invalidPathVariable_400() throws Exception {
        mockMvc.perform(get("/categorias/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());

    }

    //Response 404
    @Test
    void getById_CategoriaNotFound_404() throws Exception {
        // Mockear que el servicio lanza PlantaNotFoundException
        when(categoriaService.get(9999L)).thenThrow(new CategoriaNotFoundException());

        mockMvc.perform(get("/categorias/9999"))
                .andExpect(status().isNotFound());


    }



    //Add**********************************************************
    //Response 201
    @Test
    void Add_Ok() throws Exception {

        CategoriaInDto categoriaInDto = new CategoriaInDto("ornamental", "cultivadas por su valor estetico",
                3.0f, false, List.of(99L));


        CategoriaOutDto categoriaOutDto = new CategoriaOutDto(1L, "ornamental", "cultivadas por su valor estetico",
                3.0f, false, List.of(99L));

        String requestBody = """
                {
                    "nombre": "ornamental",
                    "descripcion": "cultivadas por su valor estetico",
                    "nivelDificultad": 3.0,
                    "paraPrincipiantes": false,
                    "plantaIds": [99]
                }
                """;

        when(categoriaService.addCategoria(any(CategoriaInDto.class))).thenReturn(categoriaOutDto);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("ornamental"))
                .andExpect(jsonPath("$.descripcion").value("cultivadas por su valor estetico"))
                .andExpect(jsonPath("$.plantaIds").value(99));


        verify(categoriaService).addCategoria(any(CategoriaInDto.class));

    }

    //Response 400
    @Test
    void Add_400() throws Exception {

        CategoriaInDto categoriaInDto = new CategoriaInDto("ornamental", "cultivadas por su valor estetico",
                3.0f, false, List.of(99L));

        String invalidRequestBody = """
                {
                    "nombre": null,
                    "descripcion": "cultivadas por su valor estetico",
                    "nivelDificultad": 3.0,
                    "paraPrincipiantes": false,
                    "plantaIds": [99]
                }
                """;

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(categoriaService, never()).addCategoria(any(CategoriaInDto.class));
    }

    //Response 404
    @Test
    void
    Add_PlantaNotFound404() throws Exception {

        String invalidRequestBody = """
            {
              "nombre": "ornamental",
              "descripcion": "cultivadas por su valor estetico",
              "nivelDificultad": 3.0,
              "paraPrincipiantes": false,
              "plantaIds": [999]
            }
        """;

        given(categoriaService.addCategoria(any(CategoriaInDto.class)))
                .willThrow(new PlantaNotFoundException());

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isNotFound());


    }



    //Modify **********************************************************
    //Response 201
    @Test
    void Modify_Ok() throws Exception {

        long idCategoria = 1L;

        CategoriaInDto categoriaInDto = new CategoriaInDto("ornamental", "cultivadas por su valor estetico",
                3.0f, false, List.of(99L));
        CategoriaOutDto categoriaOutDto = new CategoriaOutDto(1L, "ornamental", "cultivadas por su valor estetico",
                3.0f, true, List.of(99L));

        when(categoriaService.modify(eq(idCategoria), any(CategoriaInDto.class))).thenReturn(categoriaOutDto);

        String RequestBody = """
                {
                   "nombre": "ornamental",
                    "descripcion": "cultivadas por su valor estetico",
                    "nivelDificultad": 3.0,
                    "paraPrincipiantes": true,
                    "plantaIds": [99]
                }
                """;

        mockMvc.perform(put("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("ornamental"))
                .andExpect(jsonPath("$.descripcion").value("cultivadas por su valor estetico"))
                .andExpect(jsonPath("$.idCategoria").value(1));

        verify(categoriaService).modify(eq(idCategoria), any(CategoriaInDto.class));
    }

    //Response 400
    @Test
    void Modify_invalidPathVariable_400() throws Exception {
        // No necesito mockear el servicio porqeu nunca llegar, el error se provoca antes

        String requestBody = """
            {
                "nombre": "ornamental",
                "descripcion": "cultivadas por su valor estetico",
                "nivelDificultad": 3.0,
                "paraPrincipiantes": true,
                "plantaIds": [99]
            }
            """;

        mockMvc.perform(put("/categorias/abc") // id inválido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Modify_CategoriaNotFound_404() throws Exception{

        long idCategoria = 1L;

        String requestBody = """
            {
                "nombre": "ornamental",
                "descripcion": "cultivadas por su valor estetico",
                "nivelDificultad": 3.0,
                "paraPrincipiantes": true,
                "plantaIds": [99]
            }
            """;

        // simulo que el servicio lanza el 404
        when(categoriaService.modify(eq(idCategoria), any(CategoriaInDto.class)))
                .thenThrow(new CategoriaNotFoundException());

        mockMvc.perform(put("/categorias/{categoriaId}", idCategoria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(categoriaService).modify(eq(idCategoria), any(CategoriaInDto.class));
    }

    //Response 409
    @Test
    void Modify_plantaConflict_409() throws Exception{

        long idCategoria = 1L;

        String requestBody = """
            {
                "nombre": "ornamental",
                "descripcion": "cultivadas por su valor estetico",
                "nivelDificultad": 3.0,
                "paraPrincipiantes": true,
                "plantaIds": [99]
            }
            """;

        // simulo que el servicio lanza el 409
        when(categoriaService.modify(eq(idCategoria), any(CategoriaInDto.class)))
                .thenThrow(new CategoriaConflictException());

        mockMvc.perform(put("/categorias/{categoriaId}", idCategoria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());

        verify(categoriaService).modify(eq(idCategoria), any(CategoriaInDto.class));
    }



    //Delete **********************************************************
    //Response 204
    @Test
    void Delete_Ok() throws Exception {

        mockMvc.perform(delete("/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).remove(1L);
    }

    //Response 400
    @Test
    void Delete_invalidPathVariable_400() throws Exception {

        mockMvc.perform(delete("/categorias/khkjgjkg"))  // "khkjgjkg" no puede convertirse a long
                .andExpect(status().isBadRequest());
    }

    //Response 404
    @Test
    void Delete_CategoriaNotFound404() throws Exception {

        // Simula que al intentar borrar la categoria con id 1 lanza CategoriaNotFoundException
        doThrow(new CategoriaNotFoundException()).when(categoriaService).remove(99999L);

        mockMvc.perform(delete("/categorias/99999"))
                .andExpect(status().isNotFound());
    }

    //Response 409
    @Test
    void Delete_CategoriaConflict409() throws Exception {

        long idCategoria = 1L;
        // Simula que al intentar borrar el cuidado con id 1 lanza cuidadoNotFoundException
        doThrow(new CategoriaConflictException()).when(categoriaService).remove(idCategoria);

        mockMvc.perform(delete("/categorias/{idCategoria}", idCategoria))
                .andExpect(status().isConflict());

        verify(categoriaService).remove(idCategoria);


    }


}
