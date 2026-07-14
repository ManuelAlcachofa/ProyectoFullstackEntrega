package com.example.multa.MultaService.service;

import com.example.multa.MultaService.dto.MultaDTO;
import com.example.multa.MultaService.dto.UsuarioResponseDTO;
import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.model.Multa;
import com.example.multa.MultaService.repository.MultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MultaService {

    @Autowired
    private MultaRepository multaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.usuario.url}")
    private String usuarioServiceUrl;

    // 1. OBTENER TODAS LAS MULTAS
    public List<MultaDTO> listarTodas() {
        return multaRepository.findAll().stream()
                .map(multa -> {
                    MultaDTO dto = MultaDTO.fromModel(multa);
                    this.inyectarUsuarioEnDto(multa.getUsuarioId(), dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 2. OBTENER POR ID
    public MultaDTO obtenerPorId(Long id) {
        Optional<Multa> multaOpt = multaRepository.findById(id);
        if (multaOpt.isPresent()) {
            Multa multa = multaOpt.get();
            MultaDTO dto = MultaDTO.fromModel(multa);
            this.inyectarUsuarioEnDto(multa.getUsuarioId(), dto);
            return dto;
        }
        return null;
    }

    // 3. EMITIR / CREAR UNA NUEVA MULTA
    public MultaDTO emitir(MultaDTO dto) {
        Multa multa = dto.toModel();
        multa.setEstadoMulta(EstadoMulta.PENDIENTE);
        multa.setFechaEmision(LocalDateTime.now());
        multa.setFechaPago(null);

        Multa nueva = multaRepository.save(multa);
        MultaDTO nuevoDto = MultaDTO.fromModel(nueva);
        this.inyectarUsuarioEnDto(nueva.getUsuarioId(), nuevoDto);
        return nuevoDto;
    }

    // 4. ACTUALIZAR MULTA
    public MultaDTO actualizar(Long id, MultaDTO dto) {
        return multaRepository.findById(id).map(multaExistente -> {
            multaExistente.setPrestamoId(dto.getPrestamoId());
            multaExistente.setUsuarioId(dto.getUsuarioId());
            multaExistente.setMonto(dto.getMonto());
            multaExistente.setDiasRetraso(dto.getDiasRetraso());

            Multa actualizada = multaRepository.save(multaExistente);
            MultaDTO actualizadaDto = MultaDTO.fromModel(actualizada);
            this.inyectarUsuarioEnDto(actualizada.getUsuarioId(), actualizadaDto);
            return actualizadaDto;
        }).orElse(null);
    }

    // 5. ELIMINAR MULTA
    public void eliminar(Long id) {
        multaRepository.deleteById(id);
    }

    // 6. OPERACIÓN DE NEGOCIO: REGISTRAR EL PAGO DE UNA MULTA
    public MultaDTO pagar(Long id) {
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la multa con ID: " + id));

        if (multa.getEstadoMulta() == EstadoMulta.PAGADA) {
            throw new RuntimeException("La multa ya se encuentra pagada");
        }
        if (multa.getEstadoMulta() == EstadoMulta.ANULADA) {
            throw new RuntimeException("No se puede pagar una multa anulada");
        }

        multa.setEstadoMulta(EstadoMulta.PAGADA);
        multa.setFechaPago(LocalDateTime.now());

        Multa actualizada = multaRepository.save(multa);
        MultaDTO dto = MultaDTO.fromModel(actualizada);
        this.inyectarUsuarioEnDto(actualizada.getUsuarioId(), dto);
        return dto;
    }

    // 7. OPERACIÓN DE NEGOCIO: ANULAR UNA MULTA
    public MultaDTO anular(Long id) {
        Multa multa = multaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la multa con ID: " + id));

        if (multa.getEstadoMulta() == EstadoMulta.PAGADA) {
            throw new RuntimeException("No se puede anular una multa que ya fue pagada");
        }

        multa.setEstadoMulta(EstadoMulta.ANULADA);

        Multa actualizada = multaRepository.save(multa);
        MultaDTO dto = MultaDTO.fromModel(actualizada);
        this.inyectarUsuarioEnDto(actualizada.getUsuarioId(), dto);
        return dto;
    }

    // 8. LISTAR MULTAS POR USUARIO
    public List<MultaDTO> listarPorUsuario(Long usuarioId) {
        return multaRepository.findByUsuarioId(usuarioId).stream()
                .map(multa -> {
                    MultaDTO dto = MultaDTO.fromModel(multa);
                    this.inyectarUsuarioEnDto(usuarioId, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 9. LISTAR MULTAS POR ESTADO
    public List<MultaDTO> listarPorEstado(EstadoMulta estado) {
        return multaRepository.findByEstadoMulta(estado).stream()
                .map(multa -> {
                    MultaDTO dto = MultaDTO.fromModel(multa);
                    this.inyectarUsuarioEnDto(multa.getUsuarioId(), dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Método auxiliar para Usuarios
    private void inyectarUsuarioEnDto(Long usuarioId, MultaDTO dto) {
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
