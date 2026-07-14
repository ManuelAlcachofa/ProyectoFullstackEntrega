package com.example.multa.MultaService.repository;

import com.example.multa.MultaService.model.EstadoMulta;
import com.example.multa.MultaService.model.Multa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {

    List<Multa> findByUsuarioId(Long usuarioId);

    List<Multa> findByEstadoMulta(EstadoMulta estadoMulta);

    List<Multa> findByUsuarioIdAndEstadoMulta(Long usuarioId, EstadoMulta estadoMulta);

    List<Multa> findByPrestamoId(Long prestamoId);
}
