package com.example.inventario.InventarioService.service;

import com.example.inventario.InventarioService.dto.InventarioDTO;
import com.example.inventario.InventarioService.model.Inventario;
import com.example.inventario.InventarioService.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    @BeforeEach
    void configurarUrls() {
        ReflectionTestUtils.setField(inventarioService, "libroServiceUrl", "http://libro-service:9092");
    }

    @Test
    void prestarLibroConStockDebeAumentarPrestados() {
        Inventario inventario = crearInventario(1L, 2L, 5, 2);
        when(inventarioRepository.findByLibroId(2L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(inventario)).thenReturn(inventario);
        when(restTemplate.getForObject("http://libro-service:9092/libros/2", Map.class))
                .thenReturn(Map.of("titulo", "Cien anos de soledad"));

        InventarioDTO resultado = inventarioService.prestarLibro(2L);

        assertEquals(3, resultado.getLibrosPrestados());
        assertEquals(2, resultado.getLibrosDisponibles());
        assertEquals("Cien anos de soledad", resultado.getTituloLibro());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void prestarLibroSinStockDebeLanzarExcepcion() {
        Inventario inventario = crearInventario(1L, 2L, 5, 5);
        when(inventarioRepository.findByLibroId(2L)).thenReturn(Optional.of(inventario));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> inventarioService.prestarLibro(2L));

        assertTrue(excepcion.getMessage().contains("Stock agotado"));
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void devolverLibroPrestadoDebeDisminuirPrestados() {
        Inventario inventario = crearInventario(1L, 2L, 5, 3);
        when(inventarioRepository.findByLibroId(2L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        InventarioDTO resultado = inventarioService.devolverLibro(2L);

        assertEquals(2, resultado.getLibrosPrestados());
        assertEquals(3, resultado.getLibrosDisponibles());
    }

    @Test
    void devolverLibroSinPrestamosDebeLanzarExcepcion() {
        Inventario inventario = crearInventario(1L, 2L, 5, 0);
        when(inventarioRepository.findByLibroId(2L)).thenReturn(Optional.of(inventario));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> inventarioService.devolverLibro(2L));

        assertTrue(excepcion.getMessage().contains("No hay registros"));
    }

    @Test
    void guardarInventarioDuplicadoDebeLanzarExcepcion() {
        InventarioDTO dto = new InventarioDTO(null, 2L, null, 5, 0, 5);
        when(inventarioRepository.findByLibroId(2L))
                .thenReturn(Optional.of(crearInventario(1L, 2L, 5, 0)));

        assertThrows(RuntimeException.class, () -> inventarioService.guardar(dto));
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void obtenerLibroInexistenteDebeLanzarExcepcion() {
        when(inventarioRepository.findByLibroId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventarioService.obtenerPorLibroId(99L));
    }

    private Inventario crearInventario(Long id, Long libroId, int stock, int prestados) {
        Inventario inventario = new Inventario();
        inventario.setId(id);
        inventario.setLibroId(libroId);
        inventario.setStockTotal(stock);
        inventario.setLibrosPrestados(prestados);
        return inventario;
    }
}
