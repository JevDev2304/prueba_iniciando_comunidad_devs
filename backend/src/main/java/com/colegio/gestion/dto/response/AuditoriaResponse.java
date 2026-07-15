package com.colegio.gestion.dto.response;

import com.colegio.gestion.domain.AccionAuditoria;

import java.time.Instant;

public record AuditoriaResponse(
        Long id,
        String entidad,
        Long entidadId,
        AccionAuditoria accion,
        String detalle,
        Instant fecha
) {
}
