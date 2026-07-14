package com.example.prestamo_service.repository;

import com.example.prestamo_service.model.EstadoPrestamo;
import com.example.prestamo_service.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByUsuarioId(Long usuarioId);
    List<Prestamo> findByLibroId(Long libroId);
    List<Prestamo> findByEstadoPrestamo(EstadoPrestamo estadoPrestamo);
    List<Prestamo> findByUsuarioIdAndEstadoPrestamo(Long usuarioId, EstadoPrestamo estadoPrestamo);
}
