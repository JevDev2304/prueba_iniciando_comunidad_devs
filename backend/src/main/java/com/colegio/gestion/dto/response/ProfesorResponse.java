package com.colegio.gestion.dto.response;

import java.util.List;

public record ProfesorResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String especialidad,
        List<CursoResumenResponse> cursos
) {
}
