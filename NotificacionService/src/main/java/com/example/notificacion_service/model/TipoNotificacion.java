package com.example.notificacion_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos funcionales de notificación.", enumAsRef = true)
public enum TipoNotificacion {
    VENCIMIENTO,
    DISPONIBILIDAD_RESERVA,
    BIENVENIDA,
    MULTA
}
