package com.example.multa.MultaService.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de una multa.", enumAsRef = true)
public enum EstadoMulta {
    PENDIENTE,
    PAGADA,
    ANULADA
}
