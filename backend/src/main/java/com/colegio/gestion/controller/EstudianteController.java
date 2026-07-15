package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.EstudianteRequest;
import com.colegio.gestion.dto.response.EstudianteResponse;
import com.colegio.gestion.dto.response.EstudianteResumenResponse;
import com.colegio.gestion.service.EstudianteService;
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
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Estudiantes", description = "Gestion de estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @PostMapping
    @Operation(summary = "Crear un estudiante")
    public ResponseEntity<EstudianteResponse> crear(@Valid @RequestBody EstudianteRequest request) {
        EstudianteResponse creado = estudianteService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/estudiantes/" + creado.id())).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar todos los estudiantes")
    public ResponseEntity<List<EstudianteResumenResponse>> listar() {
        return ResponseEntity.ok(estudianteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un estudiante por id")
    public ResponseEntity<EstudianteResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un estudiante")
    public ResponseEntity<EstudianteResponse> actualizar(@PathVariable Long id,
                                                           @Valid @RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un estudiante")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
