package com.colegio.gestion.mapper;

import com.colegio.gestion.domain.Profesor;
import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;

import java.util.List;

/**
 * Mapeo manual entidad <-> DTO. Los metodos toResumen() nunca incluyen colecciones anidadas,
 * lo que evita recursion infinita al combinarse con CursoMapper.
 */
public final class ProfesorMapper {

    private ProfesorMapper() {
    }

    public static ProfesorResumenResponse toResumen(Profesor profesor) {
        return new ProfesorResumenResponse(
                profesor.getId(),
                profesor.getNombre(),
                profesor.getApellido(),
                profesor.getEmail(),
                profesor.getEspecialidad()
        );
    }

    public static ProfesorResponse toResponse(Profesor profesor) {
        List<CursoResumenResponse> cursos = profesor.getCursos().stream()
                .map(CursoMapper::toResumen)
                .toList();

        return new ProfesorResponse(
                profesor.getId(),
                profesor.getNombre(),
                profesor.getApellido(),
                profesor.getEmail(),
                profesor.getEspecialidad(),
                cursos
        );
    }

    public static Profesor toEntity(ProfesorRequest request) {
        Profesor profesor = new Profesor();
        applyRequest(profesor, request);
        return profesor;
    }

    public static void applyRequest(Profesor profesor, ProfesorRequest request) {
        profesor.setNombre(request.nombre());
        profesor.setApellido(request.apellido());
        profesor.setEmail(request.email());
        profesor.setEspecialidad(request.especialidad());
    }
}
