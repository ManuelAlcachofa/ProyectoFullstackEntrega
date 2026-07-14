package com.example.notificacion_service.config;

import com.example.notificacion_service.model.Notificacion;
import com.example.notificacion_service.model.TipoNotificacion;
import com.example.notificacion_service.repository.NotificacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarNotificacionesIniciales(NotificacionRepository notificacionRepository) {
        return args -> {
            if (notificacionRepository.count() == 0) {
                notificacionRepository.save(new Notificacion(null, 1L, 1L, null, "Tu préstamo está por vencer", "Hola Pepe, recuerda devolver Cien años de soledad antes de la fecha pactada.", TipoNotificacion.VENCIMIENTO, LocalDateTime.now().minusHours(2), false));
                notificacionRepository.save(new Notificacion(null, 1L, null, 1L, "Libro reservado disponible", "Hola Pepe, el libro que reservaste ya está disponible por 48 horas.", TipoNotificacion.DISPONIBILIDAD_RESERVA, LocalDateTime.now().minusHours(1), false));
                notificacionRepository.save(new Notificacion(null, 2L, null, null, "Bienvenida", "Bienvenida Ana Biblioteca al sistema de gestión de biblioteca.", TipoNotificacion.BIENVENIDA, LocalDateTime.now().minusDays(1), true));
            }
        };
    }
}
