package com.example.inventario.InventarioService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. ENLACE DESACOPLADO CON EL LIBRO
    @NotNull(message = "El ID del libro es obligatorio")
    @Column(name = "libro_id", nullable = false, unique = true) // Un solo registro de inventario por libro
    private Long libroId;

    // 2. STOCK TOTAL DE COPIAS ADQUIRIDAS POR LA BIBLIOTECA
    @NotNull(message = "El stock total es obligatorio")
    @Min(value = 0, message = "El stock total no puede ser un número negativo")
    @Column(name = "stock_total", nullable = false)
    private Integer stockTotal;

    // 3. CANTIDAD DE COPIAS QUE ESTÁN ACTUALMENTE PRESTADAS
    @NotNull(message = "La cantidad de libros prestados es obligatoria")
    @Min(value = 0, message = "Los libros prestados no pueden ser un número negativo")
    @Column(name = "libros_prestados", nullable = false)
    private Integer librosPrestados;

    // 4. CANTIDAD DE COPIAS DISPONIBLES PARA PRÉSTAMO (CALCULADA)
    @Transient
    public Integer getStockDisponible() {
        return this.stockTotal - this.librosPrestados;
    }
}