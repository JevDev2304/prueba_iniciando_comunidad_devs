package com.colegio.gestion.exception;

/**
 * Intento de eliminar un profesor que aun tiene cursos asignados. Mapeada a HTTP 409.
 * Decision de diseno: no se reasignan cursos automaticamente; el cliente debe reasignarlos
 * explicitamente (PUT /cursos/{id}) antes de poder eliminar al profesor.
 */
public class ProfesorConCursosAsignadosException extends RuntimeException {

    public ProfesorConCursosAsignadosException(Long profesorId, long cantidadCursos) {
        super("No se puede eliminar el profesor con id " + profesorId + " porque tiene "
                + cantidadCursos + " curso(s) asignado(s). Reasigne los cursos a otro profesor antes de eliminar.");
    }
}
