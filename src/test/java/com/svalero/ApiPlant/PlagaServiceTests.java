package com.svalero.ApiPlant;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.*;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;
import com.svalero.ApiPlant.service.PlagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlagaServiceTests {

    @InjectMocks
    PlagaService plagaService;
    @Mock
    PlagaRepository plagaRepository;
    @Mock
    PlantaRepository plantaRepository;
    @Mock
    private ModelMapper modelMapper;

    private final List<Plaga> mockPlagaList = List.of(
            new Plaga(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza", 6.5f, true, "jabón potasico", null, List.of(new Planta(50L))),
            new Plaga(2, "Araña roja", "Punteado amarillento o blanquecino en las hojas", 7.5f, true, "acaricida", null, List.of(new Planta(30L)))
    );

    private final List<PlagaOutDto> mockPlagaOutDtoList = List.of(
            new PlagaOutDto(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza", 6.5f, true, "jabón potasico",  List.of(50L)),
            new PlagaOutDto(2, "Araña roja", "Punteado amarillento o blanquecino en las hojas", 7.5f, true, "acaricida",List.of(30L))
    );

    @Test
    public void testGetAll() throws PlagaNotFoundException {


        when(plagaRepository.findAll()).thenReturn(mockPlagaList);

        List<Plaga> resultado = plagaService.getAll(null, null, null);

        assertEquals(2, resultado.size());  //cuantos registros va a encontrar
        assertEquals("Cochinilla algodonosa", resultado.get(0).getNombre());
        assertEquals(6.5f, resultado.get(0).getRiesgo());
        assertEquals("Araña roja", resultado.get(1).getNombre());

        verify(plagaRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_ByNombre() throws PlagaNotFoundException {

        String nombre = "araña roja";

        List<Plaga> filtroNombre = mockPlagaList.stream()
                .filter(plaga -> plaga.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();

        when(plagaRepository.findByNombreContainingIgnoreCase(nombre)).thenReturn(filtroNombre);

        List<Plaga> resultado = plagaService.getAll(nombre, null, null);

        assertEquals(filtroNombre.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase())));

        verify(plagaRepository, times(1)).findByNombreContainingIgnoreCase(nombre);
    }

    @Test
    void testGetAll_ByRiesgo() throws PlagaNotFoundException {
        Float riesgo = 6.5f;

        List<Plaga> filtroRiesgo = mockPlagaList.stream()
                .filter(plaga -> riesgo.equals(plaga.getRiesgo()))
                .toList();

        when(plagaRepository.findByRiesgo(riesgo)).thenReturn(filtroRiesgo);


        List<Plaga> resultado = plagaService.getAll(null, riesgo, null);

        assertEquals(filtroRiesgo.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> riesgo.equals(p.getRiesgo())));

        verify(plagaRepository, times(1)).findByRiesgo(riesgo);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsLetalTrue() throws PlagaNotFoundException {

        boolean esLetal = true;

        List<Plaga> filtroLetal = mockPlagaList.stream()
                .filter(Plaga::isEsLetal)
                .toList();

        when(plagaRepository.findByEsLetal(esLetal)).thenReturn(filtroLetal);

        for (int i = 0; i < filtroLetal.size(); i++) {
            when(modelMapper.map(filtroLetal.get(i), Plaga.class)).thenReturn(mockPlagaList.get(i));
        }

        when(modelMapper.map(filtroLetal, new TypeToken<List<Plaga>>() {
        }.getType()))
                .thenReturn(mockPlagaList.stream()
                        .filter(Plaga::isEsLetal)
                        .toList());

        List<Plaga> resultado = plagaService.getAll(null, null, true);

        assertEquals(filtroLetal.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(Plaga::isEsLetal));

        verify(plagaRepository, times(1)).findByEsLetal(esLetal);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsLetalFalse() throws PlagaNotFoundException {

        boolean esLetal = false;

        List<Plaga> filtroLetal = mockPlagaList.stream()
                .filter(Plaga::isEsLetal)
                .toList();

        when(plagaRepository.findByEsLetal(esLetal)).thenReturn(filtroLetal);

        for (int i = 0; i < filtroLetal.size(); i++) {
            when(modelMapper.map(filtroLetal.get(i), Plaga.class)).thenReturn(mockPlagaList.get(i));
        }

        when(modelMapper.map(filtroLetal, new TypeToken<List<Plaga>>() {
        }.getType()))
                .thenReturn(mockPlagaList.stream()
                        .filter(dto -> !dto.isEsLetal())
                        .toList());

        List<Plaga> resultado = plagaService.getAll(null, null, false);

        assertEquals(filtroLetal.size(), resultado.size());
        assertFalse(resultado.stream().noneMatch(Plaga::isEsLetal));

        verify(plagaRepository, times(1)).findByEsLetal(esLetal);
    }

    @Test
    public void testGetById() throws PlagaNotFoundException {

        long id = 1;
        // Plaga de prueba con plantas asociadas
        List<Planta> plantas = List.of(new Planta(50L));
        Plaga mockPlaga= new Plaga(id, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza", 6.5f, true, "jabón potasico",  null, plantas);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(plagaRepository.findById(id)).thenReturn(Optional.of(mockPlaga));

        //EJECUCION DEL METODO DEL SERVICIO
        PlagaOutDto result = plagaService.get(id);

        //VERIFICACION DE LOS CAMPOS DEVUELTOS
        assertEquals(1, result.getIdPlaga());
        assertEquals("Cochinilla algodonosa", result.getNombre());
        assertEquals("Presencia de masas blancas y algodonosas con melaza", result.getSintomas());
        assertEquals(6.5f, result.getRiesgo());
        assertTrue(result.isEsLetal());
        assertEquals(List.of(50L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS METODOS MOCKEADOS SE HAYAN LLAMADO UNA VEZ
        verify(plagaRepository, times(1)).findById(id);

    }

    @Test
    public void testAdd()  {

        //DEFINO EL OBJETO DE ENTRADA (plagaINDTO)
        PlagaInDto plagaInDto = new PlagaInDto("Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza", 6.5f, true,"jabón potasico",null);

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Plaga plagaMapped = new Plaga(1, "Cochinilla algodonosa", "Presencia de masas blancas y algodonosas con melaza", 6.5f, true,"jabón potasico", null,  null);

        //CONFIGURO LOS MAPEOS

        Plaga mockPlaga = mockPlagaList.get(0);

        //INDICO QUE ESPERO A LA SALIDA (EL OBJETO ESPERADO cuidadoOUTDTO)
        PlagaOutDto expectedDto = mockPlagaOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(modelMapper.map(plagaInDto, Plaga.class)).thenReturn(plagaMapped);
        when(plagaRepository.save(plagaMapped)).thenReturn(plagaMapped);


        //EJECUTO EL METODO ADD DEL SERVICIO
        Plaga result = plagaService.add(plagaInDto);

        //COMPROBACION DE QUE LA SALIDA ES LO QUE SE ESPERABA
        assertEquals(1, result.getIdPlaga());
        assertEquals("Cochinilla algodonosa", result.getNombre());
        assertEquals("Presencia de masas blancas y algodonosas con melaza", result.getSintomas());
        assertEquals(6.5f, result.getRiesgo());
        assertTrue(result.isEsLetal());

        //VERIFICACION DE QUE LOS MOCKS FUNCIONARON OK
        verify(modelMapper).map(plagaInDto, Plaga.class);
        verify(plagaRepository).save(plagaMapped);


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
    public void testDeleteOk() throws PlagaNotFoundException, PlagaConflictException {

        long idPlaga = 1;
        Plaga mockPlaga = mock(Plaga.class);

        when(plagaRepository.findById(idPlaga)).thenReturn(Optional.of(mockPlaga)); //simulamos que la plaga existe
        when(plantaRepository.findByPlagas_IdPlaga(idPlaga)).thenReturn(List.of()); //simulacion de que hay plantas associadas a ese cuidado

        plagaService.remove(idPlaga);

        verify(plagaRepository).findById(idPlaga);
        verify(plantaRepository).findByPlagas_IdPlaga(idPlaga);
        verify(plagaRepository).deleteById(idPlaga);

    }

    @Test
    public void testDeletePlaga_NotFound() {
        long idPlaga = 1L;

        when(plagaRepository.findById(idPlaga)).thenReturn(Optional.empty());

        assertThrows(PlagaNotFoundException.class, () -> {
            plagaService.remove(idPlaga);
        });

        verify(plagaRepository).findById(idPlaga);
        verify(plantaRepository, never()).findByPlagas_IdPlaga(anyLong());
        verify(plagaRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeletePlaga_Conflict() {
        long idPlaga = 1L;
        Plaga mockPlaga = mock(Plaga.class);
        Planta planta = new Planta();

        when(plagaRepository.findById(idPlaga)).thenReturn(Optional.of(mockPlaga));
        when(plantaRepository.findByPlagas_IdPlaga(idPlaga)).thenReturn(List.of(planta));

        assertThrows(PlagaConflictException.class, () -> {
            plagaService.remove(idPlaga);
        });

        verify(plagaRepository).findById(idPlaga);
        verify(plantaRepository).findByPlagas_IdPlaga(idPlaga);
        verify(plagaRepository, never()).deleteById(anyLong());
    }









}
