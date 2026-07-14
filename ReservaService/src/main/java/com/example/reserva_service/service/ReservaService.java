package com.example.reserva_service.service;

import com.example.reserva_service.dto.InventarioResponseDTO;
import com.example.reserva_service.dto.LibroResponseDTO;
import com.example.reserva_service.dto.ReservaDTO;
import com.example.reserva_service.dto.UsuarioResponseDTO;
import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.model.Reserva;
import com.example.reserva_service.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.usuario.url}")
    private String usuarioServiceUrl;

    @Value("${services.libro.url}")
    private String libroServiceUrl;

    @Value("${services.inventario.url}")
    private String inventarioServiceUrl;

    public List<ReservaDTO> listarTodos() {
        return reservaRepository.findAll().stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public ReservaDTO obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .map(this::toDtoConDatosExternos)
                .orElse(null);
    }

    public List<ReservaDTO> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> listarPorEstado(EstadoReserva estado) {
        return reservaRepository.findByEstadoReserva(estado).stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public ReservaDTO guardar(ReservaDTO dto) {
        validarDatosBasicos(dto);
        validarUsuario(dto.getUsuarioId());
        validarLibro(dto.getLibroId());
        validarSinStock(dto.getLibroId());

        Reserva reserva = new Reserva();
        reserva.setLibroId(dto.getLibroId());
        reserva.setUsuarioId(dto.getUsuarioId());
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setFechaDisponibilidad(null);
        reserva.setFechaVencimiento(null);
        reserva.setEstadoReserva(EstadoReserva.ACTIVA);

        Reserva guardada = reservaRepository.save(reserva);
        return toDtoConDatosExternos(guardada);
    }

    public ReservaDTO marcarDisponible(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la reserva con ID: " + id));
        reserva.setEstadoReserva(EstadoReserva.DISPONIBLE);
        reserva.setFechaDisponibilidad(LocalDateTime.now());
        reserva.setFechaVencimiento(LocalDateTime.now().plusHours(48));
        return toDtoConDatosExternos(reservaRepository.save(reserva));
    }

    public ReservaDTO completar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la reserva con ID: " + id));
        reserva.setEstadoReserva(EstadoReserva.COMPLETADA);
        return toDtoConDatosExternos(reservaRepository.save(reserva));
    }

    public ReservaDTO cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la reserva con ID: " + id));
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        return toDtoConDatosExternos(reservaRepository.save(reserva));
    }

    public boolean eliminar(Long id) {
        if (!reservaRepository.existsById(id)) return false;
        reservaRepository.deleteById(id);
        return true;
    }

    private void validarDatosBasicos(ReservaDTO dto) {
        if (dto == null) throw new RuntimeException("Los datos de reserva son obligatorios");
        if (dto.getUsuarioId() == null) throw new RuntimeException("El ID del usuario es obligatorio");
        if (dto.getLibroId() == null) throw new RuntimeException("El ID del libro es obligatorio");
    }

    private void validarUsuario(Long usuarioId) {
        try {
            Boolean existe = restTemplate.getForObject(usuarioServiceUrl + "/usuarios/" + usuarioId + "/existe", Boolean.class);
            if (existe == null || !existe) throw new RuntimeException("El usuario no existe");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar el usuario en UsuarioService");
        }
    }

    private void validarLibro(Long libroId) {
        try {
            LibroResponseDTO libro = restTemplate.getForObject(libroServiceUrl + "/libros/" + libroId, LibroResponseDTO.class);
            if (libro == null || libro.getId() == null) throw new RuntimeException("El libro no existe");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar el libro en LibroService");
        }
    }

    private void validarSinStock(Long libroId) {
        try {
            InventarioResponseDTO inventario = restTemplate.getForObject(inventarioServiceUrl + "/inventario/libro/" + libroId, InventarioResponseDTO.class);
            if (inventario != null && inventario.getLibrosDisponibles() != null && inventario.getLibrosDisponibles() > 0) {
                throw new RuntimeException("El libro aún tiene stock disponible; no corresponde generar reserva");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar el inventario en InventarioService");
        }
    }

    private ReservaDTO toDtoConDatosExternos(Reserva reserva) {
        ReservaDTO dto = ReservaDTO.fromModel(reserva);
        inyectarUsuario(dto);
        inyectarLibro(dto);
        return dto;
    }

    private void inyectarUsuario(ReservaDTO dto) {
        try {
            UsuarioResponseDTO usuario = restTemplate.getForObject(usuarioServiceUrl + "/usuarios/" + dto.getUsuarioId(), UsuarioResponseDTO.class);
            if (usuario != null) {
                dto.setNombreUsuario(usuario.getNombre());
                dto.setCorreoUsuario(usuario.getCorreo());
            }
        } catch (Exception e) {
            dto.setNombreUsuario("UsuarioService no disponible");
        }
    }

    private void inyectarLibro(ReservaDTO dto) {
        try {
            LibroResponseDTO libro = restTemplate.getForObject(libroServiceUrl + "/libros/" + dto.getLibroId(), LibroResponseDTO.class);
            if (libro != null) dto.setTituloLibro(libro.getTitulo());
        } catch (Exception e) {
            dto.setTituloLibro("LibroService no disponible");
        }
    }
}
