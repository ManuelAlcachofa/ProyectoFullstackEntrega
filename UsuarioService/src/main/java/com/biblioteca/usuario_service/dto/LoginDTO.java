package com.biblioteca.usuario_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
@Schema(description = "Credenciales utilizadas para iniciar sesión.")

@Data
public class LoginDTO {
    @Schema(description = "Correo electrónico del usuario", example = "ana.admin@biblioteca.cl")
    private String correo;
    @Schema(description = "Contraseña del usuario", example = "admin123")
    private String password;
}
