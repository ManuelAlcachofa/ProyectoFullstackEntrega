package com.example_cat.CategoriaService.service;

import com.example_cat.CategoriaService.dto.CategoriaDTO;
import com.example_cat.CategoriaService.model.Categoria;
import com.example_cat.CategoriaService.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // 1. OBTENER TODAS
    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaDTO::fromModel)
                .collect(Collectors.toList());
    }

    // 2. OBTENER POR ID
    public CategoriaDTO obtenerPorId(Long id) {
        Optional<Categoria> catOpt = categoriaRepository.findById(id);
        return catOpt.map(CategoriaDTO::fromModel).orElse(null);
    }

    // 3. GUARDAR (Con regla de negocio: no duplicar nombres)
    public CategoriaDTO guardar(CategoriaDTO dto) {
        // Regla de negocio: Si ya existe una categoría con ese nombre, no la crea
        Optional<Categoria> existente = categoriaRepository.findByNombre(dto.getNombre());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una categoría registrada con el nombre: " + dto.getNombre());
        }

        Categoria categoria = dto.toModel();
        Categoria nueva = categoriaRepository.save(categoria);
        return CategoriaDTO.fromModel(nueva);
    }

    // 4. ACTUALIZAR
    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        return categoriaRepository.findById(id).map(categoriaExistente -> {
            categoriaExistente.setNombre(dto.getNombre());
            categoriaExistente.setDescripcion(dto.getDescripcion());
            
            Categoria actualizada = categoriaRepository.save(categoriaExistente);
            return CategoriaDTO.fromModel(actualizada);
        }).orElse(null);
    }

    // 5. ELIMINAR
    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }

    // 6. OPERACIÓN PERSONALIZADA (Buscador dinámico)
    public List<CategoriaDTO> buscarPorNombre(String palabra) {
        return categoriaRepository.findByNombreContainingIgnoreCase(palabra)
                .stream()
                .map(CategoriaDTO::fromModel)
                .collect(Collectors.toList());
    }
}