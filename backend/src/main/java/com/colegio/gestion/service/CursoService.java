package com.colegio.gestion.service;

import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;

import java.util.List;

public interface CursoService {

    CursoResponse crear(CursoRequest request);

    CursoResponse obtenerPorId(Long id);

    List<CursoResumenResponse> listarTodos();

    CursoResponse actualizar(Long id, CursoRequest request);

    void eliminar(Long id);

    CursoResponse inscribirEstudiante(Long cursoId, Long estudianteId);

    CursoResponse retirarEstudiante(Long cursoId, Long estudianteId);
}
