package com.grupo7.controladores;

import java.sql.Connection;

import com.grupo7.config.ConexionDB;
import com.grupo7.dao.UsuarioDAO;
import com.grupo7.modelos.Bibliotecario;
import com.grupo7.modelos.Usuario;

/** Controla la autenticación y el acceso al sistema administrativo. */
public final class LoginController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public ResultadoLogin autenticar(String codigo, String contrasena) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            Usuario usuario = usuarioDAO.buscarPorCodigo(codigo, con);

            if (usuario == null || !usuario.login(codigo, contrasena)) {
                return ResultadoLogin.error("Credenciales inválidas.");
            }

            if (!(usuario instanceof Bibliotecario bibliotecario)) {
                return ResultadoLogin.error(
                    "Acceso denegado. El sistema administrativo es exclusivo para el personal de biblioteca."
                );
            }

            if (!"ACTIVO".equals(bibliotecario.getEstado())) {
                return ResultadoLogin.error(
                    "El usuario está " + bibliotecario.getEstado()
                        + ". Contacta al administrador."
                );
            }

            String rol = bibliotecario.getRol();
            if (!"ADMINISTRADOR".equals(rol) && !"BIBLIOTECARIO".equals(rol)) {
                return ResultadoLogin.error("El usuario no tiene un rol autorizado.");
            }

            return ResultadoLogin.exito(bibliotecario);
        }
    }
}
