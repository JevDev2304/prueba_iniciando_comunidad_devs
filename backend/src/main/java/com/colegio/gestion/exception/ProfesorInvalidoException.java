package com.colegio.gestion.exception;

/**
 * El profesorId enviado al crear/actualizar un curso no corresponde a ningun profesor existente.
 * Mapeada a HTTP 400 (no 404) porque el criterio de aceptacion del sistema exige explicitamente
 * que un profesor invalido en un curso se rechace como error de solicitud, no como "no encontrado".
 */
public class ProfesorInvalidoException extends RuntimeException {

    public ProfesorInvalidoException(Long profesorId) {
        super("El profesor con id " + profesorId + " no existe. Debe especificar un profesor valido");
    }
}
