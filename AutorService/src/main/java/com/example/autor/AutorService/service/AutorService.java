package com.example.autor.AutorService.service;

import com.example.autor.AutorService.dto.AutorDTO;
import com.example.autor.AutorService.model.Autor;
import com.example.autor.AutorService.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutorService {

    @Autowired
    private AutorRepository autorRepository;

    public List<AutorDTO> listarTodos() {
        return autorRepository.findAll().stream()
                .map(AutorDTO::fromModel)
                .collect(Collectors.toList());
    }

    public AutorDTO obtenerPorId(Long id) {
        return autorRepository.findById(id)
                .map(AutorDTO::fromModel)
                .orElse(null);
    }

    public AutorDTO guardar(AutorDTO dto) {
        Autor autor = dto.toModel();
        Autor nuevo = autorRepository.save(autor);
        return AutorDTO.fromModel(nuevo);
    }

    public AutorDTO actualizar(Long id, AutorDTO dto) {
        return autorRepository.findById(id).map(autorExistente -> {
            autorExistente.setNombre(dto.getNombre());
            autorExistente.setApellido(dto.getApellido());
            autorExistente.setNacionalidad(dto.getNacionalidad());
            autorExistente.setBiografia(dto.getBiografia());
            
            Autor actualizado = autorRepository.save(autorExistente);
            return AutorDTO.fromModel(actualizado);
        }).orElse(null);
    }

    public void eliminar(Long id) {
        autorRepository.deleteById(id);
    }

    public List<AutorDTO> buscarPorNombreOApellido(String criterio) {
    return autorRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(criterio, criterio)
            .stream()
            .map(AutorDTO::fromModel)
            .collect(Collectors.toList());
}
}
