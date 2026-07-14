package com.example.resena.ResenaService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. ENLACE CON EL MICROSERVICIO DE LIBROS (libro calificado)
    @NotNull(message = "El ID del libro es obligatorio")
    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    // 2. ENLACE CON EL MICROSERVICIO DE USUARIOS (cliente que reseña)
    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // 3. CALIFICACIÓN (escala 1 a 5)
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    @Column(nullable = false)
    private Integer calificacion;

    // 4. COMENTARIO (texto libre)
    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String comentario;

    // 5. FECHA EN QUE SE DEJÓ LA RESEÑA
    @NotNull(message = "La fecha de la reseña es obligatoria")
    @Column(name = "fecha_resena", nullable = false)
    private LocalDateTime fechaResena;
}
