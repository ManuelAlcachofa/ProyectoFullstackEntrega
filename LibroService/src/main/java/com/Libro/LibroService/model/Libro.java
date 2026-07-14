package com.Libro.LibroService.model;

import jakarta.persistence.*; // Si usan Spring Boot 3+
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "libros")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Libro {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. VALIDACIÓN DEL ISBN
    // No puede estar vacío y debe cumplir con el formato estándar de un ISBN (de 10 o 13 dígitos)
    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^(?:\\d{9}[\\dX]|\\d{13})$", message = "El formato del ISBN no es válido (debe tener 10 o 13 dígitos numéricos)")
    @Column(nullable = false, unique = true, length = 17)
    private String isbn; 

    // 2. VALIDACIÓN DEL TÍTULO
    // Filtro invisible: obligatorio y máximo 150 caracteres para proteger la base de datos
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    // 3. VALIDACIÓN DE LA FECHA
    // Al ser un objeto de fecha, usamos @NotNull en lugar de @NotBlank
    @NotNull(message = "La fecha de publicación es obligatoria")
    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion; 

    // 4. VALIDACIÓN DE LA SINOPSIS
    // Obligatoria y con soporte para textos extremadamente largos en MySQL usando TEXT
    @NotBlank(message = "La sinopsis es obligatoria")
    @Column(columnDefinition = "TEXT") 
    private String sinopsis; 

    // 5. ENLACE CON EL MICROSERVICIO DE AUTOR
    // Validamos que obligatoriamente se asigne un ID de autor al crear el libro
    @NotNull(message = "El ID del autor es obligatorio para enlazar el libro")
    @Column(name = "autor_id", nullable = false)
    private Long autorId; 
    
    // 6. ENLACE CON EL MICROSERVICIO DE CATEGORÍA
    @NotNull(message = "El ID de la categoría es obligatorio")
    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;
}