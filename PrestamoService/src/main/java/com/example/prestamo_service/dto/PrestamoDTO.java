package com.example.prestamo_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.model.Prestamo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Schema(description = "Datos de un préstamo de biblioteca.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del usuario relacionado", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Nombre del usuario obtenido desde UsuarioService", example = "Ana Biblioteca")
    private String nombreUsuario;
    @Schema(description = "Correo del usuario obtenido desde UsuarioService", example = "ana.admin@biblioteca.cl")
    private String correoUsuario;

    @Schema(description = "Identificador del libro relacionado", example = "1")
    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @Schema(description = "Título del libro obtenido desde LibroService", example = "Cien años de soledad")
    private String tituloLibro;

    @Schema(description = "Fecha y hora de inicio del préstamo", example = "2026-07-13T10:30:00")
    private LocalDateTime fechaPrestamo;
    @Schema(description = "Fecha y hora límite de devolución", example = "2026-07-27T10:30:00")
    private LocalDateTime fechaDevolucionPactada;
    @Schema(description = "Fecha y hora real de devolución", example = "2026-07-25T09:00:00")
    private LocalDateTime fechaDevolucionReal;
    @Schema(description = "Estado actual del préstamo", example = "ACTIVO")
    private EstadoPrestamo estadoPrestamo;

    public static PrestamoDTO fromModel(Prestamo prestamo) {
        if (prestamo == null) return null;
        PrestamoDTO dto = new PrestamoDTO();
        dto.setId(prestamo.getId());
        dto.setUsuarioId(prestamo.getUsuarioId());
        dto.setLibroId(prestamo.getLibroId());
        dto.setFechaPrestamo(prestamo.getFechaPrestamo());
        dto.setFechaDevolucionPactada(prestamo.getFechaDevolucionPactada());
        dto.setFechaDevolucionReal(prestamo.getFechaDevolucionReal());
        dto.setEstadoPrestamo(prestamo.getEstadoPrestamo());
        return dto;
    }

    public Prestamo toModel() {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(this.id);
        prestamo.setUsuarioId(this.usuarioId);
        prestamo.setLibroId(this.libroId);
        prestamo.setFechaPrestamo(this.fechaPrestamo);
        prestamo.setFechaDevolucionPactada(this.fechaDevolucionPactada);
        prestamo.setFechaDevolucionReal(this.fechaDevolucionReal);
        prestamo.setEstadoPrestamo(this.estadoPrestamo);
        return prestamo;
    }
}
