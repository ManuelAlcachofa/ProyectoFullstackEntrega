package com.example.prestamo_service.service;

import com.example.prestamo_service.dto.InventarioResponseDTO;
import com.example.prestamo_service.dto.LibroResponseDTO;
import com.example.prestamo_service.dto.PrestamoDTO;
import com.example.prestamo_service.dto.UsuarioResponseDTO;
import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.model.Prestamo;
import com.example.prestamo_service.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PrestamoService prestamoService;

    @BeforeEach
    void configurarUrls() {
        ReflectionTestUtils.setField(prestamoService, "usuarioServiceUrl", "http://usuario-service:9091");
        ReflectionTestUtils.setField(prestamoService, "libroServiceUrl", "http://libro-service:9092");
        ReflectionTestUtils.setField(prestamoService, "inventarioServiceUrl", "http://inventario-service:9093");
    }

    @Test
    void guardarPrestamoValidoDebeCrearPrestamoActivoYDescontarStock() {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setUsuarioId(1L);
        dto.setLibroId(2L);
        configurarDatosRemotosValidos(3);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocacion -> {
            Prestamo prestamo = invocacion.getArgument(0);
            prestamo.setId(10L);
            return prestamo;
        });

        PrestamoDTO resultado = prestamoService.guardar(dto);

        assertEquals(EstadoPrestamo.ACTIVO, resultado.getEstadoPrestamo());
        assertNotNull(resultado.getFechaPrestamo());
        assertNotNull(resultado.getFechaDevolucionPactada());
        assertEquals("Ana Biblioteca", resultado.getNombreUsuario());
        assertEquals("Cien anos de soledad", resultado.getTituloLibro());
        verify(restTemplate).put("http://inventario-service:9093/inventario/prestar/2", null);
    }

    @Test
    void guardarSinStockDebeLanzarExcepcionYNoGuardar() {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setUsuarioId(1L);
        dto.setLibroId(2L);
        configurarDatosRemotosValidos(0);

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> prestamoService.guardar(dto));

        assertTrue(excepcion.getMessage().contains("No hay stock disponible"));
        verify(prestamoRepository, never()).save(any());
        verify(restTemplate, never()).put(anyString(), any());
    }

    @Test
    void guardarSinUsuarioIdDebeLanzarExcepcionAntesDeLlamadasRemotas() {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setLibroId(2L);

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> prestamoService.guardar(dto));

        assertEquals("El ID del usuario es obligatorio", excepcion.getMessage());
        verifyNoInteractions(restTemplate, prestamoRepository);
    }

    @Test
    void devolverPrestamoActivoDebeCambiarEstadoYDevolverStock() {
        Prestamo prestamo = crearPrestamo(EstadoPrestamo.ACTIVO);
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(prestamo)).thenReturn(prestamo);

        PrestamoDTO resultado = prestamoService.devolver(10L);

        assertEquals(EstadoPrestamo.DEVUELTO, resultado.getEstadoPrestamo());
        assertNotNull(resultado.getFechaDevolucionReal());
        verify(restTemplate).put("http://inventario-service:9093/inventario/devolver/2", null);
    }

    @Test
    void devolverPrestamoYaDevueltoDebeLanzarExcepcion() {
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(crearPrestamo(EstadoPrestamo.DEVUELTO)));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> prestamoService.devolver(10L));

        assertEquals("El préstamo ya fue devuelto", excepcion.getMessage());
        verify(restTemplate, never()).put(anyString(), any());
    }

    @Test
    void eliminarPrestamoInexistenteDebeRetornarFalse() {
        when(prestamoRepository.existsById(99L)).thenReturn(false);

        assertFalse(prestamoService.eliminar(99L));
    }

    private void configurarDatosRemotosValidos(int disponibles) {
        LibroResponseDTO libro = new LibroResponseDTO();
        libro.setId(2L);
        libro.setTitulo("Cien anos de soledad");
        InventarioResponseDTO inventario = new InventarioResponseDTO();
        inventario.setLibrosDisponibles(disponibles);
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setNombre("Ana Biblioteca");
        usuario.setCorreo("ana@biblioteca.cl");

        when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1/existe", Boolean.class)).thenReturn(true);
        when(restTemplate.getForObject("http://libro-service:9092/libros/2", LibroResponseDTO.class)).thenReturn(libro);
        when(restTemplate.getForObject("http://inventario-service:9093/inventario/libro/2", InventarioResponseDTO.class)).thenReturn(inventario);
        if (disponibles > 0) {
            when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1", UsuarioResponseDTO.class)).thenReturn(usuario);
        }
    }

    private Prestamo crearPrestamo(EstadoPrestamo estado) {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(10L);
        prestamo.setUsuarioId(1L);
        prestamo.setLibroId(2L);
        prestamo.setFechaPrestamo(LocalDateTime.now().minusDays(2));
        prestamo.setFechaDevolucionPactada(LocalDateTime.now().plusDays(12));
        prestamo.setEstadoPrestamo(estado);
        return prestamo;
    }
}
