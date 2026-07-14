package com.example.multa.MultaService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.multa.MultaService.dto.MultaDTO;
import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.service.MultaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Multas", description = "Emisión, consulta, pago y anulación de multas por atraso.")
@RestController
@RequestMapping("/multas")
public class MultaController {

    @Autowired
    private MultaService multaService;

    private EntityModel<MultaDTO> toModel(MultaDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(MultaController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(MultaController.class).listar()).withRel("multas"),
                linkTo(methodOn(MultaController.class).pagar(dto.getId())).withRel("pagar"),
                linkTo(methodOn(MultaController.class).anular(dto.getId())).withRel("anular"),
                linkTo(methodOn(MultaController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    // 1. Listar todas las multas
    @Operation(
            summary = "Listar multas",
            description = "Obtiene todas las multas registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MultaDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<MultaDTO>> listar() {
        List<EntityModel<MultaDTO>> multas = multaService.listarTodas().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(multas,
                linkTo(methodOn(MultaController.class).listar()).withSelfRel());
    }

    // 2. Obtener una multa por su ID
    @Operation(
            summary = "Buscar multa por ID",
            description = "Obtiene una multa mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multa encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Multa no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una multa con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MultaDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        MultaDTO dto = multaService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    // 3. Emitir / crear una nueva multa
    @Operation(
            summary = "Emitir multa",
            description = "Registra una multa asociada a un préstamo y a un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Multa emitida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las reglas de emisión")))
    })
    @PostMapping
    public ResponseEntity<EntityModel<MultaDTO>> emitir(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la multa.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"prestamoId\":1,\"usuarioId\":1,\"monto\":3500.0,\"diasRetraso\":3}"))) @Valid @RequestBody MultaDTO dto) {
        MultaDTO nueva = multaService.emitir(dto);
        return new ResponseEntity<>(toModel(nueva), HttpStatus.CREATED);
    }

    // 4. Actualizar una multa existente
    @Operation(
            summary = "Actualizar multa",
            description = "Modifica la información editable de una multa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multa actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Multa no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una multa con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MultaDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la multa.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"prestamoId\":1,\"usuarioId\":1,\"monto\":3500.0,\"diasRetraso\":3}"))) @Valid @RequestBody MultaDTO dto) {
        MultaDTO actualizada = multaService.actualizar(id, dto);
        return actualizada != null ? ResponseEntity.ok(toModel(actualizada)) : ResponseEntity.notFound().build();
    }

    // 5. Eliminar una multa
    @Operation(
            summary = "Eliminar multa",
            description = "Elimina una multa mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Multa eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Multa no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una multa con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        multaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. ENDPOINT DE NEGOCIO: Registrar el pago de una multa
    @Operation(
            summary = "Pagar multa",
            description = "Cambia el estado de una multa pendiente a PAGADA y registra la fecha de pago."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multa pagada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La multa ya fue pagada o está anulada"))),
            @ApiResponse(responseCode = "404", description = "Multa no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una multa con la ID indicada")))
    })
    @PutMapping("/{id}/pagar")
    public ResponseEntity<?> pagar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            MultaDTO actualizada = multaService.pagar(id);
            return ResponseEntity.ok(toModel(actualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 7. ENDPOINT DE NEGOCIO: Anular una multa
    @Operation(
            summary = "Anular multa",
            description = "Cambia el estado de una multa pendiente a ANULADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multa anulada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MultaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La multa no puede ser anulada"))),
            @ApiResponse(responseCode = "404", description = "Multa no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una multa con la ID indicada")))
    })
    @PutMapping("/{id}/anular")
    public ResponseEntity<?> anular(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            MultaDTO actualizada = multaService.anular(id);
            return ResponseEntity.ok(toModel(actualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 8. Listar multas por usuario
    @Operation(
            summary = "Listar multas por usuario",
            description = "Obtiene todas las multas asociadas a un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MultaDTO.class))))
    })
    @GetMapping("/usuario/{usuarioId}")
    public CollectionModel<EntityModel<MultaDTO>> listarPorUsuario(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long usuarioId) {
        List<EntityModel<MultaDTO>> multas = multaService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(multas,
                linkTo(methodOn(MultaController.class).listarPorUsuario(usuarioId)).withSelfRel());
    }

    // 9. Listar multas por estado (PENDIENTE, PAGADA, ANULADA)
    @Operation(
            summary = "Listar multas por estado",
            description = "Filtra las multas por PENDIENTE, PAGADA o ANULADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Multas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MultaDTO.class))))
    })
    @GetMapping("/estado/{estado}")
    public CollectionModel<EntityModel<MultaDTO>> listarPorEstado(@Parameter(description = "Estado utilizado para filtrar resultados", example = "ACTIVO") @PathVariable EstadoMulta estado) {
        List<EntityModel<MultaDTO>> multas = multaService.listarPorEstado(estado).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(multas,
                linkTo(methodOn(MultaController.class).listarPorEstado(estado)).withSelfRel());
    }
}
