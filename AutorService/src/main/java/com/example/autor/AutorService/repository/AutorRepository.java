package com.example.autor.AutorService.repository;

import com.example.autor.AutorService.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    List<Autor> findByNacionalidadIgnoreCase(String nacionalidad);

    List<Autor> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}