package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.CursoRequest;
import com.colegio.gestion.dto.response.CursoResponse;
import com.colegio.gestion.dto.response.CursoResumenResponse;
import com.colegio.gestion.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cursos")
@Tag(name = "Cursos", description = "Gestion de cursos e inscripciones")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    @Operation(summary = "Crear un curso (requiere un profesorId valido)")
    public ResponseEntity<CursoResponse> crear(@Valid @RequestBody CursoRequest request) {
        CursoResponse creado = cursoService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/cursos/" + creado.id())).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar todos los cursos")
    public ResponseEntity<List<CursoResumenResponse>> listar() {
        return ResponseEntity.ok(cursoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un curso por id, incluyendo profesor y estudiantes")
    public ResponseEntity<CursoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un curso (permite reasignar profesor)")
    public ResponseEntity<CursoResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody CursoRequest request) {
        return ResponseEntity.ok(cursoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un curso")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cursoId}/estudiantes/{estudianteId}")
    @Operation(summary = "Inscribir un estudiante en el curso")
    public ResponseEntity<CursoResponse> inscribirEstudiante(@PathVariable Long cursoId,
                                                               @PathVariable Long estudianteId) {
        return ResponseEntity.ok(cursoService.inscribirEstudiante(cursoId, estudianteId));
    }

    @DeleteMapping("/{cursoId}/estudiantes/{estudianteId}")
    @Operation(summary = "Retirar un estudiante del curso")
    public ResponseEntity<CursoResponse> retirarEstudiante(@PathVariable Long cursoId,
                                                             @PathVariable Long estudianteId) {
        return ResponseEntity.ok(cursoService.retirarEstudiante(cursoId, estudianteId));
    }
}
