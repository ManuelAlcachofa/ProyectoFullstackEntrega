package com.biblioteca.usuario_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.biblioteca.usuario_service.dto.LoginDTO;
import com.biblioteca.usuario_service.dto.RegistroDTO;
import com.biblioteca.usuario_service.dto.UsuarioDTO;
import com.biblioteca.usuario_service.model.Rol;
import com.biblioteca.usuario_service.service.UsuarioService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Usuarios", description = "Registro, autenticación y administración de usuarios de la biblioteca.")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private EntityModel<UsuarioDTO> toModel(UsuarioDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).buscarPorId(dto.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"),
                linkTo(methodOn(UsuarioController.class).actualizar(dto.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(UsuarioController.class).eliminar(dto.getId())).withRel("eliminar"));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida correo y contraseña y devuelve los datos públicos del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Correo o contraseña incorrectos")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales de acceso.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"correo\":\"ana.admin@biblioteca.cl\",\"password\":\"admin123\"}"))) @RequestBody LoginDTO dto) {
        return usuarioService.login(dto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas"));
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Crea un usuario nuevo y cifra su contraseña."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "409", description = "Correo duplicado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Ya existe un usuario registrado con ese correo")))
    })
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de registro del usuario.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Ana Biblioteca\",\"correo\":\"ana.admin@biblioteca.cl\",\"password\":\"admin123\",\"rol\":\"ADMINISTRADOR\"}"))) @RequestBody RegistroDTO dto) {
        try {
            UsuarioDTO creado = usuarioService.registrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toModel(creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene todos los usuarios registrados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioDTO.class))))
    })
    @GetMapping
    public CollectionModel<EntityModel<UsuarioDTO>> listar() {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.listar().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene un usuario mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un usuario con la ID indicada")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> buscarPorId(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Buscar usuario por correo",
            description = "Obtiene un usuario mediante su correo electrónico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un usuario con el correo indicado")))
    })
    @GetMapping("/correo/{correo}")
    public ResponseEntity<EntityModel<UsuarioDTO>> buscarPorCorreo(@Parameter(description = "Correo electrónico del usuario", example = "ana.admin@biblioteca.cl") @PathVariable String correo) {
        return usuarioService.buscarPorCorreo(correo)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar usuarios por rol",
            description = "Filtra usuarios por ADMINISTRADOR, CLIENTE u OPERADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioDTO.class))))
    })
    @GetMapping("/rol/{rol}")
    public CollectionModel<EntityModel<UsuarioDTO>> listarPorRol(@Parameter(description = "Rol utilizado para filtrar usuarios", example = "CLIENTE") @PathVariable Rol rol) {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.listarPorRol(rol).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listarPorRol(rol)).withSelfRel());
    }

    @Operation(
            summary = "Verificar existencia de usuario",
            description = "Indica si existe un usuario con la ID solicitada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class), examples = @ExampleObject(value = "true")))
    })
    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existe(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.existePorId(id));
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Modifica los datos públicos de un usuario existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un usuario con la ID indicada")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> actualizar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos públicos del usuario.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class), examples = @ExampleObject(name = "Ejemplo válido", value = "{\"nombre\":\"Ana Biblioteca\",\"correo\":\"ana.admin@biblioteca.cl\",\"rol\":\"ADMINISTRADOR\"}"))) @RequestBody UsuarioDTO dto) {
        return usuarioService.actualizar(id, dto)
                .map(this::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "No existe un usuario con la ID indicada")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador único del recurso", example = "1") @PathVariable Long id) {
        return usuarioService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
