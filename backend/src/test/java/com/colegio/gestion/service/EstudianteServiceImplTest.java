package com.colegio.gestion.service;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Estudiante;
import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.repository.EstudianteRepository;
import com.colegio.gestion.service.impl.EstudianteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceImplTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private EstudianteServiceImpl estudianteService;

    @BeforeEach
    void setUp() {
        estudianteService = new EstudianteServiceImpl(estudianteRepository, auditoriaService);
    }

    private EstudianteRequest requestValido() {
        return new EstudianteRequest("Luis", "Perez", "luis.perez@colegio.edu", LocalDate.of(2010, 5, 20));
    }

    private Estudiante estudianteConId(Long id) {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(id);
        estudiante.setNombre("Luis");
        estudiante.setApellido("Perez");
        estudiante.setEmail("luis.perez@colegio.edu");
        estudiante.setFechaNacimiento(LocalDate.of(2010, 5, 20));
        return estudiante;
    }

    @Test
    void crear_conDatosValidos_retornaEstudianteCreadoYRegistraAuditoria() {
        when(estudianteRepository.existsByEmail("luis.perez@colegio.edu")).thenReturn(false);
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocation -> {
            Estudiante guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        EstudianteResponse response = estudianteService.crear(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("luis.perez@colegio.edu");
        verify(auditoriaService).registrar(eq("ESTUDIANTE"), eq(1L), eq(AccionAuditoria.CREAR), any());
    }

    @Test
    void crear_conEmailYaRegistrado_lanzaDuplicateEmailException() {
        when(estudianteRepository.existsByEmail("luis.perez@colegio.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> estudianteService.crear(requestValido()));

        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_existente_retornaEstudiante() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteConId(1L)));

        EstudianteResponse response = estudianteService.obtenerPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.fechaNacimiento()).isEqualTo(LocalDate.of(2010, 5, 20));
    }

    @Test
    void obtenerPorId_noExistente_lanzaResourceNotFoundException() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> estudianteService.obtenerPorId(99L));
    }

    @Test
    void listarTodos_retornaListaDeResumenes() {
        when(estudianteRepository.findAll()).thenReturn(List.of(estudianteConId(1L), estudianteConId(2L)));

        List<EstudianteResumenResponse> resultado = estudianteService.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void actualizar_conDatosValidos_retornaEstudianteActualizado() {
        Estudiante existente = estudianteConId(1L);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(estudianteRepository.existsByEmailAndIdNot("luis.perez@colegio.edu", 1L)).thenReturn(false);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(existente);

        EstudianteResponse response = estudianteService.actualizar(1L, requestValido());

        assertThat(response.id()).isEqualTo(1L);
        verify(auditoriaService).registrar(eq("ESTUDIANTE"), eq(1L), eq(AccionAuditoria.ACTUALIZAR), any());
    }

    @Test
    void eliminar_marcaEliminadoEnYRegistraAuditoria() {
        Estudiante existente = estudianteConId(1L);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(existente);

        estudianteService.eliminar(1L);

        ArgumentCaptor<Estudiante> captor = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepository).save(captor.capture());
        assertThat(captor.getValue().getEliminadoEn()).isNotNull();
        verify(auditoriaService).registrar(eq("ESTUDIANTE"), eq(1L), eq(AccionAuditoria.ELIMINAR), any());
    }

    @Test
    void eliminar_noExistente_lanzaResourceNotFoundException() {
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> estudianteService.eliminar(99L));
    }
}
