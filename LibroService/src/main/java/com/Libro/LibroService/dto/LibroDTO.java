package com.Libro.LibroService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.Libro.LibroService.model.Libro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Schema(description = "Datos completos de un libro.")

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibroDTO {
    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Código ISBN-10 o ISBN-13 del libro", example = "9780307474728")
    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^(?:\\d{9}[\\dX]|\\d{13})$", message = "El formato del ISBN no es válido")
    private String isbn;

    @Schema(description = "Título del libro o de la notificación", example = "Cien años de soledad")
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    private String titulo;

    @Schema(description = "Fecha de publicación del libro", example = "1967-06-05")
    @NotNull(message = "La fecha de publicación es obligatoria")
    private LocalDate fechaPublicacion;

    @Schema(description = "Resumen argumental del libro", example = "La historia de la familia Buendía a lo largo de varias generaciones.")
    @NotBlank(message = "La sinopsis es obligatoria")
    private String sinopsis;

    @Schema(description = "Identificador de la categoría relacionada", example = "1")
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;
    
    // --- DATOS PARA LA RELACIÓN CON CATEGORÍA ---
    @Schema(description = "Nombre de la categoría obtenido desde CategoriaService", example = "Novela")
    private String nombreCategoria;
    
    // --- DATOS PARA LA RELACIÓN CON AUTOR ---
    @Schema(description = "Identificador del autor relacionado", example = "1")
    @NotNull(message = "El ID del autor es obligatorio")
    private Long autorId; 
    @Schema(description = "Nombre del autor obtenido desde AutorService", example = "Gabriel")
    private String nombreAutor;
    @Schema(description = "Apellido del autor obtenido desde AutorService", example = "García Márquez")
    private String apellidoAutor;

    // 1. Convertir de Entidad (Modelo) a DTO
    public static LibroDTO fromModel(Libro libro) {
        if (libro == null) return null;

        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setIsbn(libro.getIsbn());
        dto.setTitulo(libro.getTitulo());
        dto.setFechaPublicacion(libro.getFechaPublicacion());
        dto.setSinopsis(libro.getSinopsis());
        dto.setAutorId(libro.getAutorId());
        dto.setCategoriaId(libro.getCategoriaId());
        
        // Tanto los datos del autor como el de categoría se quedan vacíos aquí.
        // El Service se encargará de llenarlos haciendo las llamadas HTTP.
        return dto;
    }

    // 2. Convertir de DTO a Entidad (Modelo)
    public Libro toModel() {
        
        Libro libro = new Libro();
        libro.setId(this.id);
        libro.setIsbn(this.isbn);
        libro.setTitulo(this.titulo);
        libro.setFechaPublicacion(this.fechaPublicacion);
        libro.setSinopsis(this.sinopsis);
        libro.setAutorId(this.autorId);
        libro.setCategoriaId(this.categoriaId);
        return libro;
    }
}
