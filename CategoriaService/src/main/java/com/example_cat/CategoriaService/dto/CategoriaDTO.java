package com.example_cat.CategoriaService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example_cat.CategoriaService.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Datos de una categoría de libros.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Nombre único de la categoría", example = "Novela")
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Schema(description = "Descripción del recurso", example = "Obras narrativas de ficción extensa.")
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    // --- TRADUCTORES MÁGICOS ---

    // De Modelo a DTO
    public static CategoriaDTO fromModel(Categoria categoria) {
        if (categoria == null) return null;
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }

    // De DTO a Modelo
    public Categoria toModel() {
        Categoria categoria = new Categoria();
        categoria.setId(this.id);
        categoria.setNombre(this.nombre);
        categoria.setDescripcion(this.descripcion);
        return categoria;
    }
}
