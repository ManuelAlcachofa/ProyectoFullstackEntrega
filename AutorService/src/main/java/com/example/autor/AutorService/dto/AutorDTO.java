package com.example.autor.AutorService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.autor.AutorService.model.Autor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Datos de un autor de la biblioteca.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutorDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Nombre de la persona o recurso", example = "Gabriel")
    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Schema(description = "Apellido del autor", example = "García Márquez")
    @NotBlank(message = "El apellido del autor es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;

    @Schema(description = "Nacionalidad del autor", example = "Colombiana")
    @NotBlank(message = "La nacionalidad del autor es obligatoria")
    @Size(max = 50, message = "La nacionalidad no puede superar los 50 caracteres")
    private String nacionalidad;

    @Schema(description = "Resumen biográfico del autor", example = "Escritor y periodista colombiano, premio Nobel de Literatura.")
    @Size(max = 1000, message = "La biografía no puede superar los 1000 caracteres")
    private String biografia;

    public static AutorDTO fromModel(Autor autor) {
        if (autor == null) return null;
        AutorDTO dto = new AutorDTO();
        dto.setId(autor.getId());
        dto.setNombre(autor.getNombre());
        dto.setApellido(autor.getApellido());
        dto.setNacionalidad(autor.getNacionalidad());
        dto.setBiografia(autor.getBiografia());
        return dto;
    }

    public Autor toModel() {
        Autor autor = new Autor();
        autor.setId(this.id);
        autor.setNombre(this.nombre);
        autor.setApellido(this.apellido);
        autor.setNacionalidad(this.nacionalidad);
        autor.setBiografia(this.biografia); 
        return autor;
    }
}
