package com.colegio.gestion.mapper;

import com.colegio.gestion.domain.Curso;
import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;

import java.util.List;

public final class CursoMapper {

    private CursoMapper() {
    }

    public static CursoResumenResponse toResumen(Curso curso) {
        return new CursoResumenResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                ProfesorMapper.toResumen(curso.getProfesor())
        );
    }

    public static CursoResponse toResponse(Curso curso) {
        List<EstudianteResumenResponse> estudiantes = curso.getEstudiantes().stream()
                .map(EstudianteMapper::toResumen)
                .toList();

        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                ProfesorMapper.toResumen(curso.getProfesor()),
                estudiantes
        );
    }

    public static void applyRequest(Curso curso, CursoRequest request) {
        curso.setNombre(request.nombre());
        curso.setDescripcion(request.descripcion());
    }
}
