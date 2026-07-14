package com.example.reserva_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.model.Reserva;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Schema(description = "Datos de una reserva de libro.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del libro relacionado", example = "1")
    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @Schema(description = "Título del libro obtenido desde LibroService", example = "Cien años de soledad")
    private String tituloLibro;

    @Schema(description = "Identificador del usuario relacionado", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Nombre del usuario obtenido desde UsuarioService", example = "Ana Biblioteca")
    private String nombreUsuario;
    @Schema(description = "Correo del usuario obtenido desde UsuarioService", example = "ana.admin@biblioteca.cl")
    private String correoUsuario;
    @Schema(description = "Fecha y hora de creación de la reserva", example = "2026-07-13T10:30:00")
    private LocalDateTime fechaReserva;
    @Schema(description = "Fecha en que el libro quedó disponible", example = "2026-07-15T09:00:00")
    private LocalDateTime fechaDisponibilidad;
    @Schema(description = "Fecha límite para retirar la reserva", example = "2026-07-17T09:00:00")
    private LocalDateTime fechaVencimiento;
    @Schema(description = "Estado actual de la reserva", example = "ACTIVA")
    private EstadoReserva estadoReserva;

    public static ReservaDTO fromModel(Reserva reserva) {
        if (reserva == null) return null;
        ReservaDTO dto = new ReservaDTO();
        dto.setId(reserva.getId());
        dto.setLibroId(reserva.getLibroId());
        dto.setUsuarioId(reserva.getUsuarioId());
        dto.setFechaReserva(reserva.getFechaReserva());
        dto.setFechaDisponibilidad(reserva.getFechaDisponibilidad());
        dto.setFechaVencimiento(reserva.getFechaVencimiento());
        dto.setEstadoReserva(reserva.getEstadoReserva());
        return dto;
    }

    public Reserva toModel() {
        Reserva reserva = new Reserva();
        reserva.setId(this.id);
        reserva.setLibroId(this.libroId);
        reserva.setUsuarioId(this.usuarioId);
        reserva.setFechaReserva(this.fechaReserva);
        reserva.setFechaDisponibilidad(this.fechaDisponibilidad);
        reserva.setFechaVencimiento(this.fechaVencimiento);
        reserva.setEstadoReserva(this.estadoReserva);
        return reserva;
    }
}
