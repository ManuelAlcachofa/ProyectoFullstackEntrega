package com.example.inventario.InventarioService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.inventario.InventarioService.model.Inventario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
@Schema(description = "Estado de inventario de un libro.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del libro relacionado", example = "1")
    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @Schema(description = "Título del libro obtenido desde LibroService", example = "Cien años de soledad")
    private String tituloLibro;

    @Schema(description = "Cantidad total de ejemplares", example = "5", minimum = "0")
    @NotNull(message = "El stock total es obligatorio")
    @Min(value = 0, message = "El stock total no puede ser negativo")
    private Integer stockTotal;

    @Schema(description = "Cantidad de ejemplares actualmente prestados", example = "2", minimum = "0")
    @NotNull(message = "Los libros prestados son obligatorios")
    @Min(value = 0, message = "Los libros prestados no pueden ser negativos")
    private Integer librosPrestados;
    
    // Campo calculado para el Frontend o Postman
    @Schema(description = "Cantidad calculada de ejemplares disponibles", example = "3", minimum = "0")
    private Integer librosDisponibles; 

    // --- TRADUCTORES ---

    public static InventarioDTO fromModel(Inventario inventario) {
        if (inventario == null) return null;
        
        InventarioDTO dto = new InventarioDTO();
        dto.setId(inventario.getId());
        dto.setLibroId(inventario.getLibroId());
        dto.setStockTotal(inventario.getStockTotal());
        dto.setLibrosPrestados(inventario.getLibrosPrestados());
        
        // Lógica de negocio: Disponibles = Total - Prestados
        dto.setLibrosDisponibles(inventario.getStockTotal() - inventario.getLibrosPrestados());
        return dto;
    }

    public Inventario toModel() {
        Inventario inventario = new Inventario();
        inventario.setId(this.id);
        inventario.setLibroId(this.libroId);
        inventario.setStockTotal(this.stockTotal);
        inventario.setLibrosPrestados(this.librosPrestados);
        return inventario;
    }
}
