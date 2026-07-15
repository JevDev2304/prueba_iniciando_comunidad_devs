package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.service.EstudianteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstudianteController.class)
class EstudianteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstudianteService estudianteService;

    private EstudianteResponse estudianteResponse() {
        return new EstudianteResponse(1L, "Luis", "Perez", "luis.perez@colegio.edu",
                LocalDate.of(2010, 5, 20), List.of());
    }

    @Test
    void crear_conDatosValidos_retorna201() throws Exception {
        EstudianteRequest request = new EstudianteRequest("Luis", "Perez", "luis.perez@colegio.edu",
                LocalDate.of(2010, 5, 20));
        when(estudianteService.crear(any(EstudianteRequest.class))).thenReturn(estudianteResponse());

        mockMvc.perform(post("/api/v1/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_conEmailMalFormado_retorna400() throws Exception {
        EstudianteRequest request = new EstudianteRequest("Luis", "Perez", "no-es-email",
                LocalDate.of(2010, 5, 20));

        mockMvc.perform(post("/api/v1/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void crear_conFechaNacimientoEnElFuturo_retorna400() throws Exception {
        EstudianteRequest request = new EstudianteRequest("Luis", "Perez", "luis@colegio.edu",
                LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/v1/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conEmailDuplicado_retorna409() throws Exception {
        EstudianteRequest request = new EstudianteRequest("Luis", "Perez", "luis.perez@colegio.edu",
                LocalDate.of(2010, 5, 20));
        when(estudianteService.crear(any(EstudianteRequest.class)))
                .thenThrow(new DuplicateEmailException("luis.perez@colegio.edu"));

        mockMvc.perform(post("/api/v1/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void listar_retorna200() throws Exception {
        when(estudianteService.listarTodos()).thenReturn(
                List.of(new EstudianteResumenResponse(1L, "Luis", "Perez", "luis@x.com", LocalDate.of(2010, 5, 20))));

        mockMvc.perform(get("/api/v1/estudiantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Luis"));
    }

    @Test
    void obtenerPorId_existente_retorna200() throws Exception {
        when(estudianteService.obtenerPorId(1L)).thenReturn(estudianteResponse());

        mockMvc.perform(get("/api/v1/estudiantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Luis"));
    }

    @Test
    void obtenerPorId_inexistente_retorna404() throws Exception {
        when(estudianteService.obtenerPorId(99L)).thenThrow(ResourceNotFoundException.of("Estudiante", 99L));

        mockMvc.perform(get("/api/v1/estudiantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_conDatosValidos_retorna200() throws Exception {
        EstudianteRequest request = new EstudianteRequest("Luis", "Perez", "luis.perez@colegio.edu",
                LocalDate.of(2010, 5, 20));
        when(estudianteService.actualizar(eq(1L), any(EstudianteRequest.class))).thenReturn(estudianteResponse());

        mockMvc.perform(put("/api/v1/estudiantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/estudiantes/1"))
                .andExpect(status().isNoContent());
    }
}
