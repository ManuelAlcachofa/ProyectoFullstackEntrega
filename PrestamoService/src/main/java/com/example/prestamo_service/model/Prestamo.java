package com.example.prestamo_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @NotNull(message = "El ID del libro es obligatorio")
    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    @NotNull(message = "La fecha de préstamo es obligatoria")
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @NotNull(message = "La fecha de devolución pactada es obligatoria")
    @Column(name = "fecha_devolucion_pactada", nullable = false)
    private LocalDateTime fechaDevolucionPactada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @NotNull(message = "El estado del préstamo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_prestamo", nullable = false, length = 20)
    private EstadoPrestamo estadoPrestamo;
}
