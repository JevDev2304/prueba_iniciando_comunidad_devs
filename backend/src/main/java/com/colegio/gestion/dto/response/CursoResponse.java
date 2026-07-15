package com.colegio.gestion.dto.response;

import java.util.List;

public record CursoResponse(
        Long id,
        String nombre,
        String descripcion,
        ProfesorResumenResponse profesor,
        List<EstudianteResumenResponse> estudiantes
) {
}
