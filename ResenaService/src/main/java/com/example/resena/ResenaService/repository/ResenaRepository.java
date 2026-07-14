package com.example.resena.ResenaService.repository;

import com.example.resena.ResenaService.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByLibroId(Long libroId);

    List<Resena> findByUsuarioId(Long usuarioId);

    Optional<Resena> findByLibroIdAndUsuarioId(Long libroId, Long usuarioId);
}
