package com.colegio.gestion.controller;

import com.colegio.gestion.dto.request.ProfesorRequest;
import com.colegio.gestion.dto.response.ProfesorResponse;
import com.colegio.gestion.dto.response.ProfesorResumenResponse;
import com.colegio.gestion.service.ProfesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/profesores")
@Tag(name = "Profesores", description = "Gestion de profesores")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @PostMapping
    @Operation(summary = "Crear un profesor")
    public ResponseEntity<ProfesorResponse> crear(@Valid @RequestBody ProfesorRequest request) {
        ProfesorResponse creado = profesorService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/profesores/" + creado.id())).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar todos los profesores")
    public ResponseEntity<List<ProfesorResumenResponse>> listar() {
        return ResponseEntity.ok(profesorService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un profesor por id")
    public ResponseEntity<ProfesorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un profesor")
    public ResponseEntity<ProfesorResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ProfesorRequest request) {
        return ResponseEntity.ok(profesorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un profesor (falla con 409 si tiene cursos asignados)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
