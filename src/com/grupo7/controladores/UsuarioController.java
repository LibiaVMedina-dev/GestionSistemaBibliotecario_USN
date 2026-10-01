package com.grupo7.controladores;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.grupo7.config.ConexionDB;
import com.grupo7.dao.UsuarioDAO;
import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Usuario;

/** Controla el alta, consulta y estado administrativo de los usuarios. */
public final class UsuarioController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PrestamoController prestamoController = new PrestamoController();

    public List<Usuario> listar() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return usuarioDAO.listarTodos(con);
        }
    }

    public Usuario buscar(String codigo) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return usuarioDAO.buscarPorCodigo(codigo, con);
        }
    }

    public ResultadoOperacion registrar(Usuario usuario) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                if (usuarioDAO.existeDuplicado(
                        usuario.getCodigo(), usuario.getDni(), usuario.getCorreo(), 0, con)) {
                    con.rollback();
                    return ResultadoOperacion.error(
                        "El código, el DNI o el correo ya están registrados."
                    );
                }
                if (!usuarioDAO.insertar(usuario, con)) {
                    throw new SQLException("No se pudo registrar íntegramente el usuario.");
                }
                con.commit();
                return ResultadoOperacion.exito("El usuario se registró correctamente.");
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public ResultadoOperacion cambiarEstado(Usuario usuario, String nuevoEstado) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                if (nuevoEstado.equals(usuario.getEstado())) {
                    con.rollback();
                    return ResultadoOperacion.error(
                        "El usuario ya está en estado " + nuevoEstado + "."
                    );
                }

                if ("ACTIVO".equals(nuevoEstado) && usuario instanceof Estudiante estudiante) {
                    Usuario actual = usuarioDAO.buscarPorCodigo(estudiante.getCodigo(), con);
                    if (actual instanceof Estudiante estudianteActual) {
                        ResultadoOperacion elegibilidad =
                            prestamoController.validarObligacionesPendientes(estudianteActual, con);
                        if (!elegibilidad.exitoso()) {
                            con.rollback();
                            return elegibilidad;
                        }
                    }
                }

                if (!usuarioDAO.actualizarEstado(usuario.getIdUsuario(), nuevoEstado, con)) {
                    throw new SQLException("No se pudo cambiar el estado del usuario.");
                }
                con.commit();
                return ResultadoOperacion.exito(
                    "El usuario quedó en estado " + nuevoEstado + "."
                );
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

}
