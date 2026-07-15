package com.colegio.gestion.service;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Curso;
import com.colegio.gestion.domain.Profesor;
import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ProfesorConCursosAsignadosException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.repository.CursoRepository;
import com.colegio.gestion.repository.ProfesorRepository;
import com.colegio.gestion.service.impl.ProfesorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la capa de negocio de Profesor. Los repositorios y el
 * servicio de auditoria se mockean: aqui solo se verifican las reglas de
 * negocio (criterios 1, 3 y 4 del enunciado), no el acceso real a datos.
 */
@ExtendWith(MockitoExtension.class)
class ProfesorServiceImplTest {

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private ProfesorServiceImpl profesorService;

    @BeforeEach
    void setUp() {
        profesorService = new ProfesorServiceImpl(profesorRepository, cursoRepository, auditoriaService);
    }

    private ProfesorRequest requestValido() {
        return new ProfesorRequest("Ana", "Gomez", "ana.gomez@colegio.edu", "Matematicas");
    }

    private Profesor profesorConId(Long id) {
        Profesor profesor = new Profesor();
        profesor.setId(id);
        profesor.setNombre("Ana");
        profesor.setApellido("Gomez");
        profesor.setEmail("ana.gomez@colegio.edu");
        profesor.setEspecialidad("Matematicas");
        return profesor;
    }

    @Test
    void crear_conDatosValidos_retornaProfesorCreadoYRegistraAuditoria() {
        when(profesorRepository.existsByEmail("ana.gomez@colegio.edu")).thenReturn(false);
        when(profesorRepository.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        ProfesorResponse response = profesorService.crear(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana.gomez@colegio.edu");
        verify(auditoriaService).registrar(eq("PROFESOR"), eq(1L), eq(AccionAuditoria.CREAR), any());
    }

    @Test
    void crear_conEmailYaRegistrado_lanzaDuplicateEmailException() {
        when(profesorRepository.existsByEmail("ana.gomez@colegio.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> profesorService.crear(requestValido()));

        verify(profesorRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(any(), any(), any(), any());
    }

    @Test
    void obtenerPorId_existente_retornaProfesor() {
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(profesorConId(1L)));

        ProfesorResponse response = profesorService.obtenerPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nombre()).isEqualTo("Ana");
    }

    @Test
    void obtenerPorId_noExistente_lanzaResourceNotFoundException() {
        when(profesorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profesorService.obtenerPorId(99L));
    }

    @Test
    void listarTodos_retornaListaDeResumenes() {
        when(profesorRepository.findAll()).thenReturn(List.of(profesorConId(1L), profesorConId(2L)));

        List<ProfesorResumenResponse> resultado = profesorService.listarTodos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nombre()).isEqualTo("Ana");
    }

    @Test
    void actualizar_conDatosValidos_retornaProfesorActualizadoYRegistraAuditoria() {
        Profesor existente = profesorConId(1L);
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(profesorRepository.existsByEmailAndIdNot("ana.gomez@colegio.edu", 1L)).thenReturn(false);
        when(profesorRepository.save(any(Profesor.class))).thenReturn(existente);

        ProfesorResponse response = profesorService.actualizar(1L, requestValido());

        assertThat(response.id()).isEqualTo(1L);
        verify(auditoriaService).registrar(eq("PROFESOR"), eq(1L), eq(AccionAuditoria.ACTUALIZAR), any());
    }

    @Test
    void actualizar_conEmailDeOtroProfesor_lanzaDuplicateEmailException() {
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(profesorConId(1L)));
        when(profesorRepository.existsByEmailAndIdNot("ana.gomez@colegio.edu", 1L)).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> profesorService.actualizar(1L, requestValido()));

        verify(profesorRepository, never()).save(any());
    }

    @Test
    void eliminar_sinCursosAsignados_marcaEliminadoEnYRegistraAuditoria() {
        Profesor existente = profesorConId(1L);
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(cursoRepository.existsByProfesorId(1L)).thenReturn(false);
        when(profesorRepository.save(any(Profesor.class))).thenReturn(existente);

        profesorService.eliminar(1L);

        ArgumentCaptor<Profesor> captor = ArgumentCaptor.forClass(Profesor.class);
        verify(profesorRepository).save(captor.capture());
        assertThat(captor.getValue().getEliminadoEn()).isNotNull();
        verify(auditoriaService).registrar(eq("PROFESOR"), eq(1L), eq(AccionAuditoria.ELIMINAR), any());
    }

    @Test
    void eliminar_conCursosAsignados_lanzaProfesorConCursosAsignadosException() {
        Profesor existente = profesorConId(1L);
        existente.setCursos(List.of(new Curso()));
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(cursoRepository.existsByProfesorId(1L)).thenReturn(true);

        assertThrows(ProfesorConCursosAsignadosException.class, () -> profesorService.eliminar(1L));

        verify(profesorRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(any(), anyLong(), any(), any());
    }
}
