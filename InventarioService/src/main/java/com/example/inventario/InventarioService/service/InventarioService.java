package com.example.inventario.InventarioService.service;

import com.example.inventario.InventarioService.dto.InventarioDTO;
import com.example.inventario.InventarioService.model.Inventario;
import com.example.inventario.InventarioService.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.libro.url}")
    private String libroServiceUrl;

    // 1. OBTENER TODO EL INVENTARIO
    public List<InventarioDTO> obtenerTodos() {
        List<Inventario> inventarios = inventarioRepository.findAll();
        return inventarios.stream().map(inventario -> {
            InventarioDTO dto = InventarioDTO.fromModel(inventario);
            this.inyectarTituloEnDto(inventario.getLibroId(), dto);
            return dto;
        }).collect(Collectors.toList());
    }

    // 2. OBTENER POR LIBRO ID
    public InventarioDTO obtenerPorLibroId(Long libroId) {
        Inventario inventario = inventarioRepository.findByLibroId(libroId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado en inventario"));

        InventarioDTO dto = InventarioDTO.fromModel(inventario);
        this.inyectarTituloEnDto(libroId, dto);
        return dto;
    }

    // 3. GUARDAR / CREAR REGISTRO DE INVENTARIO
    public InventarioDTO guardar(InventarioDTO dto) {
        Optional<Inventario> existente = inventarioRepository.findByLibroId(dto.getLibroId());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un registro de inventario para el libro con ID: " + dto.getLibroId());
        }
        
        Inventario inventario = dto.toModel();
        Inventario nuevo = inventarioRepository.save(inventario);
        
        InventarioDTO nuevoDto = InventarioDTO.fromModel(nuevo);
        this.inyectarTituloEnDto(nuevo.getLibroId(), nuevoDto);
        return nuevoDto;
    }

    // 4. OPERACIÓN DE NEGOCIO: REGISTRAR UN PRÉSTAMO
    public InventarioDTO prestarLibro(Long libroId) {
        Inventario inventario = inventarioRepository.findByLibroId(libroId)
                .orElseThrow(() -> new RuntimeException("No se encontró inventario para el libro ID: " + libroId));

        if (inventario.getLibrosPrestados() >= inventario.getStockTotal()) {
            throw new RuntimeException("¡No quedan copias disponibles en este momento! Stock agotado.");
        }

        inventario.setLibrosPrestados(inventario.getLibrosPrestados() + 1);
        Inventario actualizado = inventarioRepository.save(inventario);
        
        InventarioDTO dto = InventarioDTO.fromModel(actualizado);
        this.inyectarTituloEnDto(libroId, dto); 
        return dto;
    }

    // 5. OPERACIÓN DE NEGOCIO: REGISTRAR UNA DEVOLUCIÓN
    public InventarioDTO devolverLibro(Long libroId) {
        Inventario inventario = inventarioRepository.findByLibroId(libroId)
                .orElseThrow(() -> new RuntimeException("No se encontró inventario para el libro ID: " + libroId));

        if (inventario.getLibrosPrestados() <= 0) {
            throw new RuntimeException("Error: No hay registros de que este libro haya sido prestado.");
        }

        inventario.setLibrosPrestados(inventario.getLibrosPrestados() - 1);
        Inventario actualizado = inventarioRepository.save(inventario);
        
        InventarioDTO dto = InventarioDTO.fromModel(actualizado);
        this.inyectarTituloEnDto(libroId, dto); 
        return dto;
    }

    private void inyectarTituloEnDto(Long libroId, InventarioDTO dto) {
        String titulo = "Título no disponible";
        try {
            String urlLibroService = libroServiceUrl + "/libros/" + libroId;
            Map<String, Object> libroResponse = restTemplate.getForObject(urlLibroService, Map.class);
            
            if (libroResponse != null && libroResponse.containsKey("titulo")) {
                titulo = (String) libroResponse.get("titulo");
            } else {
                titulo = "Título no encontrado";
            }
        } catch (Exception e) {
            titulo = "Servicio de Libros no disponible";
        }
        dto.setTituloLibro(titulo);
    }
}