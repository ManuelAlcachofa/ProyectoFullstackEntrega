package com.example.inventario.InventarioService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.inventario.InventarioService.dto.InventarioDTO;
import com.example.inventario.InventarioService.service.InventarioService;
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

@Tag(name = "Inventario", description = "Control de stock, préstamos y devoluciones de ejemplares.")
@RestController
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    private EntityModel<InventarioDTO> toModel(InventarioDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(InventarioController.class).obtenerPorLibro(dto.getLibroId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listar()).withRel("inventario"),
                linkTo(methodOn(InventarioController.class).prestar(dto.getLibroId())).withRel("prestar"),
                linkTo(methodOn(InventarioController.class).devolver(dto.getLibroId())).withRel("devolver"));
    }

    @Operation(
            summary = "Listar inventario",
            description = "Obtiene el inventario completo con stock total, prestado y disponible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario obtenido correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InventarioDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<InventarioDTO>> listar() {
        List<EntityModel<InventarioDTO>> items = inventarioService.obtenerTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(items,
                linkTo(methodOn(InventarioController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Consultar inventario por libro",
            description = "Obtiene el estado de inventario asociado a un libro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe inventario para el libro indicado")))
    })
    @GetMapping("/libro/{libroId}")
    public ResponseEntity<?> obtenerPorLibro(@Parameter(description = "Identificador del libro", example = "1") @PathVariable Long libroId) {
        try {
            InventarioDTO dto = inventarioService.obtenerPorLibroId(libroId);
            return ResponseEntity.ok(toModel(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Crear registro de inventario",
            description = "Registra el stock inicial de un libro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventario creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El inventario ya existe o los datos son inválidos")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del inventario.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"libroId\":1,\"stockTotal\":5,\"librosPrestados\":0}"))) @Valid @RequestBody InventarioDTO dto) {
        try {
            InventarioDTO nuevo = inventarioService.guardar(dto);
            return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ENDPOINT DE NEGOCIO: PRESTAR
    @Operation(
            summary = "Descontar ejemplar por préstamo",
            description = "Aumenta la cantidad de libros prestados si existe disponibilidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Préstamo aplicado al inventario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No quedan ejemplares disponibles"))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe inventario para el libro indicado")))
    })
    @PutMapping("/prestar/{libroId}")
    public ResponseEntity<?> prestar(@Parameter(description = "Identificador del libro", example = "1") @PathVariable Long libroId) {
        try {
            InventarioDTO actualizado = inventarioService.prestarLibro(libroId);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    // ENDPOINT DE NEGOCIO: DEVOLVER
    @Operation(
            summary = "Reintegrar ejemplar por devolución",
            description = "Disminuye la cantidad de libros prestados al registrar una devolución."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución aplicada al inventario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InventarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existen ejemplares prestados para devolver"))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe inventario para el libro indicado")))
    })
    @PutMapping("/devolver/{libroId}")
    public ResponseEntity<?> devolver(@Parameter(description = "Identificador del libro", example = "1") @PathVariable Long libroId) {
        try {
            InventarioDTO actualizado = inventarioService.devolverLibro(libroId);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }
}
