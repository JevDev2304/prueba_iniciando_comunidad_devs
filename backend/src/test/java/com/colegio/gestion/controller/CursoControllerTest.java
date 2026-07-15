package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.exception.ProfesorInvalidoException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.service.CursoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CursoController.class)
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CursoService cursoService;

    private CursoResponse cursoResponse() {
        ProfesorResumenResponse profesor = new ProfesorResumenResponse(1L, "Ana", "Gomez", "ana@x.com", "Matematicas");
        return new CursoResponse(1L, "Algebra I", "Curso intro", profesor, List.of());
    }

    @Test
    void crear_conProfesorValido_retorna201() throws Exception {
        CursoRequest request = new CursoRequest("Algebra I", "Curso intro", 1L);
        when(cursoService.crear(any(CursoRequest.class))).thenReturn(cursoResponse());

        mockMvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profesor.id").value(1));
    }

    @Test
    void crear_sinProfesorId_retorna400PorValidacion() throws Exception {
        CursoRequest request = new CursoRequest("Algebra I", "x", null);

        mockMvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("profesorId"));
    }

    @Test
    void crear_conProfesorInexistente_retorna400() throws Exception {
        CursoRequest request = new CursoRequest("Algebra I", "x", 999L);
        when(cursoService.crear(any(CursoRequest.class))).thenThrow(new ProfesorInvalidoException(999L));

        mockMvc.perform(post("/api/v1/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listar_retorna200() throws Exception {
        ProfesorResumenResponse profesor = new ProfesorResumenResponse(1L, "Ana", "Gomez", "ana@x.com", "Matematicas");
        when(cursoService.listarTodos())
                .thenReturn(List.of(new CursoResumenResponse(1L, "Algebra I", "x", profesor)));

        mockMvc.perform(get("/api/v1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Algebra I"));
    }

    @Test
    void obtenerPorId_inexistente_retorna404() throws Exception {
        when(cursoService.obtenerPorId(99L)).thenThrow(ResourceNotFoundException.of("Curso", 99L));

        mockMvc.perform(get("/api/v1/cursos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void inscribirEstudiante_retorna200ConElCursoActualizado() throws Exception {
        when(cursoService.inscribirEstudiante(1L, 1L)).thenReturn(cursoResponse());

        mockMvc.perform(post("/api/v1/cursos/1/estudiantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void inscribirEstudiante_estudianteInexistente_retorna404() throws Exception {
        when(cursoService.inscribirEstudiante(1L, 99L)).thenThrow(ResourceNotFoundException.of("Estudiante", 99L));

        mockMvc.perform(post("/api/v1/cursos/1/estudiantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void retirarEstudiante_retorna200() throws Exception {
        when(cursoService.retirarEstudiante(1L, 1L)).thenReturn(cursoResponse());

        mockMvc.perform(delete("/api/v1/cursos/1/estudiantes/1"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/cursos/1"))
                .andExpect(status().isNoContent());
    }
}
