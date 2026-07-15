package com.colegio.gestion.mapper;

import com.colegio.gestion.domain.Auditoria;
import com.colegio.gestion.dto.response.AuditoriaResponse;

public final class AuditoriaMapper {

    private AuditoriaMapper() {
    }

    public static AuditoriaResponse toResponse(Auditoria auditoria) {
        return new AuditoriaResponse(
                auditoria.getId(),
                auditoria.getEntidad(),
                auditoria.getEntidadId(),
                auditoria.getAccion(),
                auditoria.getDetalle(),
                auditoria.getFecha()
        );
    }
}
