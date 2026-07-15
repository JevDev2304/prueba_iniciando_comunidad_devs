package com.colegio.gestion.service;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.dto.response.AuditoriaResponse;

import java.util.List;

public interface AuditoriaService {

    void registrar(String entidad, Long entidadId, AccionAuditoria accion, Object snapshot);

    List<AuditoriaResponse> listarTodo();

    List<AuditoriaResponse> listarPorEntidad(String entidad, Long entidadId);
}
