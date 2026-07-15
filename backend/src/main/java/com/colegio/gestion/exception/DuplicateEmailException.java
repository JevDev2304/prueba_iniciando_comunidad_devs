package com.colegio.gestion.exception;

/**
 * Email ya registrado para la entidad. Mapeada a HTTP 409 en GlobalExceptionHandler:
 * el payload es valido en formato, pero entra en conflicto con el estado actual de la base.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("El email '" + email + "' ya esta registrado");
    }
}
