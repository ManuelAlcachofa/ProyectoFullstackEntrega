package com.example.reserva_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de una reserva.", enumAsRef = true)
public enum EstadoReserva {
    ACTIVA,
    DISPONIBLE,
    COMPLETADA,
    CANCELADA,
    EXPIRADA
}
