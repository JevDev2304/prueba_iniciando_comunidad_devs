package com.colegio.gestion.dto.response;

import java.time.LocalDate;

public record EstudianteResumenResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        LocalDate fechaNacimiento
) {
}
