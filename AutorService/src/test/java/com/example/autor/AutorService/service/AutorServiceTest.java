package com.example.autor.AutorService.service;

import com.example.autor.AutorService.dto.AutorDTO;
import com.example.autor.AutorService.model.Autor;
import com.example.autor.AutorService.repository.AutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorServiceTest {

    @Mock
    private AutorRepository autorRepository;

    @InjectMocks
    private AutorService autorService;

    @Test
    void listarTodosDebeConvertirEntidadesADto() {
        Autor autor = crearAutor(1L, "Gabriel", "Garcia Marquez");
        when(autorRepository.findAll()).thenReturn(List.of(autor));

        List<AutorDTO> resultado = autorService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Gabriel", resultado.get(0).getNombre());
        assertEquals("Garcia Marquez", resultado.get(0).getApellido());
        verify(autorRepository).findAll();
    }

    @Test
    void guardarDebePersistirYRetornarDto() {
        AutorDTO dto = new AutorDTO(null, "Isabel", "Allende", "Chilena", "Escritora chilena");
        when(autorRepository.save(any(Autor.class))).thenAnswer(invocacion -> {
            Autor autor = invocacion.getArgument(0);
            autor.setId(2L);
            return autor;
        });

        AutorDTO resultado = autorService.guardar(dto);

        assertEquals(2L, resultado.getId());
        assertEquals("Isabel", resultado.getNombre());
        verify(autorRepository).save(any(Autor.class));
    }

    @Test
    void actualizarAutorExistenteDebeModificarSusDatos() {
        Autor existente = crearAutor(1L, "Nombre", "Anterior");
        AutorDTO cambios = new AutorDTO(null, "Pablo", "Neruda", "Chilena", "Poeta chileno");
        when(autorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(autorRepository.save(existente)).thenReturn(existente);

        AutorDTO resultado = autorService.actualizar(1L, cambios);

        assertNotNull(resultado);
        assertEquals("Pablo", resultado.getNombre());
        assertEquals("Neruda", resultado.getApellido());
        verify(autorRepository).save(existente);
    }

    @Test
    void actualizarAutorInexistenteDebeRetornarNull() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        AutorDTO resultado = autorService.actualizar(99L, new AutorDTO());

        assertNull(resultado);
        verify(autorRepository, never()).save(any());
    }

    @Test
    void buscarPorNombreOApellidoDebeUsarRepositorio() {
        when(autorRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("ner", "ner"))
                .thenReturn(List.of(crearAutor(1L, "Pablo", "Neruda")));

        List<AutorDTO> resultado = autorService.buscarPorNombreOApellido("ner");

        assertEquals(1, resultado.size());
        assertEquals("Neruda", resultado.get(0).getApellido());
    }

    private Autor crearAutor(Long id, String nombre, String apellido) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNombre(nombre);
        autor.setApellido(apellido);
        autor.setNacionalidad("Chilena");
        autor.setBiografia("Biografia de prueba");
        return autor;
    }
}
