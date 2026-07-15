package com.colegio.gestion.service;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Curso;
import com.colegio.gestion.domain.Estudiante;
import com.colegio.gestion.domain.Profesor;
import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.exception.ProfesorInvalidoException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.repository.CursoRepository;
import com.colegio.gestion.repository.EstudianteRepository;
import com.colegio.gestion.repository.ProfesorRepository;
import com.colegio.gestion.service.impl.CursoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la capa de negocio de Curso: creacion/actualizacion
 * con profesor invalido (criterio 2, debe ser 400 -> ProfesorInvalidoException)
 * e inscripcion/retiro bidireccional de estudiantes (criterio 4).
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private CursoServiceImpl cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoServiceImpl(cursoRepository, profesorRepository, estudianteRepository, auditoriaService);
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

    private Curso cursoConId(Long id, Profesor profesor) {
        Curso curso = new Curso();
        curso.setId(id);
        curso.setNombre("Algebra I");
        curso.setDescripcion("Curso introductorio");
        curso.setProfesor(profesor);
        return curso;
    }

    private Estudiante estudianteConId(Long id) {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(id);
        estudiante.setNombre("Luis");
        estudiante.setApellido("Perez");
        estudiante.setEmail("luis.perez@colegio.edu");
        return estudiante;
    }

    @Test
    void crear_conProfesorValido_retornaCursoCreado() {
        Profesor profesor = profesorConId(1L);
        when(profesorRepository.findById(1L)).thenReturn(Optional.of(profesor));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> {
            Curso guardado = invocation.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        CursoResponse response = cursoService.crear(new CursoRequest("Algebra I", "x", 1L));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.profesor().id()).isEqualTo(1L);
        verify(auditoriaService).registrar(eq("CURSO"), eq(1L), eq(AccionAuditoria.CREAR), any());
    }

    @Test
    void crear_conProfesorInexistente_lanzaProfesorInvalidoException() {
        when(profesorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProfesorInvalidoException.class,
                () -> cursoService.crear(new CursoRequest("Algebra I", "x", 999L)));

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_existente_retornaCurso() {
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(cursoConId(1L, profesorConId(1L))));

        CursoResponse response = cursoService.obtenerPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.profesor().id()).isEqualTo(1L);
    }

    @Test
    void actualizar_conProfesorValido_retornaCursoActualizadoYRegistraAuditoria() {
        Curso curso = cursoConId(1L, profesorConId(1L));
        Profesor nuevoProfesor = profesorConId(2L);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(profesorRepository.findById(2L)).thenReturn(Optional.of(nuevoProfesor));
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        CursoResponse response = cursoService.actualizar(1L, new CursoRequest("Algebra II", "actualizado", 2L));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(curso.getProfesor().getId()).isEqualTo(2L);
        verify(auditoriaService).registrar(eq("CURSO"), eq(1L), eq(AccionAuditoria.ACTUALIZAR), any());
    }

    @Test
    void actualizar_conProfesorInexistente_lanzaProfesorInvalidoException() {
        Curso curso = cursoConId(1L, profesorConId(1L));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(profesorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProfesorInvalidoException.class,
                () -> cursoService.actualizar(1L, new CursoRequest("Algebra I", "x", 999L)));

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_noExistente_lanzaResourceNotFoundException() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cursoService.obtenerPorId(99L));
    }

    @Test
    void listarTodos_retornaListaDeResumenes() {
        Profesor profesor = profesorConId(1L);
        when(cursoRepository.findAll()).thenReturn(List.of(cursoConId(1L, profesor), cursoConId(2L, profesor)));

        List<CursoResumenResponse> resultado = cursoService.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void inscribirEstudiante_actualizaLaRelacionEnAmbosSentidos() {
        Profesor profesor = profesorConId(1L);
        Curso curso = cursoConId(1L, profesor);
        Estudiante estudiante = estudianteConId(1L);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        CursoResponse response = cursoService.inscribirEstudiante(1L, 1L);

        assertThat(curso.getEstudiantes()).contains(estudiante);
        assertThat(estudiante.getCursos()).contains(curso);
        assertThat(response.estudiantes()).extracting("id").containsExactly(1L);
        verify(auditoriaService).registrar(eq("CURSO"), eq(1L), eq(AccionAuditoria.ACTUALIZAR), any());
    }

    @Test
    void retirarEstudiante_actualizaLaRelacionEnAmbosSentidos() {
        Profesor profesor = profesorConId(1L);
        Curso curso = cursoConId(1L, profesor);
        Estudiante estudiante = estudianteConId(1L);
        curso.getEstudiantes().add(estudiante);
        estudiante.getCursos().add(curso);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        cursoService.retirarEstudiante(1L, 1L);

        assertThat(curso.getEstudiantes()).doesNotContain(estudiante);
        assertThat(estudiante.getCursos()).doesNotContain(curso);
    }

    @Test
    void inscribirEstudiante_estudianteInexistente_lanzaResourceNotFoundException() {
        Curso curso = cursoConId(1L, profesorConId(1L));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(estudianteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cursoService.inscribirEstudiante(1L, 99L));

        verify(cursoRepository, never()).save(any());
    }

    @Test
    void eliminar_marcaEliminadoEnYRegistraAuditoria() {
        Curso curso = cursoConId(1L, profesorConId(1L));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenReturn(curso);

        cursoService.eliminar(1L);

        assertThat(curso.getEliminadoEn()).isNotNull();
        verify(auditoriaService).registrar(eq("CURSO"), eq(1L), eq(AccionAuditoria.ELIMINAR), any());
    }
}
