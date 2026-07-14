package com.Libro.LibroService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
@Schema(description = "Datos remotos de una categoría.")

@Data
public class CategoriaResponseDTO {
    
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Nombre de la categoría", example = "Novela")
    private String nombre; 
    @Schema(description = "Descripción del recurso", example = "Obras narrativas de ficción extensa.")
    private String descripcion;

}
