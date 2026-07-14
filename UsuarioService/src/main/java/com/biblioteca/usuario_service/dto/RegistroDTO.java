package com.biblioteca.usuario_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.biblioteca.usuario_service.model.Rol;
import lombok.Data;
@Schema(description = "Datos necesarios para registrar un usuario.")

@Data
public class RegistroDTO {
    @Schema(description = "Nombre completo del usuario", example = "Ana Biblioteca")
    private String nombre;
    @Schema(description = "Correo electrónico del usuario", example = "ana.admin@biblioteca.cl")
    private String correo;
    @Schema(description = "Contraseña del usuario", example = "admin123")
    private String password;
    @Schema(description = "Rol asignado al usuario", example = "ADMINISTRADOR")
    private Rol rol;
}
