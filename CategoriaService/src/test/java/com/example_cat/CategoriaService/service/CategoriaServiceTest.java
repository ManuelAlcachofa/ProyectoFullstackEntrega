package com.example_cat.CategoriaService.service;

import com.example_cat.CategoriaService.dto.CategoriaDTO;
import com.example_cat.CategoriaService.model.Categoria;
import com.example_cat.CategoriaService.repository.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void guardarCategoriaNuevaDebePersistirla() {
        CategoriaDTO dto = new CategoriaDTO(null, "Novela", "Narrativa de ficcion");
        when(categoriaRepository.findByNombre("Novela")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocacion -> {
            Categoria categoria = invocacion.getArgument(0);
            categoria.setId(1L);
            return categoria;
        });

        CategoriaDTO resultado = categoriaService.guardar(dto);

        assertEquals(1L, resultado.getId());
        assertEquals("Novela", resultado.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void guardarCategoriaDuplicadaDebeLanzarExcepcion() {
        when(categoriaRepository.findByNombre("Novela"))
                .thenReturn(Optional.of(new Categoria(1L, "Novela", "Existente")));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> categoriaService.guardar(new CategoriaDTO(null, "Novela", "Otra")));

        assertTrue(excepcion.getMessage().contains("Ya existe una categoría"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizarCategoriaExistenteDebeGuardarCambios() {
        Categoria existente = new Categoria(1L, "Vieja", "Descripcion vieja");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(existente)).thenReturn(existente);

        CategoriaDTO resultado = categoriaService.actualizar(1L,
                new CategoriaDTO(null, "Historia", "Libros historicos"));

        assertNotNull(resultado);
        assertEquals("Historia", resultado.getNombre());
        assertEquals("Libros historicos", resultado.getDescripcion());
    }

    @Test
    void obtenerPorIdInexistenteDebeRetornarNull() {
        when(categoriaRepository.findById(50L)).thenReturn(Optional.empty());

        assertNull(categoriaService.obtenerPorId(50L));
    }

    @Test
    void buscarPorNombreDebeRetornarCoincidencias() {
        when(categoriaRepository.findByNombreContainingIgnoreCase("fic"))
                .thenReturn(List.of(new Categoria(2L, "Ciencia Ficcion", "Genero")));

        List<CategoriaDTO> resultado = categoriaService.buscarPorNombre("fic");

        assertEquals(1, resultado.size());
        assertEquals("Ciencia Ficcion", resultado.get(0).getNombre());
    }
}
