package com.Libro.LibroService.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import lombok.Data;
@Schema(description = "Datos remotos de un autor.")

@Data
public class AutorResponseDTO {
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;
    @Schema(description = "Nombre del autor", example = "Gabriel")
    private String nombre;
    @Schema(description = "Apellido del autor", example = "García Márquez")
    private String apellido;
    @Schema(description = "Nacionalidad del autor", example = "Colombiana")
    private String nacionalidad;
    @Schema(description = "Resumen biográfico del autor", example = "Escritor y periodista colombiano, premio Nobel de Literatura.")
    private String biografia;

}
