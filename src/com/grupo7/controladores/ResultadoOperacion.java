package com.grupo7.controladores;

/** Resultado simple de una regla de negocio ejecutada por un controlador. */
public record ResultadoOperacion(boolean exitoso, String mensaje) {
    public static ResultadoOperacion exito(String mensaje) {
        return new ResultadoOperacion(true, mensaje);
    }

    public static ResultadoOperacion error(String mensaje) {
        return new ResultadoOperacion(false, mensaje);
    }
}
