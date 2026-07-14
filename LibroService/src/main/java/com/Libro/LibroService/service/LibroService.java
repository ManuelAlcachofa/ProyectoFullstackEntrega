package com.Libro.LibroService.service;

import com.Libro.LibroService.dto.AutorResponseDTO;
import com.Libro.LibroService.dto.CategoriaResponseDTO;
import com.Libro.LibroService.dto.LibroDTO;
import com.Libro.LibroService.model.Libro;
import com.Libro.LibroService.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibroService {
    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.autor.url}")
    private String autorServiceUrl;

    @Value("${services.categoria.url}")
    private String categoriaServiceUrl;

    // 1. OBTENER TODOS LOS LIBROS
    public List<LibroDTO> listarTodos() {
        List<Libro> libros = libroRepository.findAll();
        
        return libros.stream().map(libro -> {
            LibroDTO dto = LibroDTO.fromModel(libro);
            
            this.inyectarAutorEnDto(libro.getAutorId(), dto);
            this.inyectarCategoriaEnDto(libro.getCategoriaId(), dto);
            
            return dto;
        }).collect(Collectors.toList());
    }

    // 2. OBTENER UN LIBRO POR ID
    public LibroDTO obtenerPorId(Long id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isPresent()) {
            Libro libro = libroOpt.get();
            LibroDTO dto = LibroDTO.fromModel(libro);
            
            this.inyectarAutorEnDto(libro.getAutorId(), dto);
            this.inyectarCategoriaEnDto(libro.getCategoriaId(), dto);
            
            return dto;
        }
        return null;
    }

    // 3. GUARDAR / CREAR UN NUEVO LIBRO
    public LibroDTO guardar(LibroDTO libroDto) {
        Libro libro = libroDto.toModel();
        Libro nuevoLibro = libroRepository.save(libro);
        return LibroDTO.fromModel(nuevoLibro);
    }

    // 4. ACTUALIZAR LIBRO
    public LibroDTO actualizar(Long id, LibroDTO libroDto) {
        return libroRepository.findById(id).map(libroExistente -> {
            libroExistente.setIsbn(libroDto.getIsbn());
            libroExistente.setTitulo(libroDto.getTitulo());
            libroExistente.setFechaPublicacion(libroDto.getFechaPublicacion());
            libroExistente.setSinopsis(libroDto.getSinopsis());
            libroExistente.setAutorId(libroDto.getAutorId());
            libroExistente.setCategoriaId(libroDto.getCategoriaId());
            
            Libro libroActualizado = libroRepository.save(libroExistente);
            return LibroDTO.fromModel(libroActualizado);
        }).orElse(null);
    }

    // 5. ELIMINAR UN LIBRO
    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }
    
    // 6. BUSCAR LIBROS POR AUTOR
    public List<LibroDTO> buscarPorAutor(Long autorId) {
        return libroRepository.findByAutorId(autorId)
                .stream()
                .map(libro -> {
                    LibroDTO dto = LibroDTO.fromModel(libro);
                    this.inyectarAutorEnDto(autorId, dto);
                    this.inyectarCategoriaEnDto(libro.getCategoriaId(), dto); 
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Método auxiliar para Autores
    private void inyectarAutorEnDto(Long autorId, LibroDTO dto) {
        String nombre = "Autor no disponible";
        String apellido = "";
        try {
            String urlAutorService = autorServiceUrl + "/autores/" + autorId;
            AutorResponseDTO autor = restTemplate.getForObject(urlAutorService, AutorResponseDTO.class);
            
            if (autor != null) {
                nombre = autor.getNombre();
                apellido = autor.getApellido();
            }
        } catch (Exception e) {
            nombre = "Error al conectar con Autores";
            apellido = "";
        }
        dto.setNombreAutor(nombre);
        dto.setApellidoAutor(apellido);
    }

    // Método auxiliar para Categorías
    private void inyectarCategoriaEnDto(Long categoriaId, LibroDTO dto) {
        String nombreCat = "Categoría no disponible";
        try {
            String urlCategoriaService = categoriaServiceUrl + "/categorias/" + categoriaId;
            CategoriaResponseDTO categoria = restTemplate.getForObject(urlCategoriaService, CategoriaResponseDTO.class);
            
            if (categoria != null) {
                nombreCat = categoria.getNombre();
            }
        } catch (Exception e) {
            nombreCat = "Error al conectar con Categorías";
        }
        dto.setNombreCategoria(nombreCat); 
    }
}