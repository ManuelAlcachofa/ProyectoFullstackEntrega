package com.example.reserva_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.reserva_service.dto.ReservaDTO;
import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.service.ReservaService;
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

@Tag(name = "Reservas", description = "Gestión del ciclo de reserva y disponibilidad de libros.")
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    private EntityModel<ReservaDTO> toModel(ReservaDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ReservaController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(ReservaController.class).listar()).withRel("reservas"),
                linkTo(methodOn(ReservaController.class).completar(dto.getId())).withRel("completar"),
                linkTo(methodOn(ReservaController.class).cancelar(dto.getId())).withRel("cancelar"),
                linkTo(methodOn(ReservaController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Listar reservas",
            description = "Obtiene todas las reservas registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<ReservaDTO>> listar() {
        List<EntityModel<ReservaDTO>> reservas = reservaService.listarTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(reservas,
                linkTo(methodOn(ReservaController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Buscar reserva por ID",
            description = "Obtiene una reserva mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reserva con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ReservaDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        ReservaDTO dto = reservaService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Listar reservas por usuario",
            description = "Obtiene las reservas asociadas a un usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))))
    })
    @GetMapping("/usuario/{usuarioId}")
    public CollectionModel<EntityModel<ReservaDTO>> listarPorUsuario(@Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long usuarioId) {
        List<EntityModel<ReservaDTO>> reservas = reservaService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(reservas,
                linkTo(methodOn(ReservaController.class).listarPorUsuario(usuarioId)).withSelfRel());
    }

    @Operation(
            summary = "Listar reservas por estado",
            description = "Filtra reservas por su estado actual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))))
    })
    @GetMapping("/estado/{estado}")
    public CollectionModel<EntityModel<ReservaDTO>> listarPorEstado(@Parameter(description = "Estado utilizado para filtrar resultados", example = "ACTIVO") @PathVariable EstadoReserva estado) {
        List<EntityModel<ReservaDTO>> reservas = reservaService.listarPorEstado(estado).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(reservas,
                linkTo(methodOn(ReservaController.class).listarPorEstado(estado)).withSelfRel());
    }

    @Operation(
            summary = "Crear reserva",
            description = "Registra una reserva validando usuario, libro e inventario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos son inválidos o la reserva no puede crearse"))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El usuario o el libro no existen")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la reserva.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"libroId\":1,\"usuarioId\":1}"))) @Valid @RequestBody ReservaDTO dto) {
        try {
            return new ResponseEntity<>(toModel(reservaService.guardar(dto)), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Marcar reserva disponible",
            description = "Indica que el ejemplar reservado está disponible para retiro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva marcada como disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La transición de estado no está permitida"))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reserva con la ID indicada")))
    })
    @PutMapping("/{id}/disponible")
    public ResponseEntity<?> marcarDisponible(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(toModel(reservaService.marcarDisponible(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Completar reserva",
            description = "Marca una reserva disponible como completada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva completada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La reserva no puede completarse"))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reserva con la ID indicada")))
    })
    @PutMapping("/{id}/completar")
    public ResponseEntity<?> completar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(toModel(reservaService.completar(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Cancelar reserva",
            description = "Cancela una reserva que todavía se encuentra vigente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Operación no permitida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "La reserva no puede cancelarse"))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reserva con la ID indicada")))
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(toModel(reservaService.cancelar(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Eliminar reserva",
            description = "Elimina una reserva mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una reserva con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return reservaService.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
