package com.example.notificacion_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "prestamo_id")
    private Long prestamoId;

    @Column(name = "reserva_id")
    private Long reservaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @NotNull(message = "El tipo de notificación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false, length = 40)
    private TipoNotificacion tipoNotificacion;

    @NotNull(message = "La fecha de envío es obligatoria")
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private Boolean leida;
}
