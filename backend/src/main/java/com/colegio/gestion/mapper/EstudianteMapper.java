package com.colegio.gestion.mapper;

import com.colegio.gestion.domain.Estudiante;
import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;

import java.util.List;

public final class EstudianteMapper {

    private EstudianteMapper() {
    }

    public static EstudianteResumenResponse toResumen(Estudiante estudiante) {
        return new EstudianteResumenResponse(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getEmail(),
                estudiante.getFechaNacimiento()
        );
    }

    public static EstudianteResponse toResponse(Estudiante estudiante) {
        List<CursoResumenResponse> cursos = estudiante.getCursos().stream()
                .map(CursoMapper::toResumen)
                .toList();

        return new EstudianteResponse(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getEmail(),
                estudiante.getFechaNacimiento(),
                cursos
        );
    }

    public static Estudiante toEntity(EstudianteRequest request) {
        Estudiante estudiante = new Estudiante();
        applyRequest(estudiante, request);
        return estudiante;
    }

    public static void applyRequest(Estudiante estudiante, EstudianteRequest request) {
        estudiante.setNombre(request.nombre());
        estudiante.setApellido(request.apellido());
        estudiante.setEmail(request.email());
        estudiante.setFechaNacimiento(request.fechaNacimiento());
    }
}
