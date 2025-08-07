package com.svalero.ApiPlant;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.CuidadoInDto;
import com.svalero.ApiPlant.domain.dto.CuidadoOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;
import com.svalero.ApiPlant.service.CuidadoService;
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
public class CuidadoServiceTests {


    @InjectMocks
    CuidadoService cuidadoService;
    @Mock
    PlantaRepository plantaRepository;
    @Mock
    CuidadoRepository cuidadoRepository;
    @Mock
    private ModelMapper modelMapper;

    private final List<Cuidado> mockCuidadoList = List.of(
            new Cuidado(1L, true, "frecuente", "Tierra ácida", 80.0f, null, List.of(new Planta(1L))),
            new Cuidado(2L, false, "moderado", "Tierra neutra", 60.0f, null, null)
    );

    private final List<CuidadoOutDto> mockCuidadoOutDtoList = List.of(
            new CuidadoOutDto(1L, true, "frecuente", "Tierra ácida", 80.0f, List.of(99L))
    );

    @Test
    public void testGetAll() throws CuidadoNotFoundException {


        when(cuidadoRepository.findAll()).thenReturn(mockCuidadoList);

        List<Cuidado> resultado = cuidadoService.getAll(null, null, null);

        assertEquals(2, resultado.size());
        assertEquals("frecuente", resultado.get(0).getRiego());
        assertEquals("Tierra neutra", resultado.get(1).getSustrato());
        assertEquals(60.0f, resultado.get(1).getHumedad());


        verify(cuidadoRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_ByRiego() throws CuidadoNotFoundException {

        String riego = "moderado";

        List<Cuidado> filtroRiego = mockCuidadoList.stream()
                .filter(cuidado -> cuidado.getRiego().toLowerCase().contains(riego.toLowerCase()))
                .toList();

        when(cuidadoRepository.findByRiegoContainingIgnoreCase(riego)).thenReturn(filtroRiego);

        List<Cuidado> resultado = cuidadoService.getAll(riego, null, null);

        assertEquals(filtroRiego.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getRiego().toLowerCase().contains("moderado")));

        verify(cuidadoRepository, times(1)).findByRiegoContainingIgnoreCase(riego);
    }

    @Test
    void testGetAll_BySustrato() throws CuidadoNotFoundException {

        String sustrato = "tierra";

        List<Cuidado> filtroSustrato = mockCuidadoList.stream()
                .filter(cuidado -> cuidado.getSustrato().toLowerCase().contains(sustrato.toLowerCase()))
                .toList();

        when(cuidadoRepository.findBySustratoContainingIgnoreCase(sustrato)).thenReturn(filtroSustrato);

        List<Cuidado> resultado = cuidadoService.getAll(null, sustrato, null);

        assertEquals(filtroSustrato.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getSustrato().toLowerCase().contains("tierra")));

        verify(cuidadoRepository, times(1)).findBySustratoContainingIgnoreCase(sustrato);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsInteriorTrue() throws CuidadoNotFoundException {

        boolean esInterior = true;

        List<Cuidado> filtroUbicacion = mockCuidadoList.stream()
                .filter(Cuidado::isEsInterior)
                .toList();

        when(cuidadoRepository.findByEsInterior(esInterior)).thenReturn(filtroUbicacion);

        for (int i = 0; i < filtroUbicacion.size(); i++) {
            when(modelMapper.map(filtroUbicacion.get(i), Cuidado.class)).thenReturn(mockCuidadoList.get(i));
        }

        when(modelMapper.map(filtroUbicacion, new TypeToken<List<Cuidado>>() {
        }.getType()))
                .thenReturn(mockCuidadoList.stream()
                        .filter(Cuidado::isEsInterior)
                        .toList());

        List<Cuidado> resultado = cuidadoService.getAll(null, null, true);

        assertEquals(filtroUbicacion.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(Cuidado::isEsInterior));

        verify(cuidadoRepository, times(1)).findByEsInterior(esInterior);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsInteriorFalse() throws CuidadoNotFoundException {

        boolean esInterior = false;

        List<Cuidado> filtroUbicacion = mockCuidadoList.stream()
                .filter(cuidado -> !cuidado.isEsInterior()) // Filtro por false
                .toList();

        when(cuidadoRepository.findByEsInterior(esInterior)).thenReturn(filtroUbicacion);

        for (int i = 0; i < filtroUbicacion.size(); i++) {
            when(modelMapper.map(filtroUbicacion.get(i), Cuidado.class))
                    .thenReturn(mockCuidadoList.get(i));
        }

        when(modelMapper.map(filtroUbicacion, new TypeToken<List<Cuidado>>() {
        }.getType()))
                .thenReturn(mockCuidadoList.stream()
                        .filter(dto -> !dto.isEsInterior())
                        .toList());

        List<Cuidado> resultado = cuidadoService.getAll(null, null, false);

        assertEquals(filtroUbicacion.size(), resultado.size());
        assertTrue(resultado.stream().noneMatch(Cuidado::isEsInterior));

        verify(cuidadoRepository, times(1)).findByEsInterior(esInterior);
    }

    @Test
    public void testGetById() throws CuidadoNotFoundException {

        long id = 1;
        // Cuidado de prueba con plantas asociadas
        List<Planta> plantas = List.of(new Planta(1L));
        Cuidado mockCuidado = new Cuidado(id, true, "frecuente", "Tierra ácida", 80.0f, null, plantas);


        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(cuidadoRepository.findById(id)).thenReturn(Optional.of(mockCuidado));

        //EJECUCION DEL METODO DEL SERVICIO
        CuidadoOutDto result = cuidadoService.get(id);

        //VERIFICACION DE LOS CAMPOS DEVUELTOS
        assertEquals(1, result.getIdCuidado());
        assertTrue(result.isEsInterior());
        assertEquals("frecuente", result.getRiego());
        assertEquals("Tierra ácida", result.getSustrato());
        assertEquals(80f, result.getHumedad());
        assertEquals(List.of(1L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS METODOS MOCKEADOS SE HAYAN LLAMADO UNA VEZ
        verify(cuidadoRepository, times(1)).findById(id);

    }

    @Test
    public void testAdd() throws PlantaNotFoundException {

        //DEFINO EL OBJETO DE ENTRADA (cuidadoINDTO)
        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Tierra ácida", 80.0f, List.of(1L));

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Cuidado cuidadoMapped = new Cuidado(1, true, "frecuente", "Tierra ácida", 80.0f, null, null);

        //CONFIGURO LOS MAPEOS
        Planta mockPlanta = new Planta();
        mockPlanta.setId_planta(1L);
        Cuidado mockCuidado = mockCuidadoList.get(0);

        //INDICO QUE ESPERO A LA SALIDA (EL OBJETO ESPERADO cuidadoOUTDTO)
        CuidadoOutDto expectedDto = mockCuidadoOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(modelMapper.map(cuidadoInDto, Cuidado.class)).thenReturn(cuidadoMapped);
        when(plantaRepository.findAllById(List.of(1L))).thenReturn(List.of(mockPlanta));
        when(cuidadoRepository.save(any(Cuidado.class))).thenReturn(mockCuidado);
        when(modelMapper.map(mockCuidado, CuidadoOutDto.class)).thenReturn(expectedDto);

        //EJECUTO EL METODO ADD DEL SERVICIO
        CuidadoOutDto result = cuidadoService.addCuidado(cuidadoInDto);

        //COMPROBACION DE QUE LA SALIDA ES LO QUE SE ESPERABA
        assertEquals(1, result.getIdCuidado());
        assertTrue(result.isEsInterior());
        assertEquals("frecuente", result.getRiego());
        assertEquals("Tierra ácida", result.getSustrato());
        assertEquals(80f, result.getHumedad());
        assertEquals(List.of(1L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS MOCKS FUNCIONARON OK
        verify(modelMapper).map(cuidadoInDto, Cuidado.class);
        verify(plantaRepository).findAllById(List.of(1L));
        verify(cuidadoRepository).save(cuidadoMapped);
        verify(modelMapper).map(mockCuidado, CuidadoOutDto.class);

    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testAdd_plantaNoExiste() throws PlantaNotFoundException {

        //DEFINO EL OBJETO DE ENTRADA (cuidadoINDTO)
        CuidadoInDto cuidadoInDto = new CuidadoInDto(true, "frecuente", "Tierra ácida", 80.0f, List.of(99L));

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Cuidado cuidadoMapped = new Cuidado(99, true, "frecuente", "Tierra ácida", 80.0f, null, null);

        when(modelMapper.map(cuidadoInDto, Cuidado.class)).thenReturn(cuidadoMapped);
        when(plantaRepository.findAllById(List.of(99L))).thenReturn(Collections.emptyList());

        assertThrows(PlantaNotFoundException.class, () -> {
            cuidadoService.addCuidado(cuidadoInDto);
        });

        verify(modelMapper).map(cuidadoInDto, Cuidado.class);
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
    public void testDeleteOk() throws CuidadoNotFoundException, CuidadoConflictException {

        long idCuidado = 1;
        Cuidado mockCuidado = mock(Cuidado.class);

        when(cuidadoRepository.findById(idCuidado)).thenReturn(Optional.of(mockCuidado)); //simulamos que el cuidado existe
        when(plantaRepository.findByCuidado_IdCuidado(idCuidado)).thenReturn(List.of()); //simulacion de que hay plantas associadas a ese cuidado

        cuidadoService.remove(idCuidado);

        verify(cuidadoRepository).findById(idCuidado);
        verify(plantaRepository).findByCuidado_IdCuidado(idCuidado);
        verify(cuidadoRepository).deleteById(idCuidado);

    }

    @Test
    public void testDeleteCuidado_NotFound() {
        long idCuidado = 1L;

        when(cuidadoRepository.findById(idCuidado)).thenReturn(Optional.empty());

        assertThrows(CuidadoNotFoundException.class, () -> {
            cuidadoService.remove(idCuidado);
        });

        verify(cuidadoRepository).findById(idCuidado);
        verify(plantaRepository, never()).findByCuidado_IdCuidado(anyLong());
        verify(cuidadoRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteCuidado_Conflict() {
        long idCuidado = 1L;
        Cuidado mockCuidado = mock(Cuidado.class);
        Planta planta = new Planta();

        when(cuidadoRepository.findById(idCuidado)).thenReturn(Optional.of(mockCuidado));
        when(plantaRepository.findByCuidado_IdCuidado(idCuidado)).thenReturn(List.of(planta));

        assertThrows(CuidadoConflictException.class, () -> {
            cuidadoService.remove(idCuidado);
        });

        verify(cuidadoRepository).findById(idCuidado);
        verify(plantaRepository).findByCuidado_IdCuidado(idCuidado);
        verify(cuidadoRepository, never()).deleteById(anyLong());
    }

}
