package com.Libro.LibroService.service;

import com.Libro.LibroService.dto.AutorResponseDTO;
import com.Libro.LibroService.dto.CategoriaResponseDTO;
import com.Libro.LibroService.dto.LibroDTO;
import com.Libro.LibroService.model.Libro;
import com.Libro.LibroService.repository.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LibroService libroService;

    @BeforeEach
    void configurarUrls() {
        ReflectionTestUtils.setField(libroService, "autorServiceUrl", "http://autor-service:9095");
        ReflectionTestUtils.setField(libroService, "categoriaServiceUrl", "http://categoria-service:9094");
    }

    @Test
    void obtenerPorIdDebeEnriquecerAutorYCategoria() {
        Libro libro = crearLibro(1L);
        AutorResponseDTO autor = new AutorResponseDTO();
        autor.setNombre("Gabriel");
        autor.setApellido("Garcia Marquez");
        CategoriaResponseDTO categoria = new CategoriaResponseDTO();
        categoria.setNombre("Novela");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(restTemplate.getForObject("http://autor-service:9095/autores/10", AutorResponseDTO.class)).thenReturn(autor);
        when(restTemplate.getForObject("http://categoria-service:9094/categorias/20", CategoriaResponseDTO.class)).thenReturn(categoria);

        LibroDTO resultado = libroService.obtenerPorId(1L);

        assertEquals("Cien anos de soledad", resultado.getTitulo());
        assertEquals("Gabriel", resultado.getNombreAutor());
        assertEquals("Garcia Marquez", resultado.getApellidoAutor());
        assertEquals("Novela", resultado.getNombreCategoria());
    }

    @Test
    void obtenerPorIdInexistenteDebeRetornarNull() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(libroService.obtenerPorId(99L));
    }

    @Test
    void guardarDebePersistirLibro() {
        LibroDTO dto = LibroDTO.fromModel(crearLibro(null));
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocacion -> {
            Libro libro = invocacion.getArgument(0);
            libro.setId(1L);
            return libro;
        });

        LibroDTO resultado = libroService.guardar(dto);

        assertEquals(1L, resultado.getId());
        assertEquals("9780307474728", resultado.getIsbn());
    }

    @Test
    void actualizarLibroExistenteDebeGuardarCambios() {
        Libro existente = crearLibro(1L);
        LibroDTO cambios = LibroDTO.fromModel(crearLibro(null));
        cambios.setTitulo("Titulo actualizado");
        when(libroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(libroRepository.save(existente)).thenReturn(existente);

        LibroDTO resultado = libroService.actualizar(1L, cambios);

        assertEquals("Titulo actualizado", resultado.getTitulo());
        verify(libroRepository).save(existente);
    }

    @Test
    void listarTodosDebeUsarValoresDeRespaldoSiServiciosFallan() {
        Libro libro = crearLibro(1L);
        when(libroRepository.findAll()).thenReturn(List.of(libro));
        when(restTemplate.getForObject("http://autor-service:9095/autores/10", AutorResponseDTO.class))
                .thenThrow(new RuntimeException("Servicio no disponible"));
        when(restTemplate.getForObject("http://categoria-service:9094/categorias/20", CategoriaResponseDTO.class))
                .thenThrow(new RuntimeException("Servicio no disponible"));

        List<LibroDTO> resultado = libroService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Error al conectar con Autores", resultado.get(0).getNombreAutor());
        assertEquals("Error al conectar con Categorías", resultado.get(0).getNombreCategoria());
    }

    private Libro crearLibro(Long id) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setIsbn("9780307474728");
        libro.setTitulo("Cien anos de soledad");
        libro.setFechaPublicacion(LocalDate.of(1967, 6, 5));
        libro.setSinopsis("Historia de la familia Buendia");
        libro.setAutorId(10L);
        libro.setCategoriaId(20L);
        return libro;
    }
}
