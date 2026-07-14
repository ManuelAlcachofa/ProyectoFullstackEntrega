package com.example.reserva_service.service;

import com.example.reserva_service.dto.InventarioResponseDTO;
import com.example.reserva_service.dto.LibroResponseDTO;
import com.example.reserva_service.dto.ReservaDTO;
import com.example.reserva_service.dto.UsuarioResponseDTO;
import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.model.Reserva;
import com.example.reserva_service.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReservaService reservaService;

    @BeforeEach
    void configurarUrls() {
        ReflectionTestUtils.setField(reservaService, "usuarioServiceUrl", "http://usuario-service:9091");
        ReflectionTestUtils.setField(reservaService, "libroServiceUrl", "http://libro-service:9092");
        ReflectionTestUtils.setField(reservaService, "inventarioServiceUrl", "http://inventario-service:9093");
    }

    @Test
    void guardarReservaSinStockDebeCrearReservaActiva() {
        ReservaDTO dto = new ReservaDTO();
        dto.setUsuarioId(1L);
        dto.setLibroId(2L);
        configurarDatosRemotos(0);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocacion -> {
            Reserva reserva = invocacion.getArgument(0);
            reserva.setId(10L);
            return reserva;
        });

        ReservaDTO resultado = reservaService.guardar(dto);

        assertEquals(EstadoReserva.ACTIVA, resultado.getEstadoReserva());
        assertNotNull(resultado.getFechaReserva());
        assertEquals("Ana Biblioteca", resultado.getNombreUsuario());
        assertEquals("Cien anos de soledad", resultado.getTituloLibro());
    }

    @Test
    void guardarReservaCuandoHayStockDebeLanzarExcepcion() {
        ReservaDTO dto = new ReservaDTO();
        dto.setUsuarioId(1L);
        dto.setLibroId(2L);
        configurarDatosRemotos(2);

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> reservaService.guardar(dto));

        assertTrue(excepcion.getMessage().contains("aún tiene stock disponible"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void guardarSinLibroIdDebeLanzarExcepcion() {
        ReservaDTO dto = new ReservaDTO();
        dto.setUsuarioId(1L);

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> reservaService.guardar(dto));

        assertEquals("El ID del libro es obligatorio", excepcion.getMessage());
        verifyNoInteractions(restTemplate, reservaRepository);
    }

    @Test
    void marcarDisponibleDebeDefinirFechasYEstado() {
        Reserva reserva = crearReserva(EstadoReserva.ACTIVA);
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        ReservaDTO resultado = reservaService.marcarDisponible(10L);

        assertEquals(EstadoReserva.DISPONIBLE, resultado.getEstadoReserva());
        assertNotNull(resultado.getFechaDisponibilidad());
        assertNotNull(resultado.getFechaVencimiento());
    }

    @Test
    void cancelarReservaInexistenteDebeLanzarExcepcion() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservaService.cancelar(99L));
    }

    @Test
    void eliminarReservaExistenteDebeRetornarTrue() {
        when(reservaRepository.existsById(10L)).thenReturn(true);

        assertTrue(reservaService.eliminar(10L));
        verify(reservaRepository).deleteById(10L);
    }

    private void configurarDatosRemotos(int disponibles) {
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
        if (disponibles == 0) {
            when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1", UsuarioResponseDTO.class)).thenReturn(usuario);
        }
    }

    private Reserva crearReserva(EstadoReserva estado) {
        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setUsuarioId(1L);
        reserva.setLibroId(2L);
        reserva.setEstadoReserva(estado);
        return reserva;
    }
}
