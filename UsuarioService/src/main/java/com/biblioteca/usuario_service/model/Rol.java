package com.biblioteca.usuario_service.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Roles disponibles para los usuarios.", enumAsRef = true)
public enum Rol {
    ADMINISTRADOR,
    CLIENTE,
    OPERADOR
}
