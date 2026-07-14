package com.example_cat.CategoriaService.config;

import com.example_cat.CategoriaService.model.Categoria;
import com.example_cat.CategoriaService.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarCategoriasIniciales(CategoriaRepository categoriaRepository) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                categoriaRepository.save(new Categoria(null, "Novela", "Obras narrativas extensas para lectura general."));
                categoriaRepository.save(new Categoria(null, "Fantasía", "Libros con elementos mágicos, mundos imaginarios o aventuras fantásticas."));
                categoriaRepository.save(new Categoria(null, "Educación", "Material bibliográfico usado para estudio y formación académica."));
            }
        };
    }
}
