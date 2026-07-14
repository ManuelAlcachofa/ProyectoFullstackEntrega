package com.example.resena.ResenaService.config;

import com.example.resena.ResenaService.model.Resena;
import com.example.resena.ResenaService.repository.ResenaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarResenasIniciales(ResenaRepository resenaRepository) {
        return args -> {
            if (resenaRepository.count() == 0) {
                resenaRepository.save(new Resena(null, 1L, 1L, 5, "Muy buen libro, fácil de recomendar en la biblioteca.", LocalDateTime.now().minusDays(2)));
                resenaRepository.save(new Resena(null, 3L, 2L, 4, "Buen libro de fantasía para estudiantes.", LocalDateTime.now().minusDays(1)));
            }
        };
    }
}
