package com.example.prestamo_service.config;

import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.model.Prestamo;
import com.example.prestamo_service.repository.PrestamoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarPrestamosIniciales(PrestamoRepository prestamoRepository) {
        return args -> {
            if (prestamoRepository.count() == 0) {
                prestamoRepository.save(new Prestamo(null, 1L, 1L, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(9), null, EstadoPrestamo.ACTIVO));
                prestamoRepository.save(new Prestamo(null, 2L, 2L, LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(6), LocalDateTime.now().minusDays(5), EstadoPrestamo.DEVUELTO));
            }
        };
    }
}
