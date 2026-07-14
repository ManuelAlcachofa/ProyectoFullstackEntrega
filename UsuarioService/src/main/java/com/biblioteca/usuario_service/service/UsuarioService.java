package com.biblioteca.usuario_service.service;

import com.biblioteca.usuario_service.dto.LoginDTO;
import com.biblioteca.usuario_service.dto.RegistroDTO;
import com.biblioteca.usuario_service.dto.UsuarioDTO;
import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.model.Usuario;
import com.biblioteca.usuario_service.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UsuarioDTO> login(LoginDTO dto) {
        return usuarioRepository.findByCorreo(dto.getCorreo())
                .filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
                .map(UsuarioDTO::fromModel);
    }

    public UsuarioDTO registrar(RegistroDTO dto) {
        validarRegistro(dto);

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado: " + dto.getCorreo());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setRol(dto.getRol() != null ? dto.getRol() : Rol.CLIENTE);

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioDTO.fromModel(guardado);
    }

    private void validarRegistro(RegistroDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de registro son obligatorios");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.getCorreo() == null || dto.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (!dto.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioDTO::fromModel)
                .collect(Collectors.toList());
    }

    public Optional<UsuarioDTO> buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(UsuarioDTO::fromModel);
    }

    public Optional<UsuarioDTO> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).map(UsuarioDTO::fromModel);
    }

    public List<UsuarioDTO> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol)
                .stream()
                .map(UsuarioDTO::fromModel)
                .collect(Collectors.toList());
    }

    public boolean existePorId(Long id) {
        return usuarioRepository.existsById(id);
    }

    public Optional<UsuarioDTO> actualizar(Long id, UsuarioDTO dto) {
        return usuarioRepository.findById(id).map(u -> {
            if (dto.getNombre() != null) u.setNombre(dto.getNombre());
            if (dto.getCorreo() != null) u.setCorreo(dto.getCorreo());
            if (dto.getRol() != null) u.setRol(dto.getRol());
            return UsuarioDTO.fromModel(usuarioRepository.save(u));
        });
    }

    public boolean eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) return false;
        usuarioRepository.deleteById(id);
        return true;
    }
}
