package com.colegio.gestion.dto.response;

import java.time.LocalDate;
import java.util.List;

public record EstudianteResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        LocalDate fechaNacimiento,
        List<CursoResumenResponse> cursos
) {
}
