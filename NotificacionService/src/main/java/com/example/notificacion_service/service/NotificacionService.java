package com.example.notificacion_service.service;

import com.example.notificacion_service.dto.NotificacionDTO;
import com.example.notificacion_service.dto.UsuarioResponseDTO;
import com.example.notificacion_service.model.Notificacion;
import com.example.notificacion_service.model.TipoNotificacion;
import com.example.notificacion_service.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.usuario.url}")
    private String usuarioServiceUrl;

    public List<NotificacionDTO> listarTodos() {
        return notificacionRepository.findAll().stream()
                .map(this::toDtoConUsuario)
                .collect(Collectors.toList());
    }

    public NotificacionDTO obtenerPorId(Long id) {
        return notificacionRepository.findById(id)
                .map(this::toDtoConUsuario)
                .orElse(null);
    }

    public List<NotificacionDTO> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDtoConUsuario)
                .collect(Collectors.toList());
    }

    public List<NotificacionDTO> listarPorTipo(TipoNotificacion tipo) {
        return notificacionRepository.findByTipoNotificacion(tipo).stream()
                .map(this::toDtoConUsuario)
                .collect(Collectors.toList());
    }

    public List<NotificacionDTO> listarNoLeidas() {
        return notificacionRepository.findByLeida(false).stream()
                .map(this::toDtoConUsuario)
                .collect(Collectors.toList());
    }

    public NotificacionDTO guardar(NotificacionDTO dto) {
        validar(dto);
        validarUsuario(dto.getUsuarioId());

        Notificacion notificacion = dto.toModel();
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacion.setLeida(dto.getLeida() != null ? dto.getLeida() : false);

        Notificacion guardada = notificacionRepository.save(notificacion);
        return toDtoConUsuario(guardada);
    }

    public NotificacionDTO marcarLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la notificación con ID: " + id));
        notificacion.setLeida(true);
        return toDtoConUsuario(notificacionRepository.save(notificacion));
    }

    public boolean eliminar(Long id) {
        if (!notificacionRepository.existsById(id)) return false;
        notificacionRepository.deleteById(id);
        return true;
    }

    private void validar(NotificacionDTO dto) {
        if (dto == null) throw new RuntimeException("Los datos de notificación son obligatorios");
        if (dto.getUsuarioId() == null) throw new RuntimeException("El ID del usuario es obligatorio");
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) throw new RuntimeException("El título es obligatorio");
        if (dto.getMensaje() == null || dto.getMensaje().trim().isEmpty()) throw new RuntimeException("El mensaje es obligatorio");
        if (dto.getTipoNotificacion() == null) throw new RuntimeException("El tipo de notificación es obligatorio");
    }

    private void validarUsuario(Long usuarioId) {
        try {
            Boolean existe = restTemplate.getForObject(usuarioServiceUrl + "/usuarios/" + usuarioId + "/existe", Boolean.class);
            if (existe == null || !existe) throw new RuntimeException("El usuario no existe");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar el usuario en UsuarioService");
        }
    }

    private NotificacionDTO toDtoConUsuario(Notificacion notificacion) {
        NotificacionDTO dto = NotificacionDTO.fromModel(notificacion);
        try {
            UsuarioResponseDTO usuario = restTemplate.getForObject(usuarioServiceUrl + "/usuarios/" + dto.getUsuarioId(), UsuarioResponseDTO.class);
            if (usuario != null) {
                dto.setNombreUsuario(usuario.getNombre());
                dto.setCorreoUsuario(usuario.getCorreo());
            }
        } catch (Exception e) {
            dto.setNombreUsuario("UsuarioService no disponible");
        }
        return dto;
    }
}
