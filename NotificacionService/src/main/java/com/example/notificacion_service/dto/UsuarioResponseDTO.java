package com.example.notificacion_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
@Schema(description = "Datos remotos de un usuario.")

@Data
public class UsuarioResponseDTO {
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Nombre completo del usuario", example = "Ana Biblioteca")
    private String nombre;
    @Schema(description = "Correo electrónico del usuario", example = "ana.admin@biblioteca.cl")
    private String correo;
    @Schema(description = "Rol asignado al usuario", example = "ADMINISTRADOR")
    private String rol;
}
