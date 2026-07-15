package com.colegio.gestion.service.impl;

import com.colegio.gestion.domain.Profesor;
import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ProfesorConCursosAsignadosException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.mapper.ProfesorMapper;
import com.colegio.gestion.repository.CursoRepository;
import com.colegio.gestion.repository.ProfesorRepository;
import com.colegio.gestion.service.ProfesorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProfesorServiceImpl implements ProfesorService {

    private final ProfesorRepository profesorRepository;
    private final CursoRepository cursoRepository;

    public ProfesorServiceImpl(ProfesorRepository profesorRepository, CursoRepository cursoRepository) {
        this.profesorRepository = profesorRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    public ProfesorResponse crear(ProfesorRequest request) {
        if (profesorRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        Profesor profesor = ProfesorMapper.toEntity(request);
        Profesor guardado = profesorRepository.save(profesor);
        return ProfesorMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesorResponse obtenerPorId(Long id) {
        Profesor profesor = buscarPorIdOrThrow(id);
        return ProfesorMapper.toResponse(profesor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesorResumenResponse> listarTodos() {
        return profesorRepository.findAll().stream()
                .map(ProfesorMapper::toResumen)
                .toList();
    }

    @Override
    public ProfesorResponse actualizar(Long id, ProfesorRequest request) {
        Profesor profesor = buscarPorIdOrThrow(id);

        if (profesorRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException(request.email());
        }

        ProfesorMapper.applyRequest(profesor, request);
        Profesor actualizado = profesorRepository.save(profesor);
        return ProfesorMapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Profesor profesor = buscarPorIdOrThrow(id);

        if (cursoRepository.existsByProfesorId(id)) {
            long cantidadCursos = profesor.getCursos().size();
            throw new ProfesorConCursosAsignadosException(id, cantidadCursos);
        }

        profesorRepository.delete(profesor);
    }

    private Profesor buscarPorIdOrThrow(Long id) {
        return profesorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Profesor", id));
    }
}
