package com.example.resena.ResenaService.service;

import com.example.resena.ResenaService.dto.LibroResponseDTO;
import com.example.resena.ResenaService.dto.ResenaDTO;
import com.example.resena.ResenaService.dto.UsuarioResponseDTO;
import com.example.resena.ResenaService.model.Resena;
import com.example.resena.ResenaService.repository.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ResenaService resenaService;

    @BeforeEach
    void configurarUrls() {
        ReflectionTestUtils.setField(resenaService, "libroServiceUrl", "http://libro-service:9092");
        ReflectionTestUtils.setField(resenaService, "usuarioServiceUrl", "http://usuario-service:9091");
    }

    @Test
    void guardarResenaNuevaDebeAsignarFechaYEnriquecerDatos() {
        ResenaDTO dto = crearDto();
        when(resenaRepository.findByLibroIdAndUsuarioId(2L, 1L)).thenReturn(Optional.empty());
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocacion -> {
            Resena resena = invocacion.getArgument(0);
            resena.setId(5L);
            return resena;
        });
        configurarEnriquecimiento();

        ResenaDTO resultado = resenaService.guardar(dto);

        assertEquals(5L, resultado.getId());
        assertNotNull(resultado.getFechaResena());
        assertEquals("Cien anos de soledad", resultado.getTituloLibro());
        assertEquals("Ana Biblioteca", resultado.getNombreUsuario());
    }

    @Test
    void guardarResenaDuplicadaDebeLanzarExcepcion() {
        when(resenaRepository.findByLibroIdAndUsuarioId(2L, 1L))
                .thenReturn(Optional.of(crearResena(1L, 5)));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> resenaService.guardar(crearDto()));

        assertTrue(excepcion.getMessage().contains("ya dejó una reseña"));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void calcularPromedioDebeRetornarPromedioCorrecto() {
        when(resenaRepository.findByLibroId(2L))
                .thenReturn(List.of(crearResena(1L, 5), crearResena(2L, 3), crearResena(3L, 4)));

        Double promedio = resenaService.calcularPromedioPorLibro(2L);

        assertEquals(4.0, promedio);
    }

    @Test
    void calcularPromedioSinResenasDebeRetornarCero() {
        when(resenaRepository.findByLibroId(2L)).thenReturn(List.of());

        assertEquals(0.0, resenaService.calcularPromedioPorLibro(2L));
    }

    @Test
    void actualizarResenaInexistenteDebeRetornarNull() {
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(resenaService.actualizar(99L, crearDto()));
    }

    private void configurarEnriquecimiento() {
        LibroResponseDTO libro = new LibroResponseDTO();
        libro.setTitulo("Cien anos de soledad");
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setNombre("Ana Biblioteca");
        when(restTemplate.getForObject("http://libro-service:9092/libros/2", LibroResponseDTO.class)).thenReturn(libro);
        when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1", UsuarioResponseDTO.class)).thenReturn(usuario);
    }

    private ResenaDTO crearDto() {
        ResenaDTO dto = new ResenaDTO();
        dto.setLibroId(2L);
        dto.setUsuarioId(1L);
        dto.setCalificacion(5);
        dto.setComentario("Excelente libro");
        return dto;
    }

    private Resena crearResena(Long id, int calificacion) {
        Resena resena = crearDto().toModel();
        resena.setId(id);
        resena.setCalificacion(calificacion);
        resena.setFechaResena(LocalDateTime.now());
        return resena;
    }
}
