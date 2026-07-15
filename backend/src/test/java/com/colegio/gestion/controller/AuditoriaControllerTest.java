package com.colegio.gestion.controller;

import com.colegio.gestion.domain.AccionAuditoria;
import com.colegio.gestion.dto.response.AuditoriaResponse;
import com.colegio.gestion.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditoriaController.class)
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void listar_sinFiltros_retornaTodoElHistorial() throws Exception {
        when(auditoriaService.listarTodo()).thenReturn(List.of(
                new AuditoriaResponse(1L, "PROFESOR", 1L, AccionAuditoria.CREAR, "{}", Instant.now())));

        mockMvc.perform(get("/api/v1/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entidad").value("PROFESOR"));
    }

    @Test
    void listar_conFiltros_delegaEnListarPorEntidad() throws Exception {
        when(auditoriaService.listarPorEntidad("CURSO", 3L)).thenReturn(List.of(
                new AuditoriaResponse(2L, "CURSO", 3L, AccionAuditoria.ACTUALIZAR, "{}", Instant.now())));

        mockMvc.perform(get("/api/v1/auditoria").param("entidad", "curso").param("entidadId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entidadId").value(3));
    }
}
