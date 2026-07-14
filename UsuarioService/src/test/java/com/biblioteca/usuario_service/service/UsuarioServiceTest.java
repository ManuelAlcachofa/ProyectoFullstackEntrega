package com.biblioteca.usuario_service.service;

import com.biblioteca.usuario_service.dto.LoginDTO;
import com.biblioteca.usuario_service.dto.RegistroDTO;
import com.biblioteca.usuario_service.dto.UsuarioDTO;
import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.model.Usuario;
import com.biblioteca.usuario_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarUsuarioNuevoDebeCifrarPasswordYGuardar() {
        RegistroDTO dto = crearRegistro();
        when(usuarioRepository.existsByCorreo(dto.getCorreo())).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("password-cifrada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioDTO resultado = usuarioService.registrar(dto);

        assertEquals(1L, resultado.getId());
        assertEquals(Rol.CLIENTE, resultado.getRol());
        assertEquals(LocalDate.now(), resultado.getFechaRegistro());
        verify(passwordEncoder).encode("secreto123");
        verify(usuarioRepository).save(argThat(usuario -> "password-cifrada".equals(usuario.getPassword())));
    }

    @Test
    void registrarCorreoDuplicadoDebeLanzarExcepcion() {
        RegistroDTO dto = crearRegistro();
        when(usuarioRepository.existsByCorreo(dto.getCorreo())).thenReturn(true);

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrar(dto));

        assertTrue(excepcion.getMessage().contains("ya está registrado"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarCorreoInvalidoDebeLanzarExcepcion() {
        RegistroDTO dto = crearRegistro();
        dto.setCorreo("correo-invalido");

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrar(dto));

        assertEquals("El correo no tiene un formato válido", excepcion.getMessage());
        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    void registrarPasswordCortaDebeLanzarExcepcion() {
        RegistroDTO dto = crearRegistro();
        dto.setPassword("123");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(dto));
    }

    @Test
    void loginCorrectoDebeRetornarUsuario() {
        LoginDTO login = new LoginDTO();
        login.setCorreo("ana@biblioteca.cl");
        login.setPassword("secreto123");
        Usuario usuario = crearUsuario();
        when(usuarioRepository.findByCorreo(login.getCorreo())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreto123", "password-cifrada")).thenReturn(true);

        Optional<UsuarioDTO> resultado = usuarioService.login(login);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Biblioteca", resultado.get().getNombre());
    }

    @Test
    void loginIncorrectoDebeRetornarVacio() {
        LoginDTO login = new LoginDTO();
        login.setCorreo("ana@biblioteca.cl");
        login.setPassword("incorrecta");
        when(usuarioRepository.findByCorreo(login.getCorreo())).thenReturn(Optional.of(crearUsuario()));
        when(passwordEncoder.matches("incorrecta", "password-cifrada")).thenReturn(false);

        assertTrue(usuarioService.login(login).isEmpty());
    }

    @Test
    void actualizarUsuarioExistenteDebeModificarSoloCamposEntregados() {
        Usuario usuario = crearUsuario();
        UsuarioDTO cambios = UsuarioDTO.builder().nombre("Ana Actualizada").rol(Rol.ADMINISTRADOR).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<UsuarioDTO> resultado = usuarioService.actualizar(1L, cambios);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Actualizada", resultado.get().getNombre());
        assertEquals(Rol.ADMINISTRADOR, resultado.get().getRol());
        assertEquals("ana@biblioteca.cl", resultado.get().getCorreo());
    }

    @Test
    void eliminarUsuarioInexistenteDebeRetornarFalse() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertFalse(usuarioService.eliminar(99L));
        verify(usuarioRepository, never()).deleteById(any());
    }

    private RegistroDTO crearRegistro() {
        RegistroDTO dto = new RegistroDTO();
        dto.setNombre("Ana Biblioteca");
        dto.setCorreo("ana@biblioteca.cl");
        dto.setPassword("secreto123");
        return dto;
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Ana Biblioteca");
        usuario.setCorreo("ana@biblioteca.cl");
        usuario.setPassword("password-cifrada");
        usuario.setRol(Rol.CLIENTE);
        usuario.setFechaRegistro(LocalDate.now());
        return usuario;
    }
}
