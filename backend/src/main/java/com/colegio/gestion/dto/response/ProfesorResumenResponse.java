package com.colegio.gestion.dto.response;

public record ProfesorResumenResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String especialidad
) {
}
