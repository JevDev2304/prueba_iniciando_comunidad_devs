package com.colegio.gestion.dto.response;

public record CursoResumenResponse(
        Long id,
        String nombre,
        String descripcion,
        ProfesorResumenResponse profesor
) {
}
