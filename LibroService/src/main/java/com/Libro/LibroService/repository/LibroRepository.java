package com.Libro.LibroService.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Libro.LibroService.model.Libro;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    // Método mágico automático de Spring: Busca libros por el ID del autor
    List<Libro> findByAutorId(Long autorId);
    
    // Método mágico automático: Busca libros por el ID de la categoría
    List<Libro> findByCategoriaId(Long categoriaId);
}
