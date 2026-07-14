package com.Libro.LibroService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.Libro.LibroService.dto.LibroDTO;
import com.Libro.LibroService.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Libros", description = "Gestión del catálogo de libros y consulta de datos remotos de autores y categorías.")
@RestController
@RequestMapping("/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.autor.url}")
    private String autorServiceUrl;

    private EntityModel<LibroDTO> toModel(LibroDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(LibroController.class).obtenerPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(LibroController.class).listar()).withRel("libros"),
                linkTo(methodOn(LibroController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(LibroController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Consultar autor remoto",
            description = "Consume AutorService y devuelve los datos del autor indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor remoto obtenido correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un autor con la ID indicada"))),
            @ApiResponse(responseCode = "503", description = "Servicio no disponible",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No fue posible consultar AutorService")))
    })
    @GetMapping("/autores/{id}")
    public ResponseEntity<Object> obtenerAutorDesdeLibro(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        try {
            String url = autorServiceUrl + "/autores/" + id;
            Object autor = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok(autor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("El microservicio de Autores no está respondiendo en este momento.");
        }
    }

    // 1. Obtener todos los libros
    @Operation(
            summary = "Listar libros",
            description = "Obtiene todos los libros con información enriquecida de autor y categoría."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libros obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LibroDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<LibroDTO>> listar() {
        List<EntityModel<LibroDTO>> libros = libroService.listarTodos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(libros,
                linkTo(methodOn(LibroController.class).listar()).withSelfRel());
    }

    // 2. Obtener un libro por su ID
    @Operation(
            summary = "Buscar libro por ID",
            description = "Obtiene un libro mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un libro con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> obtenerPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        LibroDTO libro = libroService.obtenerPorId(id);
        return libro != null ? ResponseEntity.ok(toModel(libro)) : ResponseEntity.notFound().build();
    }

    // 3. Crear un nuevo libro
    @Operation(
            summary = "Crear libro",
            description = "Registra un libro y valida que el autor y la categoría existan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Libro creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos del libro no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Recurso relacionado no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "El autor o la categoría no existen")))
    })
    @PostMapping
    public ResponseEntity<EntityModel<LibroDTO>> crear(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del libro.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"isbn\":\"9780307474728\",\"titulo\":\"Cien años de soledad\",\"fechaPublicacion\":\"1967-06-05\",\"sinopsis\":\"Historia de la familia Buendía.\",\"categoriaId\":1,\"autorId\":1}"))) @Valid @RequestBody LibroDTO libroDto) {
        LibroDTO nuevo = libroService.guardar(libroDto);
        return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
    }

    // 4. Actualizar un libro existente (¡El nuevo integrante!)
    @Operation(
            summary = "Actualizar libro",
            description = "Modifica los datos de un libro existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Los datos del libro no cumplen las validaciones"))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un libro con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del libro.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"isbn\":\"9780307474728\",\"titulo\":\"Cien años de soledad\",\"fechaPublicacion\":\"1967-06-05\",\"sinopsis\":\"Historia de la familia Buendía.\",\"categoriaId\":1,\"autorId\":1}"))) @Valid @RequestBody LibroDTO libroDto) {
        // El @Valid aquí frena la petición si al editar mandan datos que rompan las reglas
        LibroDTO actualizado = libroService.actualizar(id, libroDto);
        return actualizado != null ? ResponseEntity.ok(toModel(actualizado)) : ResponseEntity.notFound().build();
    }

    // 5. Eliminar un libro
    @Operation(
            summary = "Eliminar libro",
            description = "Elimina un libro mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Libro eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un libro con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Filtrar libros por un autor específico (Endpoint extra)
    @Operation(
            summary = "Listar libros por autor",
            description = "Obtiene todos los libros asociados al autor indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libros obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LibroDTO.class))))
    })
    @GetMapping("/autor/{autorId}")
    public CollectionModel<EntityModel<LibroDTO>> buscarPorAutor(@Parameter(description = "Identificador del autor", example = "1") @PathVariable Long autorId) {
        List<EntityModel<LibroDTO>> libros = libroService.buscarPorAutor(autorId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(libros,
                linkTo(methodOn(LibroController.class).buscarPorAutor(autorId)).withSelfRel());
    }
}
