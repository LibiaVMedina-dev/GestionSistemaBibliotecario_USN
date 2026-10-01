package com.grupo7.controladores;

import com.grupo7.modelos.Bibliotecario;

/** Resultado de autenticación sin exponer detalles sensibles. */
public record ResultadoLogin(boolean exitoso, String mensaje, Bibliotecario usuario) {
    public static ResultadoLogin exito(Bibliotecario usuario) {
        return new ResultadoLogin(true, "Ingreso correcto", usuario);
    }

    public static ResultadoLogin error(String mensaje) {
        return new ResultadoLogin(false, mensaje, null);
    }
}
