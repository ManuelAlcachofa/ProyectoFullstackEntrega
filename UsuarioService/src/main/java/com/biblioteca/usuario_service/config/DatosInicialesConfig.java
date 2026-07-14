package com.biblioteca.usuario_service.config;

import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.model.Usuario;
import com.biblioteca.usuario_service.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
// datos de prueba con BCrypt ya existente, sin cambios

import java.time.LocalDate;

@Configuration
public class DatosInicialesConfig {

    @Bean
    CommandLineRunner cargarUsuariosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Usuario pepe = new Usuario();
                pepe.setNombre("Pepe González");
                pepe.setCorreo("pepe@gmail.com");
                pepe.setPassword(passwordEncoder.encode("123456"));
                pepe.setRol(Rol.CLIENTE);
                pepe.setFechaRegistro(LocalDate.now());
                usuarioRepository.save(pepe);

                Usuario admin = new Usuario();
                admin.setNombre("Ana Biblioteca");
                admin.setCorreo("ana.admin@biblioteca.cl");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol(Rol.ADMINISTRADOR);
                admin.setFechaRegistro(LocalDate.now());
                usuarioRepository.save(admin);

                Usuario operador = new Usuario();
                operador.setNombre("Carlos Operador");
                operador.setCorreo("carlos.operador@biblioteca.cl");
                operador.setPassword(passwordEncoder.encode("operador123"));
                operador.setRol(Rol.OPERADOR);
                operador.setFechaRegistro(LocalDate.now());
                usuarioRepository.save(operador);
            }
        };
    }
}
