package com.Libro.LibroService.config;

import com.Libro.LibroService.model.Libro;
import com.Libro.LibroService.repository.LibroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarLibrosIniciales(LibroRepository libroRepository) {
        return args -> {
            if (libroRepository.count() == 0) {
                libroRepository.save(new Libro(null, "9780307474728", "Cien años de soledad", LocalDate.of(1967, 5, 30), "Historia de la familia Buendía en el pueblo ficticio de Macondo.", 1L, 1L));
                libroRepository.save(new Libro(null, "9780061120084", "La casa de los espíritus", LocalDate.of(1982, 1, 1), "Novela familiar con elementos históricos y políticos.", 2L, 1L));
                libroRepository.save(new Libro(null, "9780439708180", "Harry Potter y la piedra filosofal", LocalDate.of(1997, 6, 26), "Primer libro de una saga de fantasía juvenil.", 3L, 2L));
            }
        };
    }
}
