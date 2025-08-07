package com.svalero.ApiPlant;

import com.svalero.ApiPlant.domain.Consejo;
import com.svalero.ApiPlant.domain.Plaga;
import com.svalero.ApiPlant.domain.Planta;
import com.svalero.ApiPlant.domain.dto.ConsejoInDto;
import com.svalero.ApiPlant.domain.dto.ConsejoOutDto;
import com.svalero.ApiPlant.domain.dto.PlagaInDto;
import com.svalero.ApiPlant.domain.dto.PlagaOutDto;
import com.svalero.ApiPlant.exception.ConsejoConflictException;
import com.svalero.ApiPlant.exception.ConsejoNotFoundException;
import com.svalero.ApiPlant.exception.PlagaConflictException;
import com.svalero.ApiPlant.exception.PlagaNotFoundException;
import com.svalero.ApiPlant.repository.ConsejoRepository;
import com.svalero.ApiPlant.repository.PlantaRepository;
import com.svalero.ApiPlant.service.ConsejoService;
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
public class ConsejoServiceTests {

    @InjectMocks
    ConsejoService consejoService;
    @Mock
    ConsejoRepository consejoRepository;
    @Mock
    PlantaRepository plantaRepository;
    @Mock
    private ModelMapper modelMapper;

    private final List<Consejo> mockConsejoList = List.of(
            new Consejo(1, "Riego", "Riega por inmersión durante varias horas", true,6.5f,  null, List.of(new Planta(50L))),
            new Consejo(2, "Iluminación", "indirecta y durante más de 6 horas diarias", false,3.5f,  null, List.of(new Planta(30L)))
    );

    private final List<ConsejoOutDto> mockConsejoOutDtoList = List.of(
            new ConsejoOutDto(1, "Riego", "Riega por inmersión durante varias horas", true, 6.5f, List.of(50L)),
            new ConsejoOutDto(2, "Iluminación", "indirecta y durante más de 6 horas diarias", false, 3.5f, List.of(30L))
    );


    @Test
    public void testGetAll() throws ConsejoNotFoundException {


        when(consejoRepository.findAll()).thenReturn(mockConsejoList);

        List<Consejo> resultado = consejoService.getAll(null, null, null);

        assertEquals(2, resultado.size());  //cuantos registros va a encontrar
        assertEquals("Riego", resultado.get(0).getTitulo());
        assertEquals(6.5f, resultado.get(0).getImportancia());
        assertEquals("Iluminación", resultado.get(1).getTitulo());

        verify(consejoRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_ByTitulo() throws ConsejoNotFoundException {

        String titulo = "Riego";

        List<Consejo> filtroTitulo = mockConsejoList.stream()
                .filter(consejo -> consejo.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .toList();

        when(consejoRepository.findByTituloContainingIgnoreCase(titulo)).thenReturn(filtroTitulo);

        List<Consejo> resultado = consejoService.getAll(titulo, null, null);

        assertEquals(filtroTitulo.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getTitulo().toLowerCase().contains(titulo.toLowerCase())));

        verify(consejoRepository, times(1)).findByTituloContainingIgnoreCase(titulo);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByVerificadoTrue() throws ConsejoNotFoundException {

        boolean verificado = true;

        List<Consejo> filtroVerdad = mockConsejoList.stream()
                .filter(Consejo::isVerificado)
                .toList();

        when(consejoRepository.findByVerificado(verificado)).thenReturn(filtroVerdad);

        for (int i = 0; i < filtroVerdad.size(); i++) {
            when(modelMapper.map(filtroVerdad.get(i), Consejo.class)).thenReturn(mockConsejoList.get(i));
        }

        when(modelMapper.map(filtroVerdad, new TypeToken<List<Consejo>>() {
        }.getType()))
                .thenReturn(mockConsejoList.stream()
                        .filter(Consejo::isVerificado)
                        .toList());

        List<Consejo> resultado = consejoService.getAll(null,true, null );

        assertEquals(filtroVerdad.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(Consejo::isVerificado));

        verify(consejoRepository, times(1)).findByVerificado(verificado);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testGetAll_ByVerificadoFalse() throws ConsejoNotFoundException {

        boolean verificado = false;

        List<Consejo> filtroVerdad = mockConsejoList.stream()
                .filter(Consejo::isVerificado)
                .toList();

        when(consejoRepository.findByVerificado(verificado)).thenReturn(filtroVerdad);

        for (int i = 0; i < filtroVerdad.size(); i++) {
            when(modelMapper.map(filtroVerdad.get(i), Consejo.class)).thenReturn(mockConsejoList.get(i));
        }

        when(modelMapper.map(filtroVerdad, new TypeToken<List<Consejo>>() {
        }.getType()))
                .thenReturn(mockConsejoList.stream()
                        .filter(dto -> !dto.isVerificado())
                        .toList());

        List<Consejo> resultado = consejoService.getAll(null, false, null);

        assertEquals(filtroVerdad.size(), resultado.size());
        assertFalse(resultado.stream().noneMatch(Consejo::isVerificado));



        verify(consejoRepository, times(1)).findByVerificado(verificado);
    }

    @Test
    void testGetAll_ByImportancia() throws ConsejoNotFoundException {
        Float importancia = 6.5f;

        List<Consejo> filtroImportancia = mockConsejoList.stream()
                .filter(consejo -> importancia.equals(consejo.getImportancia()))
                .toList();

        when(consejoRepository.findByImportancia(importancia)).thenReturn(filtroImportancia);


        List<Consejo> resultado = consejoService.getAll(null, null, importancia);

        assertEquals(filtroImportancia.size(), resultado.size());
        assertTrue(resultado.stream().allMatch(p -> importancia.equals(p.getImportancia())));

        verify(consejoRepository, times(1)).findByImportancia(importancia);
    }

    @Test
    public void testGetById() throws ConsejoNotFoundException {

        long id = 1;
        // COnsejo de prueba con plantas asociadas
        List<Planta> plantas = List.of(new Planta(50L));
        Consejo mockConsejo= new Consejo(id,"Riego", "Riega por inmersión durante varias horas", true,6.5f,
                null, plantas);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(consejoRepository.findById(id)).thenReturn(Optional.of(mockConsejo));

        //EJECUCION DEL METODO DEL SERVICIO
        ConsejoOutDto result = consejoService.get(id);

        //VERIFICACION DE LOS CAMPOS DEVUELTOS
        assertEquals(1, result.getIdConsejo());
        assertEquals("Riego", result.getTitulo());
        assertEquals("Riega por inmersión durante varias horas", result.getExplicacion());
        assertTrue(result.isVerificado());
        assertEquals(6.5f, result.getImportancia());
        assertEquals(List.of(50L), result.getPlantaIds());

        //VERIFICACION DE QUE LOS METODOS MOCKEADOS SE HAYAN LLAMADO UNA VEZ
        verify(consejoRepository, times(1)).findById(id);

    }

    @Test
    public void testAdd()  {

        //DEFINO EL OBJETO DE ENTRADA (consejoINDTO)
        ConsejoInDto consejoInDto = new ConsejoInDto("Riego", "Riega por inmersión durante varias horas", true,6.5f,  null);

        //CREO MOCK mapeado (FICTICIOS) QUE ACTUARAN COMO "BD"
        Consejo consejoMapped = new Consejo(1, "Riego", "Riega por inmersión durante varias horas", true,6.5f,  null, null);

        //CONFIGURO LOS MAPEOS
        Consejo mockConsejo = mockConsejoList.get(0);

        //INDICO QUE ESPERO A LA SALIDA (EL OBJETO ESPERADO consejoOUTDTO)
        ConsejoOutDto expectedDto = mockConsejoOutDtoList.get(0);

        //CONFIGURACION DE LAS RESPUESTAS DEL REPOSITORIO Y DEL MODELMAPPER
        when(modelMapper.map(consejoInDto, Consejo.class)).thenReturn(consejoMapped);
        when(consejoRepository.save(consejoMapped)).thenReturn(consejoMapped);


        //EJECUTO EL METODO ADD DEL SERVICIO
        Consejo result = consejoService.add(consejoInDto);

        //COMPROBACION DE QUE LA SALIDA ES LO QUE SE ESPERABA
        assertEquals(1, result.getIdConsejo());
        assertEquals("Riego", result.getTitulo());
        assertEquals("Riega por inmersión durante varias horas", result.getExplicacion());
        assertTrue(result.isVerificado());
        assertEquals(6.5f, result.getImportancia());

        //VERIFICACION DE QUE LOS MOCKS FUNCIONARON OK
        verify(modelMapper).map(consejoInDto, Consejo.class);
        verify(consejoRepository).save(consejoMapped);


    }

    @Test
    public void testDeleteOk() throws ConsejoNotFoundException, ConsejoConflictException {

        long idConsejo = 1;
        Consejo mockConsejo = mock(Consejo.class);

        when(consejoRepository.findById(idConsejo)).thenReturn(Optional.of(mockConsejo)); //simulamos que el consejo existe
        when(plantaRepository.findByConsejos_IdConsejo(idConsejo)).thenReturn(List.of()); //simulacion de que hay plantas associadas a ese consejo

        consejoService.remove(idConsejo);

        verify(consejoRepository).findById(idConsejo);
        verify(plantaRepository).findByConsejos_IdConsejo(idConsejo);
        verify(consejoRepository).deleteById(idConsejo);

    }

    @Test
    public void testDeleteConsejo_NotFound() {
        long idConsejo = 1L;

        when(consejoRepository.findById(idConsejo)).thenReturn(Optional.empty());

        assertThrows(ConsejoNotFoundException.class, () -> {
            consejoService.remove(idConsejo);
        });

        verify(consejoRepository).findById(idConsejo);
        verify(plantaRepository, never()).findByPlagas_IdPlaga(anyLong());
        verify(consejoRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteConsejo_Conflict() {
        long idConsejo = 1L;
        Consejo mockConsejo= mock(Consejo.class);
        Planta planta = new Planta();

        when(consejoRepository.findById(idConsejo)).thenReturn(Optional.of(mockConsejo));
        when(plantaRepository.findByConsejos_IdConsejo(idConsejo)).thenReturn(List.of(planta));

        assertThrows(ConsejoConflictException.class, () -> {
            consejoService.remove(idConsejo);
        });

        verify(consejoRepository).findById(idConsejo);
        verify(plantaRepository).findByConsejos_IdConsejo(idConsejo);
        verify(consejoRepository, never()).deleteById(anyLong());
    }




}
