package com.example.reserva_service.config;

import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.model.Reserva;
import com.example.reserva_service.repository.ReservaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarReservasIniciales(ReservaRepository reservaRepository) {
        return args -> {
            if (reservaRepository.count() == 0) {
                reservaRepository.save(new Reserva(null, 3L, 1L, LocalDateTime.now().minusHours(6), null, null, EstadoReserva.ACTIVA));
                reservaRepository.save(new Reserva(null, 1L, 2L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusHours(12), LocalDateTime.now().plusHours(36), EstadoReserva.DISPONIBLE));
            }
        };
    }
}
