package com.example.multa.MultaService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "multas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. ENLACE CON EL MICROSERVICIO DE PRÉSTAMOS (origen de la multa)
    @NotNull(message = "El ID del préstamo es obligatorio")
    @Column(name = "prestamo_id", nullable = false)
    private Long prestamoId;

    // 2. ENLACE CON EL MICROSERVICIO DE USUARIOS (a quién cobrarle)
    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // 3. MONTO DE LA MULTA
    @NotNull(message = "El monto de la multa es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    // 4. DÍAS DE RETRASO (justifica el monto ante auditorías)
    @NotNull(message = "Los días de retraso son obligatorios")
    @Min(value = 1, message = "Los días de retraso deben ser al menos 1")
    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso;

    // 5. ESTADO DE LA MULTA
    @NotNull(message = "El estado de la multa es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_multa", nullable = false, length = 20)
    private EstadoMulta estadoMulta;

    // 6. FECHA DE EMISIÓN (momento exacto en que se calculó y cargó la multa)
    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    // 7. FECHA DE PAGO (nullable, se registra cuando el usuario salda la deuda)
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
}
