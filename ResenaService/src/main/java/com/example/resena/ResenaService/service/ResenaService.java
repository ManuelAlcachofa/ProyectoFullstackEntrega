package com.example.resena.ResenaService.service;

import com.example.resena.ResenaService.dto.LibroResponseDTO;
import com.example.resena.ResenaService.dto.ResenaDTO;
import com.example.resena.ResenaService.dto.UsuarioResponseDTO;
import com.example.resena.ResenaService.model.Resena;
import com.example.resena.ResenaService.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.libro.url}")
    private String libroServiceUrl;

    @Value("${services.usuario.url}")
    private String usuarioServiceUrl;

    // 1. OBTENER TODAS LAS RESEÑAS
    public List<ResenaDTO> listarTodas() {
        return resenaRepository.findAll().stream()
                .map(this::enriquecer)
                .collect(Collectors.toList());
    }

    // 2. OBTENER POR ID
    public ResenaDTO obtenerPorId(Long id) {
        return resenaRepository.findById(id)
                .map(this::enriquecer)
                .orElse(null);
    }

    // 3. CREAR UNA NUEVA RESEÑA (Regla de negocio: un usuario no puede reseñar dos veces el mismo libro)
    public ResenaDTO guardar(ResenaDTO dto) {
        Optional<Resena> existente = resenaRepository.findByLibroIdAndUsuarioId(dto.getLibroId(), dto.getUsuarioId());
        if (existente.isPresent()) {
            throw new RuntimeException("El usuario ya dejó una reseña para este libro");
        }

        Resena resena = dto.toModel();
        resena.setFechaResena(LocalDateTime.now());

        Resena nueva = resenaRepository.save(resena);
        return enriquecer(nueva);
    }

    // 4. ACTUALIZAR UNA RESEÑA EXISTENTE
    public ResenaDTO actualizar(Long id, ResenaDTO dto) {
        return resenaRepository.findById(id).map(resenaExistente -> {
            resenaExistente.setCalificacion(dto.getCalificacion());
            resenaExistente.setComentario(dto.getComentario());

            Resena actualizada = resenaRepository.save(resenaExistente);
            return enriquecer(actualizada);
        }).orElse(null);
    }

    // 5. ELIMINAR UNA RESEÑA
    public void eliminar(Long id) {
        resenaRepository.deleteById(id);
    }

    // 6. LISTAR RESEÑAS DE UN LIBRO
    public List<ResenaDTO> listarPorLibro(Long libroId) {
        return resenaRepository.findByLibroId(libroId).stream()
                .map(this::enriquecer)
                .collect(Collectors.toList());
    }

    // 7. LISTAR RESEÑAS DE UN USUARIO
    public List<ResenaDTO> listarPorUsuario(Long usuarioId) {
        return resenaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::enriquecer)
                .collect(Collectors.toList());
    }

    // 8. CALCULAR EL PROMEDIO DE CALIFICACIÓN DE UN LIBRO
    public Double calcularPromedioPorLibro(Long libroId) {
        List<Resena> resenas = resenaRepository.findByLibroId(libroId);
        if (resenas.isEmpty()) {
            return 0.0;
        }
        return resenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
    }

    // --- Métodos auxiliares de enriquecimiento ---

    private ResenaDTO enriquecer(Resena resena) {
        ResenaDTO dto = ResenaDTO.fromModel(resena);
        this.inyectarLibroEnDto(resena.getLibroId(), dto);
        this.inyectarUsuarioEnDto(resena.getUsuarioId(), dto);
        return dto;
    }

    // Método auxiliar para Libros
    private void inyectarLibroEnDto(Long libroId, ResenaDTO dto) {
        String titulo = "Libro no disponible";
        try {
            String urlLibroService = libroServiceUrl + "/libros/" + libroId;
            LibroResponseDTO libro = restTemplate.getForObject(urlLibroService, LibroResponseDTO.class);

            if (libro != null) {
                titulo = libro.getTitulo();
            }
        } catch (Exception e) {
            titulo = "Error al conectar con Libros";
        }
        dto.setTituloLibro(titulo);
    }

    // Método auxiliar para Usuarios
    private void inyectarUsuarioEnDto(Long usuarioId, ResenaDTO dto) {
        String nombre = "Usuario no disponible";
        try {
            String urlUsuarioService = usuarioServiceUrl + "/usuarios/" + usuarioId;
            UsuarioResponseDTO usuario = restTemplate.getForObject(urlUsuarioService, UsuarioResponseDTO.class);

            if (usuario != null) {
                nombre = usuario.getNombre();
            }
        } catch (Exception e) {
            nombre = "Error al conectar con Usuarios";
        }
        dto.setNombreUsuario(nombre);
    }
}
