package com.svalero.ApiPlant;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.CategoriaInDto;
import com.svalero.ApiPlant.domain.dto.CategoriaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;
import com.svalero.ApiPlant.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class CategoriaServiceTests {


    @InjectMocks
    CategoriaService categoriaService;
    @Mock
    PlantaRepository plantaRepository;
    @Mock
    CategoriaRepository categoriaRepository;
    @Mock
    private ModelMapper modelMapper;

    private final List<Categoria> mockCategoriaList = List.of(
            new Categoria(1L, "ornamental", "cultivadas por su valor estetico", 3.0f, false, null, List.of(new Planta(1L))),
            new Categoria(2L, "epifitas", "sin sustrato", 3.0f, true, null, List.of(new Planta(1L)))
    );

    private final List<CategoriaOutDto> mockCategoriaOutDtoList = List.of(
            new CategoriaOutDto(1L, "ornamental", "cultivadas por su valor estetico", 3.0f, false, List.of(99L))
    );

    @Test
    public void testGetAll() throws CategoriaNotFoundException {


        when(categoriaRepository.findAll()).thenReturn(mockCategoriaList);

        List<Categoria> resultado = categoriaService.getAll(null, null, null);

        assertEquals(2, resultado.size());
        assertEquals("ornamental", resultado.get(0).getNombre());
        assertEquals("epifitas", resultado.get(1).getNombre());


        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_ByNombre() throws CategoriaNotFoundException {

        String nombre = "epifitas";

        List<Categoria> filtroNombre = mockCategoriaList.stream()
                .filter(categoria -> categoria.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();

        when(categoriaRepository.findByNombreContainingIgnoreCase(nombre)).thenReturn(filtroNombre);

        List<Categoria> resultado = categoriaService.getAll(nombre, null, null);

        assertEquals(filtroNombre.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase())));

        verify(categoriaRepository, times(1)).findByNombreContainingIgnoreCase(nombre);
    }

    @Test
    void testGetAll_ByNivelDificultad() throws CategoriaNotFoundException {
        Float nivelDificultad = 3.0f;

        List<Categoria> filtroNivelDificultad = mockCategoriaList.stream()
                .filter(categoria -> nivelDificultad.equals(categoria.getNivelDificultad()))
                .toList();

        when(categoriaRepository.findByNivelDificultad(nivelDificultad)).thenReturn(filtroNivelDificultad);


        List<Categoria> resultado = categoriaService.getAll(null, nivelDificultad, null);

        assertEquals(filtroNivelDificultad.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> nivelDificultad.equals(p.getNivelDificultad())));

        verify(categoriaRepository, times(1)).findByNivelDificultad(nivelDificultad);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByPrincipiantesTrue() throws CategoriaNotFoundException {

        boolean paraPrincipiantes = true;

        List<Categoria> filtroPrincipiantes = mockCategoriaList.stream()
                .filter(Categoria::isParaPrincipiantes)
                .toList();

        when(categoriaRepository.findByParaPrincipiantes(paraPrincipiantes)).thenReturn(filtroPrincipiantes);

        for (int i = 0; i < filtroPrincipiantes.size(); i++) {
            when(modelMapper.map(filtroPrincipiantes.get(i), Categoria.class)).thenReturn(mockCategoriaList.get(i));
        }

        when(modelMapper.map(filtroPrincipiantes, new TypeToken<List<Categoria>>() {
        }.getType()))
                .thenReturn(mockCategoriaList.stream()
                        .filter(Categoria::isParaPrincipiantes)
                        .toList());

        List<Categoria> resultado = categoriaService.getAll(null, null, true);

        assertEquals(filtroPrincipiantes.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(Categoria::isParaPrincipiantes));

        verify(categoriaRepository, times(1)).findByParaPrincipiantes(paraPrincipiantes);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByPrincipiantesFalse() throws CategoriaNotFoundException {

        boolean paraPrincipiantes = false;

        List<Categoria> filtroPrincipiantes = mockCategoriaList.stream()
                .filter(categoria -> !categoria.isParaPrincipiantes()) // Filtro por false
                .toList();

        when(categoriaRepository.findByParaPrincipiantes(paraPrincipiantes)).thenReturn(filtroPrincipiantes);

        for (int i = 0; i < filtroPrincipiantes.size(); i++) {
            when(modelMapper.map(filtroPrincipiantes.get(i), Categoria.class))
                    .thenReturn(mockCategoriaList.get(i));
        }

        when(modelMapper.map(filtroPrincipiantes, new TypeToken<List<Categoria>>() {
        }.getType()))
                .thenReturn(mockCategoriaList.stream()
                        .filter(dto -> !dto.isParaPrincipiantes())
                        .toList());

        List<Categoria> resultado = categoriaService.getAll(null, null, false);

        assertEquals(filtroPrincipiantes.size(), resultado.size());
        assertTrue(resultado.stream().noneMatch(Categoria::isParaPrincipiantes));

        verify(categoriaRepository, times(1)).findByParaPrincipiantes(paraPrincipiantes);
    }

    @Test
    public void testGetById() throws CategoriaNotFoundException {

        long id = 1;
        // Cuidado de prueba con plantas asociadas
        List<Planta> plantas = List.of(new Planta(1L));
        Categoria mockCategoria = new Categoria(id,  "ornamental", "cultivadas por su valor estetico", 3.0f,
                false, null, plantas);


        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(categoriaRepository.findById(id)).thenReturn(Optional.of(mockCategoria));

        //EJECUCION DEL METODO DEL SERVICIO
        CategoriaOutDto result = categoriaService.get(id);

        //VERIFICACION DE LOS CAMPOS DEVUELTOS
        assertEquals(1, result.getIdCategoria());
        assertEquals("ornamental", result.getNombre());
        assertEquals("cultivadas por su valor estetico", result.getDescripcion());
        assertEquals(3.0f, result.getNivelDificultad());
        assertFalse(result.isParaPrincipiantes());
        assertEquals(List.of(1L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS METODOS MOCKEADOS SE HAYAN LLAMADO UNA VEZ
        verify(categoriaRepository, times(1)).findById(id);

    }

    @Test
    public void testAdd() throws PlantaNotFoundException {

        //DEFINO EL OBJETO DE ENTRADA (cuidadoINDTO)
        CategoriaInDto categoriaInDto = new CategoriaInDto("ornamental", "cultivadas por su valor estetico", 3.0f,
                false, List.of(1L));

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Categoria categoriaMapped = new Categoria(1, "ornamental", "cultivadas por su valor estetico", 3.0f,
                false, null, null);

        //CONFIGURO LOS MAPEOS
        Planta mockPlanta = new Planta();
        mockPlanta.setId_planta(1L);
        Categoria mockCategoria = mockCategoriaList.get(0);

        //INDICO QUE ESPERO A LA SALIDA (EL OBJETO ESPERADO cuidadoOUTDTO)
        CategoriaOutDto expectedDto = mockCategoriaOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(modelMapper.map(categoriaInDto, Categoria.class)).thenReturn(categoriaMapped);
        when(plantaRepository.findAllById(List.of(1L))).thenReturn(List.of(mockPlanta));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(mockCategoria);
        when(modelMapper.map(mockCategoria, CategoriaOutDto.class)).thenReturn(expectedDto);

        //EJECUTO EL METODO ADD DEL SERVICIO
        CategoriaOutDto result = categoriaService.addCategoria(categoriaInDto);

        //COMPROBACION DE QUE LA SALIDA ES LO QUE SE ESPERABA
        assertEquals(1, result.getIdCategoria());
        assertEquals("ornamental", result.getNombre());
        assertEquals("cultivadas por su valor estetico", result.getDescripcion());
        assertEquals(3.0f, result.getNivelDificultad());
        assertFalse(result.isParaPrincipiantes());
        assertEquals(List.of(1L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS MOCKS FUNCIONARON OK
        verify(modelMapper).map(categoriaInDto, Categoria.class);
        verify(plantaRepository).findAllById(List.of(1L));
        verify(categoriaRepository).save(categoriaMapped);
        verify(modelMapper).map(mockCategoria, CategoriaOutDto.class);

    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testAdd_plantaNoExiste() throws PlantaNotFoundException {

        //DEFINO EL OBJETO DE ENTRADA (categoriaINDTO)
        CategoriaInDto categoriaInDto = new CategoriaInDto("ornamental", "cultivadas por su valor estetico", 3.0f,
                false, List.of(99L));

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Categoria categoriaMapped = new Categoria(1,"ornamental", "cultivadas por su valor estetico", 3.0f,
                false, null, null);

        when(modelMapper.map(categoriaInDto, Categoria.class)).thenReturn(categoriaMapped);
        when(plantaRepository.findAllById(List.of(99L))).thenReturn(Collections.emptyList());

        assertThrows(PlantaNotFoundException.class, () -> {
            categoriaService.addCategoria(categoriaInDto);
        });

        verify(modelMapper).map(categoriaInDto, Categoria.class);
        verify(plantaRepository).findAllById(List.of(99L));
    }

 /*   @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testModify() throws CuidadoNotFoundException, CuidadoConflictException {

        Planta plantaMock = new Planta(1L);
        long idCuidado = 1L;

        //definicion de los nuevos datos qeu quiero introducir
        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Arcilloso", 80f, List.of(1L));

        // simulacion de la planta que hemos definido arriba, indicandole que la inicial el la posicion 0 de mockPlantList
        when(cuidadoRepository.findById(idCuidado)).thenReturn(Optional.of(mockCuidadoList.get(0)));
        when(plantaRepository.findById(1L)).thenReturn(Optional.of(mock(Planta.class)));

        //le indico qeu dto (el de posicion 0 con id=1) quiero que me devuelva el repositorio
        CuidadoOutDto expectedDto = mockCuidadoOutDtoList.get(0);
        when(modelMapper.map(any(Cuidado.class), ArgumentMatchers.eq(CuidadoOutDto.class))).thenReturn(expectedDto); //cuando llames al map con cualqueir objeto de tipo planta
        //que se quiera convertir a plantaOutDto, devuelveme el expectedDto (el que quiero que me devuelva el repositorio)

        // Ejecuta el modify
        CuidadoOutDto result = cuidadoService.modify(idCuidado, cuidadoInDto);

        // Comporbamos si el resultado coincide con lo esperado
        assertEquals(1, result.getIdCuidado());
        assertTrue(result.isEsInterior());
        assertEquals("frecuente", result.getRiego());
        assertEquals("arcilloso", result.getSustrato());
        assertEquals(80f, result.getHumedad());
        assertEquals(List.of(1L), result.getPlantaIds());

        // Verificaciones
        verify(cuidadoRepository).findById(idCuidado);
        verify(plantaRepository).findById(1L);
        verify(modelMapper).map(cuidadoInDto, mockCuidadoList.get(0));
        verify(cuidadoRepository).save(mockCuidadoList.get(0));
        verify(modelMapper).map(mockCuidadoList.get(0), CuidadoOutDto.class);
    }*/

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testDeleteOk() throws CategoriaNotFoundException, CategoriaConflictException {

        long idCategoria = 1;
        Categoria mockCategoria = mock(Categoria.class);

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(mockCategoria)); //simulamos que la categoria existe
        when(plantaRepository.findByCuidado_IdCuidado(idCategoria)).thenReturn(List.of()); //simulacion de que hay plantas associadas a esa categoria

        categoriaService.remove(idCategoria);

        verify(categoriaRepository).findById(idCategoria);
        verify(plantaRepository).findByCategoria_IdCategoria(idCategoria);
        verify(categoriaRepository).deleteById(idCategoria);

    }

    @Test
    public void testDeleteCategoria_NotFound() {
        long idCategoria= 1L;

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.empty());

        assertThrows(CategoriaNotFoundException.class, () -> {
            categoriaService.remove(idCategoria);
        });

        verify(categoriaRepository).findById(idCategoria);
        verify(plantaRepository, never()).findByCategoria_IdCategoria(anyLong());
        verify(categoriaRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteCategoria_Conflict() {
        long idCategoria = 1L;
        Categoria mockCategoria = mock(Categoria.class);
        Planta planta = new Planta();

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(mockCategoria));
        when(plantaRepository.findByCategoria_IdCategoria(idCategoria)).thenReturn(List.of(planta));

        assertThrows(CategoriaConflictException.class, () -> {
            categoriaService.remove(idCategoria);
        });

        verify(categoriaRepository).findById(idCategoria);
        verify(plantaRepository).findByCategoria_IdCategoria(idCategoria);
        verify(categoriaRepository, never()).deleteById(anyLong());
    }


}
