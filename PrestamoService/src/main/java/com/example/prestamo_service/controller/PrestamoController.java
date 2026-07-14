package com.example.prestamo_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.prestamo_service.dto.PrestamoDTO;
import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.service.PrestamoService;
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

@Tag(name = "Préstamos", description = "Gestión del ciclo de préstamo y devolución de libros.")
@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    private EntityModel<PrestamoDTO> toModel(PrestamoDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(PrestamoController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(PrestamoController.class).listar()).withRel("prestamos"),
                linkTo(methodOn(PrestamoController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(PrestamoController.class).devolver(dto.getId())).withRel("devolver"),
                linkTo(methodOn(PrestamoController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Listar préstamos",
            description = "Obtiene todos los préstamos registrados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamos obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PrestamoDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<PrestamoDTO>> listar() {
        List<EntityModel<PrestamoDTO>> prestamos = prestamoService.listarTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(prestamos,
                linkTo(methodOn(PrestamoController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Buscar préstamo por ID",
            description = "Obtiene un préstamo mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un préstamo con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PrestamoDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        PrestamoDTO dto = prestamoService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Listar préstamos por usuario",
            description = "Obtiene los préstamos asociados a un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamos obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PrestamoDTO.class))))
    })
    @GetMapping("/usuario/{usuarioId}")
    public CollectionModel<EntityModel<PrestamoDTO>> listarPorUsuario(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long usuarioId) {
        List<EntityModel<PrestamoDTO>> prestamos = prestamoService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(prestamos,
                linkTo(methodOn(PrestamoController.class).listarPorUsuario(usuarioId)).withSelfRel());
    }

    @Operation(
            summary = "Listar préstamos por estado",
            description = "Filtra préstamos por ACTIVO, DEVUELTO o EN_MORA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamos obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PrestamoDTO.class))))
    })
    @GetMapping("/estado/{estado}")
    public CollectionModel<EntityModel<PrestamoDTO>> listarPorEstado(@Parameter(description = "Estado utilizado para filtrar resultados", example = "ACTIVO") @PathVariable EstadoPrestamo estado) {
        List<EntityModel<PrestamoDTO>> prestamos = prestamoService.listarPorEstado(estado).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(prestamos,
                linkTo(methodOn(PrestamoController.class).listarPorEstado(estado)).withSelfRel());
    }

    @Operation(
            summary = "Crear préstamo",
            description = "Registra un préstamo validando usuario, libro y disponibilidad de inventario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No hay stock o los datos son inválidos"))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El usuario o el libro no existen")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del préstamo.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"usuarioId\":1,\"libroId\":1}"))) @Valid @RequestBody PrestamoDTO dto) {
        try {
            PrestamoDTO nuevo = prestamoService.guardar(dto);
            return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Actualizar préstamo",
            description = "Modifica la información de un préstamo existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las reglas del negocio"))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un préstamo con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del préstamo.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"usuarioId\":1,\"libroId\":1}"))) @RequestBody PrestamoDTO dto) {
        PrestamoDTO actualizado = prestamoService.actualizar(id, dto);
        return actualizado != null ? ResponseEntity.ok(toModel(actualizado)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Devolver libro prestado",
            description = "Finaliza un préstamo activo y reintegra el ejemplar al inventario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo devuelto correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PrestamoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El préstamo ya fue devuelto"))),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un préstamo con la ID indicada")))
    })
    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(toModel(prestamoService.devolver(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Eliminar préstamo",
            description = "Elimina un préstamo mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Préstamo eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un préstamo con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return prestamoService.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
