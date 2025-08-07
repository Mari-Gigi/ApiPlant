package com.svalero.ApiPlant;

import com.svalero.ApiPlant.domain.*;
import com.svalero.ApiPlant.domain.dto.PlantaInDto;
import com.svalero.ApiPlant.domain.dto.PlantaOutDto;
import com.svalero.ApiPlant.exception.*;
import com.svalero.ApiPlant.repository.*;
import com.svalero.ApiPlant.service.PlantaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlantaServiceTests {


    @InjectMocks
    PlantaService plantaService;
    @Mock
    PlantaRepository plantaRepository;
    @Mock
    CuidadoRepository cuidadoRepository;
    @Mock
    CategoriaRepository categoriaRepository;
    @Mock
    PlagaRepository plagaRepository;
    @Mock
    ConsejoRepository consejoRepository;
    @Mock
    private ModelMapper modelMapper;


    private final  List<Planta> mockPlantaList = List.of(
            new Planta(1, "Rosa", "Indica", 1.5f, "Herbácea", null, true, new Cuidado(1), new Categoria(1), List.of(new Plaga(1L)), List.of(new Consejo(1L))),
            new Planta(2, "Tulipa", "Gesneriana", 0.8f, "Bulbosa", null, false, new Cuidado(1), new Categoria(2), null, null),
            new Planta(3, "Lirio", "Niger", 1.0f, "Perenne", null, false, new Cuidado(2), new Categoria(3), null, null)
    );

    private final    List<PlantaOutDto> mockPlantaOutDtoList = List.of(
            new PlantaOutDto(1, "Rosa", "Indica", true, 1.5f, "Herbácea", 1, 1, List.of(1L), List.of(1L)),
            new PlantaOutDto(2, "Tulipa", "Gesneriana", false, 0.8f, "Bulbosa", 1, 2, List.of(), List.of()),
            new PlantaOutDto(3, "Lirio", "Niger", false, 1.0f, "Perenne", 2, 3, List.of(), List.of())
    );


    @Test
    public void testGetAll() throws PlantaNotFoundException {

        when(plantaRepository.findAll()).thenReturn(mockPlantaList);

       for (int i = 0; i < mockPlantaList.size(); i++) {
            when(modelMapper.map(mockPlantaList.get(i), PlantaOutDto.class)).thenReturn(mockPlantaOutDtoList.get(i));
        }

        List<PlantaOutDto> plantaList = plantaService.getAll(null, null, null);

        assertEquals(3, plantaList.size());
        assertEquals("Rosa", plantaList.get(0).getGenero());
        assertEquals("Lirio", plantaList.get(2).getGenero());

        verify(plantaRepository, times(1)).findAll();

    }

    @Test
    void testGetAll_ByGenero() throws PlantaNotFoundException {

        String genero = "Rosa";
        // Filtramos la lista mock para que solo devuelva las plantas cuyo género contiene "Rosa"
        List<Planta> filtroGenero = mockPlantaList.stream()
                .filter(planta -> planta.getGenero().toLowerCase().contains(genero.toLowerCase()))
                .toList();

        when(plantaRepository.findByGeneroContainingIgnoreCase(genero)).thenReturn(filtroGenero);

        for (int i = 0; i < filtroGenero.size(); i++) {
            when(modelMapper.map(filtroGenero.get(i), PlantaOutDto.class)).thenReturn(mockPlantaOutDtoList.get(i));
        }

        List<PlantaOutDto> resultado = plantaService.getAll(genero, null, null);

        assertEquals(filtroGenero.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p->p.getGenero().toLowerCase().contains("rosa")));

        verify(plantaRepository, times(1)).findByGeneroContainingIgnoreCase(genero);
    }

    @Test
    void testGetAll_ByEspecie() throws PlantaNotFoundException {

        String especie = "Indica";

        List<Planta> filtroEspecie = mockPlantaList.stream()
                .filter(planta -> planta.getEspecie().toLowerCase().contains(especie.toLowerCase()))
                .toList();

        when(plantaRepository.findByEspecieContainingIgnoreCase(especie)).thenReturn(filtroEspecie);

       for (int i = 0; i < filtroEspecie.size(); i++) {
            when(modelMapper.map(filtroEspecie.get(i), PlantaOutDto.class)).thenReturn(mockPlantaOutDtoList.get(i));
        }

        List<PlantaOutDto> resultado = plantaService.getAll(null, especie, null);

        assertEquals(filtroEspecie.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p->p.getEspecie().toLowerCase().contains("indica")));

        verify(plantaRepository, times(1)).findByEspecieContainingIgnoreCase(especie);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsToxicaTrue() throws PlantaNotFoundException {

        boolean esToxica = true;

        List<Planta> filtroToxico = mockPlantaList.stream()
                .filter(Planta :: getEsToxica)
                .toList();

        when(plantaRepository.findByEsToxicaTrue()).thenReturn(filtroToxico);

        for (int i = 0; i < filtroToxico.size(); i++) {
            when(modelMapper.map(filtroToxico.get(i), PlantaOutDto.class)).thenReturn(mockPlantaOutDtoList.get(i));
        }

        when(modelMapper.map(filtroToxico, new TypeToken<List<PlantaOutDto>>() {}.getType()))
                .thenReturn(mockPlantaOutDtoList.stream()
                        .filter(PlantaOutDto::getEsToxica)
                        .toList());

        List<PlantaOutDto> resultado = plantaService.getAll(null, null, true);

        assertEquals(filtroToxico.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(PlantaOutDto::getEsToxica));

        verify(plantaRepository, times(1)).findByEsToxicaTrue();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByEsToxicaFalse() throws PlantaNotFoundException {

        boolean esToxica = false;

        List<Planta> filtroToxico = mockPlantaList.stream()
                .filter(Planta :: getEsToxica)
                .toList();

        when(plantaRepository.findByEsToxicaFalse()).thenReturn(filtroToxico);

        for (int i = 0; i < filtroToxico.size(); i++) {
            when(modelMapper.map(filtroToxico.get(i), PlantaOutDto.class)).thenReturn(mockPlantaOutDtoList.get(i));
        }

        when(modelMapper.map(filtroToxico, new TypeToken<List<PlantaOutDto>>() {}.getType()))
                .thenReturn(mockPlantaOutDtoList.stream()
                        .filter(PlantaOutDto::getEsToxica)
                        .toList());

        List<PlantaOutDto> resultado = plantaService.getAll(null, null, false);

        assertEquals(filtroToxico.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(PlantaOutDto::getEsToxica));

        verify(plantaRepository, times(1)).findByEsToxicaFalse();
    }

    @Test
    public void testGetById() throws PlantaNotFoundException {

        long id = 0;
        Planta mockPlanta = mockPlantaList.get(0);
        PlantaOutDto expectedDto = mockPlantaOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(plantaRepository.findById(id)).thenReturn(Optional.of(mockPlantaList.get(0)));
        when(modelMapper.map(mockPlanta, PlantaOutDto.class)).thenReturn(expectedDto);

        //EJECUCION DEL METODO DEL SERVICIO
        PlantaOutDto result = plantaService.get(id);

        //VERIFICACION DE LOS CAMPOS DEVUELTOS
        assertEquals(1, result.getId_planta());
        assertEquals("Rosa", result.getGenero());
        assertEquals("Indica", result.getEspecie());
        assertEquals(true, result.getEsToxica());
        assertEquals(1.5f, result.getAlturaMaxima());
        assertEquals("Herbácea", result.getTipoCrecimiento());
        assertEquals(1, result.getCuidadoId());
        assertEquals(1, result.getCategoriaId());
        assertEquals(List.of(1L), result.getPlagaIds());
        assertEquals(List.of(1L), result.getConsejoIds());

        //VERIFICACION DE QUE LOS METODOS MOCKEADOS SE HAYAN LLAMADO UNA VEZ
        verify(plantaRepository, times(1)).findById(id);

    }

    @Test
    public void testAdd() throws CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {

        //DEFINO EL OBJETO DE ENTRADA (PLANTAINDTO)
         PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f, "Herbácea",
                1, 1, List.of(1L), List.of(1L));

       //CREO OBJETOS MOCK (FICTICIOS) QUE ACTUARAN COMO "BD"
        Cuidado mockCuidado = new Cuidado(1L);
        Categoria mockCategoria = new Categoria(1L);
        Plaga plaga = new Plaga(1L, "Pulgón", "sintomas", 1.0f, false, "ninguno", LocalDate.now(), new ArrayList<>());
        Consejo consejo = new Consejo(1L, "Riego frecuente", "Regar cada 2 días", true, 9f, LocalDate.now(), new ArrayList<>()
        );

        //CONFIGURO LOS MAPEOS
       Planta mockPlanta = mockPlantaList.get(0);

       //INDICO QUE ESPERO A LA SALIDA (EL OBJETO ESPERADO PLANTAOUTDTO)
        PlantaOutDto expectedDto = mockPlantaOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(cuidadoRepository.findById(1L)).thenReturn(Optional.of(mockCuidado));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(mockCategoria));
        when(plagaRepository.findAllById(List.of(1L))).thenReturn(List.of(plaga));
        when(consejoRepository.findAllById(List.of(1L))).thenReturn(List.of(consejo));

        when(modelMapper.map(plantaInDto, Planta.class)).thenReturn(mockPlanta);
        when(plantaRepository.save(any(Planta.class))).thenReturn(mockPlanta);
        when(modelMapper.map(mockPlanta, PlantaOutDto.class)).thenReturn(expectedDto);

        //EJECUTO EL METODO ADD DEL SERVICIO
        PlantaOutDto result = plantaService.add(plantaInDto);

        //COMPROBACION DE QUE LA SALIDA ES LO QUE SE ESPERABA
        assertEquals(1, result.getId_planta());
        assertEquals("Rosa", result.getGenero());
        assertEquals("Indica", result.getEspecie());
        assertEquals(true, result.getEsToxica());
        assertEquals(1.5f, result.getAlturaMaxima());
        assertEquals("Herbácea", result.getTipoCrecimiento());
        assertEquals(1, result.getCuidadoId());
        assertEquals(1, result.getCategoriaId());
        assertEquals(List.of(1L), result.getPlagaIds());
        assertEquals(List.of(1L), result.getConsejoIds());

        //VERIFICACION DE QUE LOS MOCKS FUNCIONARON OK
        verify(cuidadoRepository).findById(1L);
        verify(categoriaRepository).findById(1L);
        verify(plagaRepository).findAllById(List.of(1L));
        verify(consejoRepository).findAllById(List.of(1L));
        verify(modelMapper).map(plantaInDto, Planta.class);
        verify(plantaRepository).save(mockPlanta);
        verify(modelMapper).map(mockPlanta, PlantaOutDto.class);
    }

    @Test
    public void testAdd_cuidadoNoExiste() {

        PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f, "Herbácea",
                99L, 1L, List.of(1L), List.of(10L)
        );

        // Simular que el Cuidado no se encuentra en el repositorio
        when(cuidadoRepository.findById(99L)).thenReturn(Optional.empty());

        //Verificamos que se lanza la excepción esperada
        assertThrows(CuidadoNotFoundException.class, () -> {
            plantaService.add(plantaInDto);
        });

        verify(cuidadoRepository, times(1)).findById(99L);
        verifyNoInteractions(categoriaRepository, plagaRepository, consejoRepository, plantaRepository);
    }

    @Test
    public void testAdd_categoriaNoExiste() {

        PlantaInDto plantaInDto = new PlantaInDto("Rosa", "Indica", true, 1.5f, "Herbácea",
                99L, 1L, List.of(1L), List.of(10L)
        );

        when(cuidadoRepository.findById(99L)).thenReturn(Optional.of(mock(Cuidado.class))); // Simular que el Cuidado existe para no lanzar CuidadoNFE
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        //Verificamos que se lanza la excepción esperada
        assertThrows(CategoriaNotFoundException.class, () -> {
            plantaService.add(plantaInDto);
        });

        verify(categoriaRepository, times(1)).findById(1L);
        verify(cuidadoRepository, times(1)).findById(99L);
        verifyNoInteractions(plagaRepository, consejoRepository, plantaRepository);
    }

  /*  @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testModify() throws PlantaNotFoundException, CuidadoNotFoundException, CategoriaNotFoundException, PlagaNotFoundException, ConsejoNotFoundException {

        List<Planta> mockPlantaList = List.of(
                new Planta(1, "Rosa", "Indica", 1.5f, "Herbácea", null, true, new Cuidado(1), new Categoria(1), List.of(new Plaga(1L)), List.of(new Consejo(1L))),
                new Planta(2, "Tulipa", "Gesneriana", 0.8f, "Bulbosa", null, false, new Cuidado(1), new Categoria(2), null, null),
                new Planta(3, "Lirio", "Niger", 1.0f, "Perenne", null, false, new Cuidado(2), new Categoria(3), null, null)
        );

        Plaga plagaMock = new Plaga(1L);
        Consejo consejoMock = new Consejo(1L);
        long id = 1L;

        // Nuevos datos
        PlantaInDto plantaInDto = new PlantaInDto("Margarita", "Indica", true, 1.5f, "Herbácea",
                1L, 1L, List.of(1L), List.of(1L));

        // Mocks de repositorios
        when(plantaRepository.findById(id)).thenReturn(Optional.of(mockPlantaList.get(0)));
        when(cuidadoRepository.findById(1L)).thenReturn(Optional.of(new Cuidado(1L)));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria(1L)));
        when(plagaRepository.findAllById(List.of(1L))).thenReturn(List.of(plagaMock));
        when(consejoRepository.findAllById(List.of(1L))).thenReturn(List.of(consejoMock));

        // Map de Planta a PlantaOutDto dinámico
        when(modelMapper.map(any(Planta.class), eq(PlantaOutDto.class)))
                .thenAnswer(invocation -> {
                    Planta planta = invocation.getArgument(0);
                    return new PlantaOutDto(
                            planta.getId_planta(),
                            planta.getGenero(),
                            planta.getEspecie(),
                            planta.getEsToxica(),
                            planta.getAlturaMaxima(),
                            planta.getTipoCrecimiento(),
                            planta.getCuidado().getIdCuidado(),
                            planta.getCategoria().getIdCategoria(),
                            planta.getPlagas().stream().map(Plaga::getIdPlaga).toList(),
                            planta.getConsejos().stream().map(Consejo::getIdConsejo).toList()
                    );
                });

        // Ejecuta el método
        PlantaOutDto result = plantaService.modify(id, plantaInDto);

        // Assertions
        assertEquals(1, result.getId_planta());
        assertEquals("Margarita", result.getGenero());
        assertEquals("Indica", result.getEspecie());
        assertEquals(true, result.getEsToxica());
        assertEquals(1.5f, result.getAlturaMaxima());
        assertEquals("Herbácea", result.getTipoCrecimiento());
        assertEquals(1L, result.getCuidadoId());
        assertEquals(1L, result.getCategoriaId());
        assertEquals(List.of(1L), result.getPlagaIds());
        assertEquals(List.of(1L), result.getConsejoIds());

        // Verificaciones
        verify(plantaRepository).findById(id);
        verify(cuidadoRepository).findById(1L);
        verify(categoriaRepository).findById(1L);
        verify(plagaRepository).findAllById(List.of(1L));
        verify(consejoRepository).findAllById(List.of(1L));
        verify(plantaRepository).save(mockPlantaList.get(0));
        verify(modelMapper).map(mockPlantaList.get(0), PlantaOutDto.class);

        // 👇 Elimina esta línea si no tienes un map explícito de InDto a entidad existente
        // verify(modelMapper).map(plantaInDto, mockPlantaList.get(0)); // ⚠️ probablemente innecesaria
    }

*/
    @Test
    public void testdelete() throws PlantaNotFoundException {

        long id = 1;

        when(plantaRepository.findById(id)).thenReturn(Optional.of(new Planta()));

        plantaService.remove(id);

        verify(plantaRepository).findById(id);
        verify(plantaRepository).deleteById(id);
}




}
