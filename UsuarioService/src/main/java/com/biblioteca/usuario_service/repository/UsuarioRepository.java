package com.biblioteca.usuario_service.repository;

import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findByRol(Rol rol);
}
