package com.colegio.gestion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfesorRequest(

        @Schema(description = "Nombre del profesor", example = "Ana")
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Schema(description = "Apellido del profesor", example = "Gomez")
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Schema(description = "Email unico del profesor", example = "ana.gomez@colegio.edu")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        String email,

        @Schema(description = "Especialidad del profesor", example = "Matematicas")
        @NotBlank(message = "La especialidad es obligatoria")
        String especialidad
) {
}
