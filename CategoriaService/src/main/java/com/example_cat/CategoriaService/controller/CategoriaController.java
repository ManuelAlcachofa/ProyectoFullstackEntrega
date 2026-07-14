package com.example_cat.CategoriaService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example_cat.CategoriaService.dto.CategoriaDTO;
import com.example_cat.CategoriaService.service.CategoriaService;
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

@Tag(name = "Categorías", description = "Gestión de categorías utilizadas para clasificar los libros.")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    private EntityModel<CategoriaDTO> toModel(CategoriaDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(CategoriaController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listar()).withRel("categorias"),
                linkTo(methodOn(CategoriaController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(CategoriaController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    // 1. Listar todas
    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías registradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorías obtenidas correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CategoriaDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<CategoriaDTO>> listar() {
        List<EntityModel<CategoriaDTO>> categorias = categoriaService.listarTodas().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(categorias,
                linkTo(methodOn(CategoriaController.class).listar()).withSelfRel());
    }

    // 2. Obtener por ID
    @Operation(
            summary = "Buscar categoría por ID",
            description = "Obtiene una categoría mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una categoría con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CategoriaDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        CategoriaDTO dto = categoriaService.obtenerPorId(id);
        return dto != null ? ResponseEntity.ok(toModel(dto)) : ResponseEntity.notFound().build();
    }

    // 3. Crear categoría (Con filtro invisible @Valid)
    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría y evita nombres duplicados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El nombre ya existe o los datos son inválidos")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Novela\",\"descripcion\":\"Obras narrativas de ficción extensa.\"}"))) @Valid @RequestBody CategoriaDTO dto) {
        try {
            CategoriaDTO nueva = categoriaService.guardar(dto);
            return new ResponseEntity<>(toModel(nueva), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Si viola la regla de negocio del nombre duplicado, devuelve un error 400 limpio
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Actualizar categoría existente
    @Operation(
            summary = "Actualizar categoría",
            description = "Modifica una categoría existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una categoría con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CategoriaDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Novela\",\"descripcion\":\"Obras narrativas de ficción extensa.\"}"))) @Valid @RequestBody CategoriaDTO dto) {
        CategoriaDTO actualizada = categoriaService.actualizar(id, dto);
        return actualizada != null ? ResponseEntity.ok(toModel(actualizada)) : ResponseEntity.notFound().build();
    }

    // 5. Eliminar categoría
    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe una categoría con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Endpoint de Operación Personalizada (Buscador)
    @Operation(
            summary = "Buscar categorías",
            description = "Busca categorías cuyo nombre contiene la palabra indicada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CategoriaDTO.class))))
    })
    @GetMapping("/buscar")
    public CollectionModel<EntityModel<CategoriaDTO>> buscar(@Parameter(description = "Palabra contenida en el nombre de la categoría", example = "Novela") @RequestParam String palabra) {
        List<EntityModel<CategoriaDTO>> resultados = categoriaService.buscarPorNombre(palabra).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(resultados,
                linkTo(methodOn(CategoriaController.class).buscar(palabra)).withSelfRel());
    }
}
