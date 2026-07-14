package com.example_cat.CategoriaService.repository;

import com.example_cat.CategoriaService.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    // Consulta personalizada automática: Busca por el nombre exacto
    Optional<Categoria> findByNombre(String nombre);

    // Operación personalizada extra: Busca categorías que contengan una palabra (ej: "Fic" -> Ciencia Ficción)
    List<Categoria> findByNombreContainingIgnoreCase(String palabra);
}