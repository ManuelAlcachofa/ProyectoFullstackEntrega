package com.example.multa.MultaService.service;

import com.example.multa.MultaService.dto.MultaDTO;
import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.model.Multa;
import com.example.multa.MultaService.repository.MultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultaServiceTest {

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MultaService multaService;

    @BeforeEach
    void configurarUrl() {
        ReflectionTestUtils.setField(multaService, "usuarioServiceUrl", "http://usuario-service:9091");
    }

    @Test
    void emitirDebeCrearMultaPendienteConFecha() {
        MultaDTO dto = crearDto();
        when(multaRepository.save(any(Multa.class))).thenAnswer(invocacion -> {
            Multa multa = invocacion.getArgument(0);
            multa.setId(1L);
            return multa;
        });

        MultaDTO resultado = multaService.emitir(dto);

        assertEquals(EstadoMulta.PENDIENTE, resultado.getEstadoMulta());
        assertNotNull(resultado.getFechaEmision());
        assertNull(resultado.getFechaPago());
    }

    @Test
    void pagarMultaPendienteDebeCambiarAPagada() {
        Multa multa = crearMulta(EstadoMulta.PENDIENTE);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.save(multa)).thenReturn(multa);

        MultaDTO resultado = multaService.pagar(1L);

        assertEquals(EstadoMulta.PAGADA, resultado.getEstadoMulta());
        assertNotNull(resultado.getFechaPago());
    }

    @Test
    void pagarMultaYaPagadaDebeLanzarExcepcion() {
        when(multaRepository.findById(1L)).thenReturn(Optional.of(crearMulta(EstadoMulta.PAGADA)));

        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> multaService.pagar(1L));

        assertEquals("La multa ya se encuentra pagada", excepcion.getMessage());
        verify(multaRepository, never()).save(any());
    }

    @Test
    void pagarMultaAnuladaDebeLanzarExcepcion() {
        when(multaRepository.findById(1L)).thenReturn(Optional.of(crearMulta(EstadoMulta.ANULADA)));

        assertThrows(RuntimeException.class, () -> multaService.pagar(1L));
    }

    @Test
    void anularMultaPagadaDebeLanzarExcepcion() {
        when(multaRepository.findById(1L)).thenReturn(Optional.of(crearMulta(EstadoMulta.PAGADA)));

        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> multaService.anular(1L));

        assertTrue(excepcion.getMessage().contains("ya fue pagada"));
    }

    @Test
    void pagarMultaInexistenteDebeLanzarExcepcion() {
        when(multaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> multaService.pagar(99L));
    }

    private MultaDTO crearDto() {
        MultaDTO dto = new MultaDTO();
        dto.setPrestamoId(10L);
        dto.setUsuarioId(20L);
        dto.setMonto(new BigDecimal("3500"));
        dto.setDiasRetraso(3);
        return dto;
    }

    private Multa crearMulta(EstadoMulta estado) {
        Multa multa = crearDto().toModel();
        multa.setId(1L);
        multa.setEstadoMulta(estado);
        return multa;
    }
}
