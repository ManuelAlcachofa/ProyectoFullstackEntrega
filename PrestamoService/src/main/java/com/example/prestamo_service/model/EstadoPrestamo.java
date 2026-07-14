package com.example.prestamo_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un préstamo.", enumAsRef = true)
public enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO,
    EN_MORA
}
