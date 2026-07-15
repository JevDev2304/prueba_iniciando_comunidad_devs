package com.colegio.gestion.service.impl;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.domain.Auditoria;
import com.colegio.gestion.dto.response.AuditoriaResponse;
import com.colegio.gestion.mapper.AuditoriaMapper;
import com.colegio.gestion.repository.AuditoriaRepository;
import com.colegio.gestion.service.AuditoriaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final AuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository, ObjectMapper objectMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registrar(String entidad, Long entidadId, AccionAuditoria accion, Object snapshot) {
        Auditoria auditoria = new Auditoria();
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setAccion(accion);
        auditoria.setDetalle(serializar(snapshot));
        auditoriaRepository.save(auditoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarTodo() {
        return auditoriaRepository.findAllByOrderByFechaDesc().stream()
                .map(AuditoriaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarPorEntidad(String entidad, Long entidadId) {
        return auditoriaRepository.findByEntidadAndEntidadIdOrderByFechaDesc(entidad, entidadId).stream()
                .map(AuditoriaMapper::toResponse)
                .toList();
    }

    private String serializar(Object snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("No se pudo serializar el snapshot de auditoria, se guarda toString()", e);
            return String.valueOf(snapshot);
        }
    }
}
