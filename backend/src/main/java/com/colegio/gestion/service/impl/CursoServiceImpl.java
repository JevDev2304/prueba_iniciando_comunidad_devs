package com.colegio.gestion.service.impl;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Curso;
import com.colegio.gestion.domain.Estudiante;
import com.colegio.gestion.domain.Profesor;
import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.exception.ProfesorInvalidoException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.mapper.CursoMapper;
import com.colegio.gestion.repository.CursoRepository;
import com.colegio.gestion.repository.EstudianteRepository;
import com.colegio.gestion.repository.ProfesorRepository;
import com.colegio.gestion.service.AuditoriaService;
import com.colegio.gestion.service.CursoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CursoServiceImpl implements CursoService {

    private static final String ENTIDAD = "CURSO";

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final EstudianteRepository estudianteRepository;
    private final AuditoriaService auditoriaService;

    public CursoServiceImpl(CursoRepository cursoRepository,
                             ProfesorRepository profesorRepository,
                             EstudianteRepository estudianteRepository,
                             AuditoriaService auditoriaService) {
        this.cursoRepository = cursoRepository;
        this.profesorRepository = profesorRepository;
        this.estudianteRepository = estudianteRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public CursoResponse crear(CursoRequest request) {
        Profesor profesor = buscarProfesorValidoOrThrow(request.profesorId());

        Curso curso = new Curso();
        CursoMapper.applyRequest(curso, request);
        curso.setProfesor(profesor);

        Curso guardado = cursoRepository.save(curso);
        CursoResponse response = CursoMapper.toResponse(guardado);

        auditoriaService.registrar(ENTIDAD, response.id(), AccionAuditoria.CREAR, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CursoResponse obtenerPorId(Long id) {
        Curso curso = buscarPorIdOrThrow(id);
        return CursoMapper.toResponse(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoResumenResponse> listarTodos() {
        return cursoRepository.findAll().stream()
                .map(CursoMapper::toResumen)
                .toList();
    }

    @Override
    public CursoResponse actualizar(Long id, CursoRequest request) {
        Curso curso = buscarPorIdOrThrow(id);
        Profesor profesor = buscarProfesorValidoOrThrow(request.profesorId());

        CursoMapper.applyRequest(curso, request);
        curso.setProfesor(profesor);

        Curso actualizado = cursoRepository.save(curso);
        CursoResponse response = CursoMapper.toResponse(actualizado);

        auditoriaService.registrar(ENTIDAD, id, AccionAuditoria.ACTUALIZAR, response);
        return response;
    }

    @Override
    public void eliminar(Long id) {
        Curso curso = buscarPorIdOrThrow(id);
        CursoResponse snapshot = CursoMapper.toResponse(curso);

        curso.setEliminadoEn(Instant.now());
        cursoRepository.save(curso);

        auditoriaService.registrar(ENTIDAD, id, AccionAuditoria.ELIMINAR, snapshot);
    }

    @Override
    public CursoResponse inscribirEstudiante(Long cursoId, Long estudianteId) {
        Curso curso = buscarPorIdOrThrow(cursoId);
        Estudiante estudiante = buscarEstudianteOrThrow(estudianteId);

        curso.getEstudiantes().add(estudiante);
        estudiante.getCursos().add(curso);

        Curso actualizado = cursoRepository.save(curso);
        CursoResponse response = CursoMapper.toResponse(actualizado);

        auditoriaService.registrar(ENTIDAD, cursoId, AccionAuditoria.ACTUALIZAR, response);
        return response;
    }

    @Override
    public CursoResponse retirarEstudiante(Long cursoId, Long estudianteId) {
        Curso curso = buscarPorIdOrThrow(cursoId);
        Estudiante estudiante = buscarEstudianteOrThrow(estudianteId);

        curso.getEstudiantes().remove(estudiante);
        estudiante.getCursos().remove(curso);

        Curso actualizado = cursoRepository.save(curso);
        CursoResponse response = CursoMapper.toResponse(actualizado);

        auditoriaService.registrar(ENTIDAD, cursoId, AccionAuditoria.ACTUALIZAR, response);
        return response;
    }

    private Profesor buscarProfesorValidoOrThrow(Long profesorId) {
        return profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ProfesorInvalidoException(profesorId));
    }

    private Curso buscarPorIdOrThrow(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Curso", id));
    }

    private Estudiante buscarEstudianteOrThrow(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Estudiante", id));
    }
}
