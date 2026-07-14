package com.example.prestamo_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
@Schema(description = "Datos remotos de inventario.")

@Data
public class InventarioResponseDTO {
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Identificador del libro relacionado", example = "1")
    private Long libroId;
    @Schema(description = "Título del libro obtenido desde LibroService", example = "Cien años de soledad")
    private String tituloLibro;
    @Schema(description = "Cantidad total de ejemplares", example = "5", minimum = "0")
    private Integer stockTotal;
    @Schema(description = "Cantidad de ejemplares actualmente prestados", example = "2", minimum = "0")
    private Integer librosPrestados;
    @Schema(description = "Cantidad calculada de ejemplares disponibles", example = "3", minimum = "0")
    private Integer librosDisponibles;
}
