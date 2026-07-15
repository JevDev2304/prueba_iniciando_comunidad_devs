package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.exception.DuplicateEmailException;
import com.colegio.gestion.exception.ProfesorConCursosAsignadosException;
import com.colegio.gestion.exception.ResourceNotFoundException;
import com.colegio.gestion.service.ProfesorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

/**
 * Pruebas de la capa web de Profesor: solo se carga el contexto de Spring MVC
 * (controller + GlobalExceptionHandler), el service se mockea. Verifican los
 * criterios 1, 3, 5 y 6 del enunciado a nivel de codigos HTTP y forma de la
 * respuesta.
 */
@WebMvcTest(ProfesorController.class)
class ProfesorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfesorService profesorService;

    private ProfesorResponse profesorResponse() {
        return new ProfesorResponse(1L, "Ana", "Gomez", "ana.gomez@colegio.edu", "Matematicas", List.of());
    }

    @Test
    void crear_conDatosValidos_retorna201() throws Exception {
        ProfesorRequest request = new ProfesorRequest("Ana", "Gomez", "ana.gomez@colegio.edu", "Matematicas");
        when(profesorService.crear(any(ProfesorRequest.class))).thenReturn(profesorResponse());

        mockMvc.perform(post("/api/v1/profesores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana.gomez@colegio.edu"));
    }

    @Test
    void crear_conCamposVacios_retorna400ConDetalleDeErrores() throws Exception {
        ProfesorRequest request = new ProfesorRequest("", "", "no-es-email", "");

        mockMvc.perform(post("/api/v1/profesores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void crear_conEmailDuplicado_retorna409() throws Exception {
        ProfesorRequest request = new ProfesorRequest("Ana", "Gomez", "ana.gomez@colegio.edu", "Matematicas");
        when(profesorService.crear(any(ProfesorRequest.class)))
                .thenThrow(new DuplicateEmailException("ana.gomez@colegio.edu"));

        mockMvc.perform(post("/api/v1/profesores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listar_retorna200ConLaLista() throws Exception {
        when(profesorService.listarTodos())
                .thenReturn(List.of(new ProfesorResumenResponse(1L, "Ana", "Gomez", "ana@x.com", "Matematicas")));

        mockMvc.perform(get("/api/v1/profesores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorId_existente_retorna200() throws Exception {
        when(profesorService.obtenerPorId(1L)).thenReturn(profesorResponse());

        mockMvc.perform(get("/api/v1/profesores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void obtenerPorId_inexistente_retorna404() throws Exception {
        when(profesorService.obtenerPorId(99L)).thenThrow(ResourceNotFoundException.of("Profesor", 99L));

        mockMvc.perform(get("/api/v1/profesores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void actualizar_conDatosValidos_retorna200() throws Exception {
        ProfesorRequest request = new ProfesorRequest("Ana", "Gomez", "ana.gomez@colegio.edu", "Fisica");
        when(profesorService.actualizar(eq(1L), any(ProfesorRequest.class))).thenReturn(profesorResponse());

        mockMvc.perform(put("/api/v1/profesores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminar_sinCursosAsignados_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/profesores/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_conCursosAsignados_retorna409() throws Exception {
        org.mockito.Mockito.doThrow(new ProfesorConCursosAsignadosException(1L, 2))
                .when(profesorService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/profesores/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void crear_conJsonMalFormado_retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/profesores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json valido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void obtenerPorId_conErrorInesperadoDelService_retorna500() throws Exception {
        when(profesorService.obtenerPorId(1L)).thenThrow(new RuntimeException("fallo inesperado"));

        mockMvc.perform(get("/api/v1/profesores/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrio un error inesperado"));
    }
}
