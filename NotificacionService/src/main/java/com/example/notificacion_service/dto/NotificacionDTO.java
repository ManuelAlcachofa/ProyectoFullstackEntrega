package com.example.notificacion_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.notificacion_service.model.Notificacion;
import com.example.notificacion_service.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Schema(description = "Datos de una notificación enviada a un usuario.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del usuario relacionado", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Nombre del usuario obtenido desde UsuarioService", example = "Ana Biblioteca")
    private String nombreUsuario;
    @Schema(description = "Correo del usuario obtenido desde UsuarioService", example = "ana.admin@biblioteca.cl")
    private String correoUsuario;
    @Schema(description = "Identificador del préstamo relacionado", example = "1")
    private Long prestamoId;
    @Schema(description = "Identificador de la reserva relacionada", example = "1")
    private Long reservaId;

    @Schema(description = "Título breve de la notificación", example = "Reserva disponible")
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Schema(description = "Contenido de la notificación", example = "Tu reserva ya se encuentra disponible.")
    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    @Schema(description = "Tipo funcional de la notificación", example = "DISPONIBILIDAD_RESERVA")
    @NotNull(message = "El tipo de notificación es obligatorio")
    private TipoNotificacion tipoNotificacion;

    @Schema(description = "Fecha y hora de envío", example = "2026-07-13T10:30:00")
    private LocalDateTime fechaEnvio;
    @Schema(description = "Indica si la notificación fue leída", example = "false")
    private Boolean leida;

    public static NotificacionDTO fromModel(Notificacion notificacion) {
        if (notificacion == null) return null;
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId());
        dto.setUsuarioId(notificacion.getUsuarioId());
        dto.setPrestamoId(notificacion.getPrestamoId());
        dto.setReservaId(notificacion.getReservaId());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setTipoNotificacion(notificacion.getTipoNotificacion());
        dto.setFechaEnvio(notificacion.getFechaEnvio());
        dto.setLeida(notificacion.getLeida());
        return dto;
    }

    public Notificacion toModel() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(this.id);
        notificacion.setUsuarioId(this.usuarioId);
        notificacion.setPrestamoId(this.prestamoId);
        notificacion.setReservaId(this.reservaId);
        notificacion.setTitulo(this.titulo);
        notificacion.setMensaje(this.mensaje);
        notificacion.setTipoNotificacion(this.tipoNotificacion);
        notificacion.setFechaEnvio(this.fechaEnvio);
        notificacion.setLeida(this.leida);
        return notificacion;
    }
}
