package com.example.resena.ResenaService.dto;

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
}
