package com.example.inventario.InventarioService.config;

import com.example.inventario.InventarioService.model.Inventario;
import com.example.inventario.InventarioService.repository.InventarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarInventarioInicial(InventarioRepository inventarioRepository) {
        return args -> {
            if (inventarioRepository.count() == 0) {
                inventarioRepository.save(new Inventario(null, 1L, 5, 1));
                inventarioRepository.save(new Inventario(null, 2L, 3, 0));
                inventarioRepository.save(new Inventario(null, 3L, 4, 4));
            }
        };
    }
}
