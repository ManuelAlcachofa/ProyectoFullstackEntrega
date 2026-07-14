package com.example.multa.MultaService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.model.Multa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Schema(description = "Datos de una multa asociada a un préstamo.")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultaDTO {

    @Schema(description = "Identificador único generado por el sistema", example = "1")
    private Long id;

    @Schema(description = "Identificador del préstamo relacionado", example = "1")
    @NotNull(message = "El ID del préstamo es obligatorio")
    private Long prestamoId;

    @Schema(description = "Identificador del usuario relacionado", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Monto de la multa en pesos", example = "3500.00", minimum = "0")
    @NotNull(message = "El monto de la multa es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El monto no puede ser negativo")
    private BigDecimal monto;

    @Schema(description = "Cantidad de días de atraso", example = "3", minimum = "1")
    @NotNull(message = "Los días de retraso son obligatorios")
    @Min(value = 1, message = "Los días de retraso deben ser al menos 1")
    private Integer diasRetraso;

    @Schema(description = "Estado actual de la multa", example = "PENDIENTE")
    private EstadoMulta estadoMulta;

    @Schema(description = "Fecha y hora de emisión", example = "2026-07-13T10:30:00")
    private LocalDateTime fechaEmision;

    @Schema(description = "Fecha y hora de pago, si corresponde", example = "2026-07-14T12:00:00")
    private LocalDateTime fechaPago;

    // Campo enriquecido (no persistido): nombre del usuario, vía UsuarioService
    @Schema(description = "Nombre del usuario obtenido desde UsuarioService", example = "Ana Biblioteca")
    private String nombreUsuario;

    // --- TRADUCTORES ---

    public static MultaDTO fromModel(Multa multa) {
        if (multa == null) return null;
        MultaDTO dto = new MultaDTO();
        dto.setId(multa.getId());
        dto.setPrestamoId(multa.getPrestamoId());
        dto.setUsuarioId(multa.getUsuarioId());
        dto.setMonto(multa.getMonto());
        dto.setDiasRetraso(multa.getDiasRetraso());
        dto.setEstadoMulta(multa.getEstadoMulta());
        dto.setFechaEmision(multa.getFechaEmision());
        dto.setFechaPago(multa.getFechaPago());
        return dto;
    }

    public Multa toModel() {
        Multa multa = new Multa();
        multa.setId(this.id);
        multa.setPrestamoId(this.prestamoId);
        multa.setUsuarioId(this.usuarioId);
        multa.setMonto(this.monto);
        multa.setDiasRetraso(this.diasRetraso);
        multa.setEstadoMulta(this.estadoMulta);
        multa.setFechaEmision(this.fechaEmision);
        multa.setFechaPago(this.fechaPago);
        return multa;
    }
}
