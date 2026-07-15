package com.colegio.gestion.exception;

/**
 * Recurso solicitado por id que no existe. Mapeada a HTTP 404 en GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entidad, Long id) {
        return new ResourceNotFoundException(entidad + " con id " + id + " no encontrado");
    }
}
