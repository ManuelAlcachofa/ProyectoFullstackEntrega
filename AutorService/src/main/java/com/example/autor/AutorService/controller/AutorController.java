package com.example.autor.AutorService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.autor.AutorService.dto.AutorDTO;
import com.example.autor.AutorService.service.AutorService;
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

@Tag(name = "Autores", description = "Gestión del catálogo de autores de la biblioteca.")
@RestController
@RequestMapping("/autores")
public class AutorController {

    @Autowired
    private AutorService autorService;

    private EntityModel<AutorDTO> toModel(AutorDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(AutorController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(AutorController.class).listar()).withRel("autores"),
                linkTo(methodOn(AutorController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(AutorController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Listar autores",
            description = "Obtiene todos los autores registrados con enlaces HATEOAS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autores obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AutorDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<AutorDTO>> listar() {
        List<EntityModel<AutorDTO>> autores = autorService.listarTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(autores,
                linkTo(methodOn(AutorController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Buscar autor por ID",
            description = "Obtiene un autor mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un autor con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AutorDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        AutorDTO dto = autorService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Crear autor",
            description = "Registra un nuevo autor validando sus datos obligatorios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Autor creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos del autor no cumplen las validaciones")))
    })
    @PostMapping
    public ResponseEntity<EntityModel<AutorDTO>> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del autor.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = AutorDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Gabriel\",\"apellido\":\"García Márquez\",\"nacionalidad\":\"Colombiana\",\"biografia\":\"Escritor y periodista colombiano.\"}"))) @Valid @RequestBody AutorDTO dto) {
        AutorDTO nuevo = autorService.guardar(dto);
        return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar autor",
            description = "Modifica los datos de un autor existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos del autor no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un autor con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AutorDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del autor.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = AutorDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Gabriel\",\"apellido\":\"García Márquez\",\"nacionalidad\":\"Colombiana\",\"biografia\":\"Escritor y periodista colombiano.\"}"))) @Valid @RequestBody AutorDTO dto) {
        AutorDTO actualizado = autorService.actualizar(id, dto);
        return actualizado != null ? ResponseEntity.ok(toModel(actualizado)) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Eliminar autor",
            description = "Elimina un autor mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Autor eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un autor con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        autorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Buscar autores por criterio",
            description = "Busca autores cuyo nombre o apellido contiene el texto indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AutorDTO.class))))
    })
    @GetMapping("/buscar/por-criterio")
    public CollectionModel<EntityModel<AutorDTO>> buscarAutores(@Parameter(description = "Texto contenido en el nombre o apellido", example = "Gabriel") @RequestParam("texto") String texto) {
        List<EntityModel<AutorDTO>> resultados = autorService.buscarPorNombreOApellido(texto).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resultados,
                linkTo(methodOn(AutorController.class).buscarAutores(texto)).withSelfRel());
    }
}
