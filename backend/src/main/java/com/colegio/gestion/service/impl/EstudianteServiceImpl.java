package com.colegio.gestion.service.impl;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Estudiante;
import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.mapper.EstudianteMapper;
import com.colegio.gestion.repository.EstudianteRepository;
import com.colegio.gestion.service.AuditoriaService;
import com.colegio.gestion.service.EstudianteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class EstudianteServiceImpl implements EstudianteService {

    private static final String ENTIDAD = "ESTUDIANTE";

    private final EstudianteRepository estudianteRepository;
    private final AuditoriaService auditoriaService;

    public EstudianteServiceImpl(EstudianteRepository estudianteRepository, AuditoriaService auditoriaService) {
        this.estudianteRepository = estudianteRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public EstudianteResponse crear(EstudianteRequest request) {
        if (estudianteRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        Estudiante estudiante = EstudianteMapper.toEntity(request);
        Estudiante guardado = estudianteRepository.save(estudiante);
        EstudianteResponse response = EstudianteMapper.toResponse(guardado);

        auditoriaService.registrar(ENTIDAD, response.id(), AccionAuditoria.CREAR, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponse obtenerPorId(Long id) {
        Estudiante estudiante = buscarPorIdOrThrow(id);
        return EstudianteMapper.toResponse(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResumenResponse> listarTodos() {
        return estudianteRepository.findAll().stream()
                .map(EstudianteMapper::toResumen)
                .toList();
    }

    @Override
    public EstudianteResponse actualizar(Long id, EstudianteRequest request) {
        Estudiante estudiante = buscarPorIdOrThrow(id);

        if (estudianteRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateEmailException(request.email());
        }

        EstudianteMapper.applyRequest(estudiante, request);
        Estudiante actualizado = estudianteRepository.save(estudiante);
        EstudianteResponse response = EstudianteMapper.toResponse(actualizado);

        auditoriaService.registrar(ENTIDAD, id, AccionAuditoria.ACTUALIZAR, response);
        return response;
    }

    @Override
    public void eliminar(Long id) {
        Estudiante estudiante = buscarPorIdOrThrow(id);
        EstudianteResponse snapshot = EstudianteMapper.toResponse(estudiante);

        estudiante.setEliminadoEn(Instant.now());
        estudianteRepository.save(estudiante);

        auditoriaService.registrar(ENTIDAD, id, AccionAuditoria.ELIMINAR, snapshot);
    }

    private Estudiante buscarPorIdOrThrow(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Estudiante", id));
    }
}
