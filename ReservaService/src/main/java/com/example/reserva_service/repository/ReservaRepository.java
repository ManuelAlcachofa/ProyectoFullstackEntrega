package com.example.reserva_service.repository;

import com.example.reserva_service.model.EstadoReserva;
import com.example.reserva_service.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioId(Long usuarioId);
    List<Reserva> findByLibroId(Long libroId);
    List<Reserva> findByEstadoReserva(EstadoReserva estadoReserva);
    List<Reserva> findByLibroIdAndEstadoReserva(Long libroId, EstadoReserva estadoReserva);
}
