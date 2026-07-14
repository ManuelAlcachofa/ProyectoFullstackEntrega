package com.example.prestamo_service.service;

import com.example.prestamo_service.dto.InventarioResponseDTO;
import com.example.prestamo_service.dto.LibroResponseDTO;
import com.example.prestamo_service.dto.PrestamoDTO;
import com.example.prestamo_service.dto.UsuarioResponseDTO;
import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.model.Prestamo;
import com.example.prestamo_service.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.usuario.url}")
    private String usuarioServiceUrl;

    @Value("${services.libro.url}")
    private String libroServiceUrl;

    @Value("${services.inventario.url}")
    private String inventarioServiceUrl;

    public List<PrestamoDTO> listarTodos() {
        return prestamoRepository.findAll().stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public PrestamoDTO obtenerPorId(Long id) {
        return prestamoRepository.findById(id)
                .map(this::toDtoConDatosExternos)
                .orElse(null);
    }

    public List<PrestamoDTO> listarPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public List<PrestamoDTO> listarPorEstado(EstadoPrestamo estado) {
        return prestamoRepository.findByEstadoPrestamo(estado).stream()
                .map(this::toDtoConDatosExternos)
                .collect(Collectors.toList());
    }

    public PrestamoDTO guardar(PrestamoDTO dto) {
        validarDatosBasicos(dto);
        validarUsuario(dto.getUsuarioId());
        validarLibro(dto.getLibroId());
        validarStockDisponible(dto.getLibroId());

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuarioId(dto.getUsuarioId());
        prestamo.setLibroId(dto.getLibroId());
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setFechaDevolucionPactada(LocalDateTime.now().plusDays(14));
        prestamo.setFechaDevolucionReal(null);
        prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);

        descontarStock(dto.getLibroId());
        Prestamo guardado = prestamoRepository.save(prestamo);
        return toDtoConDatosExternos(guardado);
    }

    public PrestamoDTO actualizar(Long id, PrestamoDTO dto) {
        return prestamoRepository.findById(id).map(prestamo -> {
            if (dto.getEstadoPrestamo() != null) prestamo.setEstadoPrestamo(dto.getEstadoPrestamo());
            if (dto.getFechaDevolucionPactada() != null) prestamo.setFechaDevolucionPactada(dto.getFechaDevolucionPactada());
            Prestamo actualizado = prestamoRepository.save(prestamo);
            return toDtoConDatosExternos(actualizado);
        }).orElse(null);
    }

    public PrestamoDTO devolver(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el préstamo con ID: " + id));

        if (prestamo.getEstadoPrestamo() == EstadoPrestamo.DEVUELTO) {
            throw new RuntimeException("El préstamo ya fue devuelto");
        }

        prestamo.setFechaDevolucionReal(LocalDateTime.now());
        prestamo.setEstadoPrestamo(EstadoPrestamo.DEVUELTO);
        devolverStock(prestamo.getLibroId());

        Prestamo actualizado = prestamoRepository.save(prestamo);
        return toDtoConDatosExternos(actualizado);
    }

    public boolean eliminar(Long id) {
        if (!prestamoRepository.existsById(id)) return false;
        prestamoRepository.deleteById(id);
        return true;
    }

    private void validarDatosBasicos(PrestamoDTO dto) {
        if (dto == null) throw new RuntimeException("Los datos del préstamo son obligatorios");
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

    private void validarStockDisponible(Long libroId) {
        try {
            InventarioResponseDTO inventario = restTemplate.getForObject(inventarioServiceUrl + "/inventario/libro/" + libroId, InventarioResponseDTO.class);
            if (inventario == null || inventario.getLibrosDisponibles() == null || inventario.getLibrosDisponibles() <= 0) {
                throw new RuntimeException("No hay stock disponible para prestar este libro");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo validar el inventario en InventarioService");
        }
    }

    private void descontarStock(Long libroId) {
        restTemplate.put(inventarioServiceUrl + "/inventario/prestar/" + libroId, null);
    }

    private void devolverStock(Long libroId) {
        restTemplate.put(inventarioServiceUrl + "/inventario/devolver/" + libroId, null);
    }

    private PrestamoDTO toDtoConDatosExternos(Prestamo prestamo) {
        PrestamoDTO dto = PrestamoDTO.fromModel(prestamo);
        inyectarUsuario(dto);
        inyectarLibro(dto);
        return dto;
    }

    private void inyectarUsuario(PrestamoDTO dto) {
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

    private void inyectarLibro(PrestamoDTO dto) {
        try {
            LibroResponseDTO libro = restTemplate.getForObject(libroServiceUrl + "/libros/" + dto.getLibroId(), LibroResponseDTO.class);
            if (libro != null) dto.setTituloLibro(libro.getTitulo());
        } catch (Exception e) {
            dto.setTituloLibro("LibroService no disponible");
        }
    }
}
