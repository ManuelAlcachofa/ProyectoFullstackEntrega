package com.example.autor.AutorService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "autores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido del autor es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank(message = "La nacionalidad del autor es obligatoria")
    @Size(max = 50, message = "La nacionalidad no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nacionalidad;

    @Size(max = 1000, message = "La biografía no puede superar los 1000 caracteres")
    @Column(length = 1000) 
    private String biografia;
}
