package com.colegio.gestion.service;

import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;

import java.util.List;

public interface ProfesorService {

    ProfesorResponse crear(ProfesorRequest request);

    ProfesorResponse obtenerPorId(Long id);

    List<ProfesorResumenResponse> listarTodos();

    ProfesorResponse actualizar(Long id, ProfesorRequest request);

    void eliminar(Long id);
}
