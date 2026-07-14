package com.example.multa.MultaService.config;

import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.model.Multa;
import com.example.multa.MultaService.repository.MultaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarMultasIniciales(MultaRepository multaRepository) {
        return args -> {
            if (multaRepository.count() == 0) {
                multaRepository.save(new Multa(null, 1L, 1L, new BigDecimal("1500"), 3, EstadoMulta.PENDIENTE, LocalDateTime.now().minusDays(1), null));
                multaRepository.save(new Multa(null, 2L, 2L, new BigDecimal("500"), 1, EstadoMulta.PAGADA, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(8)));
            }
        };
    }
}
