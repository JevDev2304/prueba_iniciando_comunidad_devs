package com.colegio.gestion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CursoRequest(

        @Schema(description = "Nombre del curso", example = "Algebra I")
        @NotBlank(message = "El nombre del curso es obligatorio")
        String nombre,

        @Schema(description = "Descripcion del curso", example = "Curso introductorio de algebra")
        @Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres")
        String descripcion,

        @Schema(description = "Id del profesor asignado al curso", example = "1")
        @NotNull(message = "Debe especificar un profesor")
        Long profesorId
) {
}
