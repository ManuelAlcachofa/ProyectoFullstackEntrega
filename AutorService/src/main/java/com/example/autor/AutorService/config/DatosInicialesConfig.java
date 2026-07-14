package com.example.autor.AutorService.config;

import com.example.autor.AutorService.model.Autor;
import com.example.autor.AutorService.repository.AutorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarAutoresIniciales(AutorRepository autorRepository) {
        return args -> {
            if (autorRepository.count() == 0) {
                autorRepository.save(new Autor(null, "Gabriel", "García Márquez", "Colombiana", "Autor de Cien años de soledad y referente del realismo mágico."));
                autorRepository.save(new Autor(null, "Isabel", "Allende", "Chilena", "Escritora chilena reconocida por novelas históricas y familiares."));
                autorRepository.save(new Autor(null, "J. K.", "Rowling", "Británica", "Autora de literatura fantástica juvenil."));
            }
        };
    }
}
