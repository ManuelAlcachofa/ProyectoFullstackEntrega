package com.biblioteca.usuario_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.model.Usuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
@Schema(description = "Datos públicos de un usuario.")

@Data
@Builder
public class UsuarioDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Nombre completo del usuario", example = "Ana Biblioteca")
    private String nombre;
    @Schema(description = "Correo electrónico del usuario", example = "ana.admin@biblioteca.cl")
    private String correo;
    @Schema(description = "Rol asignado al usuario", example = "ADMINISTRADOR")
    private Rol rol;
    @Schema(description = "Fecha de registro del usuario", example = "2026-07-13")
    private LocalDate fechaRegistro;

    public static UsuarioDTO fromModel(Usuario u) {
        return UsuarioDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}
