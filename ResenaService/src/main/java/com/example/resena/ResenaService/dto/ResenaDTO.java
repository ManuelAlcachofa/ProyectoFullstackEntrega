package com.example.resena.ResenaService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.resena.ResenaService.model.Resena;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Schema(description = "Datos de una reseña y calificación.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del libro relacionado", example = "1")
    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @Schema(description = "Identificador del usuario relacionado", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Calificación del libro entre 1 y 5", example = "5", minimum = "1", maximum = "5")
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    @Schema(description = "Opinión escrita por el usuario", example = "Una novela inolvidable y muy bien construida.")
    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
    private String comentario;

    @Schema(description = "Fecha y hora de creación de la reseña", example = "2026-07-13T10:30:00")
    private LocalDateTime fechaResena;

    // --- DATOS PARA LA RELACIÓN CON LIBRO ---
    @Schema(description = "Título del libro obtenido desde LibroService", example = "Cien años de soledad")
    private String tituloLibro;

    // --- DATOS PARA LA RELACIÓN CON USUARIO ---
    @Schema(description = "Nombre del usuario obtenido desde UsuarioService", example = "Ana Biblioteca")
    private String nombreUsuario;

    // --- TRADUCTORES ---

    public static ResenaDTO fromModel(Resena resena) {
        if (resena == null) return null;
        ResenaDTO dto = new ResenaDTO();
        dto.setId(resena.getId());
        dto.setLibroId(resena.getLibroId());
        dto.setUsuarioId(resena.getUsuarioId());
        dto.setCalificacion(resena.getCalificacion());
        dto.setComentario(resena.getComentario());
        dto.setFechaResena(resena.getFechaResena());
        return dto;
    }

    public Resena toModel() {
        Resena resena = new Resena();
        resena.setId(this.id);
        resena.setLibroId(this.libroId);
        resena.setUsuarioId(this.usuarioId);
        resena.setCalificacion(this.calificacion);
        resena.setComentario(this.comentario);
        resena.setFechaResena(this.fechaResena);
        return resena;
    }
}
