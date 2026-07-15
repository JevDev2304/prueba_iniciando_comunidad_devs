package com.colegio.gestion.service;

import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;

import java.util.List;

public interface EstudianteService {

    EstudianteResponse crear(EstudianteRequest request);

    EstudianteResponse obtenerPorId(Long id);

    List<EstudianteResumenResponse> listarTodos();

    EstudianteResponse actualizar(Long id, EstudianteRequest request);

    void eliminar(Long id);
}
