package com.example.resena.ResenaService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.resena.ResenaService.dto.ResenaDTO;
import com.example.resena.ResenaService.service.ResenaService;
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

@Tag(name = "Reseñas", description = "Gestión de opiniones y calificaciones de libros.")
@RestController
@RequestMapping("/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    private EntityModel<ResenaDTO> toModel(ResenaDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ResenaController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(ResenaController.class).listar()).withRel("resenas"),
                linkTo(methodOn(ResenaController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(ResenaController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    // 1. Listar todas las reseñas
    @Operation(
            summary = "Listar reseñas",
            description = "Obtiene todas las reseñas registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResenaDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<ResenaDTO>> listar() {
        List<EntityModel<ResenaDTO>> resenas = resenaService.listarTodas().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resenas,
                linkTo(methodOn(ResenaController.class).listar()).withSelfRel());
    }

    // 2. Obtener una reseña por su ID
    @Operation(
            summary = "Buscar reseña por ID",
            description = "Obtiene una reseña mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reseña con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ResenaDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        ResenaDTO dto = resenaService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    // 3. Crear una nueva reseña (Con filtro invisible @Valid)
    @Operation(
            summary = "Crear reseña",
            description = "Registra una calificación y comentario asociados a un libro y usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La calificación o el comentario son inválidos"))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El libro o el usuario no existen")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la reseña.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"libroId\":1,\"usuarioId\":1,\"calificacion\":5,\"comentario\":\"Una novela inolvidable.\"}"))) @Valid @RequestBody ResenaDTO dto) {
        try {
            ResenaDTO nueva = resenaService.guardar(dto);
            return new ResponseEntity<>(toModel(nueva), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Actualizar una reseña existente
    @Operation(
            summary = "Actualizar reseña",
            description = "Modifica la calificación o comentario de una reseña."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reseña con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ResenaDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la reseña.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"libroId\":1,\"usuarioId\":1,\"calificacion\":5,\"comentario\":\"Una novela inolvidable.\"}"))) @Valid @RequestBody ResenaDTO dto) {
        ResenaDTO actualizada = resenaService.actualizar(id, dto);
        return actualizada != null ? ResponseEntity.ok(toModel(actualizada)) : ResponseEntity.notFound().build();
    }

    // 5. Eliminar una reseña
    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reseña eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reseña con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Listar reseñas de un libro específico
    @Operation(
            summary = "Listar reseñas por libro",
            description = "Obtiene todas las reseñas asociadas a un libro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResenaDTO.class))))
    })
    @GetMapping("/libro/{libroId}")
    public CollectionModel<EntityModel<ResenaDTO>> listarPorLibro(@Parameter(description = "Identificador del libro", example = "1") @PathVariable Long libroId) {
        List<EntityModel<ResenaDTO>> resenas = resenaService.listarPorLibro(libroId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resenas,
                linkTo(methodOn(ResenaController.class).listarPorLibro(libroId)).withSelfRel());
    }

    // 7. Listar reseñas hechas por un usuario específico
    @Operation(
            summary = "Listar reseñas por usuario",
            description = "Obtiene todas las reseñas realizadas por un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResenaDTO.class))))
    })
    @GetMapping("/usuario/{usuarioId}")
    public CollectionModel<EntityModel<ResenaDTO>> listarPorUsuario(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long usuarioId) {
        List<EntityModel<ResenaDTO>> resenas = resenaService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resenas,
                linkTo(methodOn(ResenaController.class).listarPorUsuario(usuarioId)).withSelfRel());
    }

    // 8. Obtener el promedio de calificación de un libro
    @Operation(
            summary = "Obtener promedio de un libro",
            description = "Calcula el promedio de calificaciones registradas para un libro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promedio calculado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Double.class), examples = @ExampleObject(value = "4.5")))
    })
    @GetMapping("/libro/{libroId}/promedio")
    public ResponseEntity<Double> promedioPorLibro(@Parameter(description = "Identificador del libro", example = "1") @PathVariable Long libroId) {
        return ResponseEntity.ok(resenaService.calcularPromedioPorLibro(libroId));
    }
}
