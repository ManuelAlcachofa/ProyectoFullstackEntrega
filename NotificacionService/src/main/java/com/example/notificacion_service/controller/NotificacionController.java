package com.example.notificacion_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.notificacion_service.dto.NotificacionDTO;
import com.example.notificacion_service.model.TipoNotificacion;
import com.example.notificacion_service.service.NotificacionService;
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

@Tag(name = "Notificaciones", description = "Gestión de mensajes enviados a usuarios por eventos del sistema.")
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    private EntityModel<NotificacionDTO> toModel(NotificacionDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(NotificacionController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(NotificacionController.class).listar()).withRel("notificaciones"),
                linkTo(methodOn(NotificacionController.class).marcarLeida(dto.getId())).withRel("marcar-leida"),
                linkTo(methodOn(NotificacionController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Listar notificaciones",
            description = "Obtiene todas las notificaciones registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<NotificacionDTO>> listar() {
        List<EntityModel<NotificacionDTO>> notificaciones = notificacionService.listarTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(notificaciones,
                linkTo(methodOn(NotificacionController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Buscar notificación por ID",
            description = "Obtiene una notificación mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una notificación con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<NotificacionDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        NotificacionDTO dto = notificacionService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Listar notificaciones por usuario",
            description = "Obtiene las notificaciones asociadas a un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class))))
    })
    @GetMapping("/usuario/{usuarioId}")
    public CollectionModel<EntityModel<NotificacionDTO>> listarPorUsuario(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long usuarioId) {
        List<EntityModel<NotificacionDTO>> notificaciones = notificacionService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(notificaciones,
                linkTo(methodOn(NotificacionController.class).listarPorUsuario(usuarioId)).withSelfRel());
    }

    @Operation(
            summary = "Listar notificaciones por tipo",
            description = "Filtra notificaciones por su tipo funcional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class))))
    })
    @GetMapping("/tipo/{tipo}")
    public CollectionModel<EntityModel<NotificacionDTO>> listarPorTipo(@Parameter(description = "Tipo de notificación utilizado para filtrar", example = "BIENVENIDA") @PathVariable TipoNotificacion tipo) {
        List<EntityModel<NotificacionDTO>> notificaciones = notificacionService.listarPorTipo(tipo).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(notificaciones,
                linkTo(methodOn(NotificacionController.class).listarPorTipo(tipo)).withSelfRel());
    }

    @Operation(
            summary = "Listar notificaciones no leídas",
            description = "Obtiene todas las notificaciones que aún no han sido leídas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NotificacionDTO.class))))
    })
    @GetMapping("/no-leidas")
    public CollectionModel<EntityModel<NotificacionDTO>> listarNoLeidas() {
        List<EntityModel<NotificacionDTO>> notificaciones = notificacionService.listarNoLeidas().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(notificaciones,
                linkTo(methodOn(NotificacionController.class).listarNoLeidas()).withSelfRel());
    }

    @Operation(
            summary = "Crear notificación",
            description = "Registra una nueva notificación y enriquece los datos del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificación creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las validaciones")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la notificación.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"usuarioId\":1,\"prestamoId\":1,\"titulo\":\"Préstamo próximo a vencer\",\"mensaje\":\"Tu préstamo vence pronto.\",\"tipoNotificacion\":\"VENCIMIENTO\",\"leida\":false}"))) @Valid @RequestBody NotificacionDTO dto) {
        try {
            return new ResponseEntity<>(toModel(notificacionService.guardar(dto)), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Marcar notificación como leída",
            description = "Actualiza el indicador de lectura de una notificación."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La notificación no puede actualizarse"))),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una notificación con la ID indicada")))
    })
    @PutMapping("/{id}/leida")
    public ResponseEntity<?> marcarLeida(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(toModel(notificacionService.marcarLeida(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina una notificación mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una notificación con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return notificacionService.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
