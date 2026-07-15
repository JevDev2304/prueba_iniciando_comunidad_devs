package com.colegio.gestion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record EstudianteRequest(

        @Schema(description = "Nombre del estudiante", example = "Luis")
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Schema(description = "Apellido del estudiante", example = "Perez")
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Schema(description = "Email unico del estudiante", example = "luis.perez@colegio.edu")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        String email,

        @Schema(description = "Fecha de nacimiento", example = "2010-05-20")
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento
) {
}
