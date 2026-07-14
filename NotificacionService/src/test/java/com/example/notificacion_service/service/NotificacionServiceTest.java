package com.example.notificacion_service.service;

import com.example.notificacion_service.dto.NotificacionDTO;
import com.example.notificacion_service.dto.UsuarioResponseDTO;
import com.example.notificacion_service.model.Notificacion;
import com.example.notificacion_service.model.TipoNotificacion;
import com.example.notificacion_service.repository.NotificacionRepository;
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
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificacionService notificacionService;

    @BeforeEach
    void configurarUrl() {
        ReflectionTestUtils.setField(notificacionService, "usuarioServiceUrl", "http://usuario-service:9091");
    }

    @Test
    void guardarNotificacionValidaDebeQuedarNoLeida() {
        NotificacionDTO dto = crearDto();
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setNombre("Ana Biblioteca");
        usuario.setCorreo("ana@biblioteca.cl");
        when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1/existe", Boolean.class)).thenReturn(true);
        when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1", UsuarioResponseDTO.class)).thenReturn(usuario);
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocacion -> {
            Notificacion notificacion = invocacion.getArgument(0);
            notificacion.setId(1L);
            return notificacion;
        });

        NotificacionDTO resultado = notificacionService.guardar(dto);

        assertFalse(resultado.getLeida());
        assertNotNull(resultado.getFechaEnvio());
        assertEquals("Ana Biblioteca", resultado.getNombreUsuario());
    }

    @Test
    void guardarSinTituloDebeLanzarExcepcion() {
        NotificacionDTO dto = crearDto();
        dto.setTitulo("   ");

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> notificacionService.guardar(dto));

        assertEquals("El título es obligatorio", excepcion.getMessage());
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void guardarConUsuarioNoValidableDebeLanzarExcepcion() {
        NotificacionDTO dto = crearDto();
        when(restTemplate.getForObject("http://usuario-service:9091/usuarios/1/existe", Boolean.class)).thenReturn(false);

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> notificacionService.guardar(dto));

        assertEquals("No se pudo validar el usuario en UsuarioService", excepcion.getMessage());
    }

    @Test
    void marcarLeidaDebeActualizarEstado() {
        Notificacion notificacion = crearDto().toModel();
        notificacion.setId(1L);
        notificacion.setLeida(false);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(notificacion)).thenReturn(notificacion);

        NotificacionDTO resultado = notificacionService.marcarLeida(1L);

        assertTrue(resultado.getLeida());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void eliminarInexistenteDebeRetornarFalse() {
        when(notificacionRepository.existsById(99L)).thenReturn(false);

        assertFalse(notificacionService.eliminar(99L));
        verify(notificacionRepository, never()).deleteById(any());
    }

    private NotificacionDTO crearDto() {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setUsuarioId(1L);
        dto.setTitulo("Prestamo por vencer");
        dto.setMensaje("Devuelva el libro antes del viernes");
        dto.setTipoNotificacion(TipoNotificacion.VENCIMIENTO);
        return dto;
    }
}
