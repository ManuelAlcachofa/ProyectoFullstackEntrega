package com.example.inventario.InventarioService.repository;

import com.example.inventario.InventarioService.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    
    // Operación personalizada: Buscar inventario por el ID del libro
    Optional<Inventario> findByLibroId(Long libroId);
}