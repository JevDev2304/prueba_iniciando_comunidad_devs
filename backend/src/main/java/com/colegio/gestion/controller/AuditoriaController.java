package com.colegio.gestion.controller;

import com.colegio.gestion.dto.response.AuditoriaResponse;
import com.colegio.gestion.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria", description = "Historial de cambios de Profesor, Curso y Estudiante (solo lectura)")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar el historial de auditoria, opcionalmente filtrado por entidad e id")
    public ResponseEntity<List<AuditoriaResponse>> listar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) Long entidadId) {

        if (entidad != null && entidadId != null) {
            return ResponseEntity.ok(auditoriaService.listarPorEntidad(entidad.toUpperCase(), entidadId));
        }
        return ResponseEntity.ok(auditoriaService.listarTodo());
    }
}
