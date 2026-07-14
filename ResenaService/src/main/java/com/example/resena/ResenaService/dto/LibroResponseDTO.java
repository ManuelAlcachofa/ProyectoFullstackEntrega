package com.example.resena.ResenaService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
@Schema(description = "Datos remotos de un libro.")

@Data
public class LibroResponseDTO {
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Código ISBN-10 o ISBN-13 del libro", example = "9780307474728")
    private String isbn;
    @Schema(description = "Título del libro o de la notificación", example = "Cien años de soledad")
    private String titulo;
}
