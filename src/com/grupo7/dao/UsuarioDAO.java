package com.grupo7.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.grupo7.config.ClaveUtil;
import com.grupo7.modelos.Bibliotecario;
import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Usuario;

/**
 * Clase UsuarioDAO maneja las operaciones de autenticacion y consulta de usuarios.
 * Aqui vive el CRUD de usuarios que pide el RF-02 y el RF-03.
 */
public class UsuarioDAO {

    // Validar el acceso de un usuario por su codigo institucional
    public Usuario buscarPorCodigo(String codigo, Connection con) {
        String sql = "SELECT u.id_usuario, u.codigo, u.dni, u.nombre, u.correo, " +
                     "u.contrasena, u.estado, e.carrera, e.sancionado, b.rol " +
                     "FROM usuario u " +
                     "LEFT JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                     "LEFT JOIN bibliotecario b ON u.id_usuario = b.id_usuario " +
                     "WHERE u.codigo = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener la lista completa de usuarios para cargarla en la tabla de la ventana
    public List<Usuario> listarTodos(Connection con) {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.codigo, u.dni, u.nombre, u.correo, " +
                     "u.contrasena, u.estado, e.carrera, e.sancionado, b.rol " +
                     "FROM usuario u " +
                     "LEFT JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                     "LEFT JOIN bibliotecario b ON u.id_usuario = b.id_usuario " +
                     "ORDER BY u.codigo";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leerUsuario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Registrar un usuario nuevo (RF-02)
    // El rol decide en que tabla de detalle se guarda: estudiante o bibliotecario
    public boolean insertar(Usuario usuario, Connection con) {
        String sql = "INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getCodigo());
            ps.setString(2, usuario.getDni());
            ps.setString(3, usuario.getNombre());
            ps.setString(4, usuario.getCorreo());
            // el RNF-02 pide guardar la huella, nunca la contrasena en texto plano
            ps.setString(5, ClaveUtil.encriptar(usuario.getContrasena()));
            ps.setString(6, usuario.getEstado());

            if (ps.executeUpdate() == 0) {
                return false;
            }

            // recuperamos el id que generó la base de datos
            int idUsuario;
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    return false;
                }
                idUsuario = claves.getInt(1);
            }

            // ahora guardamos el detalle segun el tipo de usuario
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                String detalle = "INSERT INTO estudiante (id_usuario, carrera, sancionado) " +
                                 "VALUES (?, ?, ?)";
                try (PreparedStatement psDetalle = con.prepareStatement(detalle)) {
                    psDetalle.setInt(1, idUsuario);
                    psDetalle.setString(2, estudiante.getCarrera());
                    psDetalle.setBoolean(3, estudiante.isSancionado());
                    psDetalle.executeUpdate();
                }
            } else {
                Bibliotecario bibliotecario = (Bibliotecario) usuario;
                String detalle = "INSERT INTO bibliotecario (id_usuario, rol) VALUES (?, ?)";
                try (PreparedStatement psDetalle = con.prepareStatement(detalle)) {
                    psDetalle.setInt(1, idUsuario);
                    psDetalle.setString(2, bibliotecario.getRol());
                    psDetalle.executeUpdate();
                }
            }
            return true;

        } catch (SQLException e) {
            // el RF-02 pide avisar cuando el codigo, dni o correo ya existen
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // Revisa si el codigo, el dni o el correo ya estan usados (RF-02)
    public boolean existeDuplicado(String codigo, String dni, String correo, int idIgnorado, Connection con) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE (codigo = ? OR dni = ? OR correo = ?) " +
                     "AND id_usuario <> ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, dni);
            ps.setString(3, correo);
            ps.setInt(4, idIgnorado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    // Cambiar el estado de un usuario entre ACTIVO e INHABILITADO (RF-03)
    public boolean actualizarEstado(int idUsuario, String nuevoEstado, Connection con) {
        String sql = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Marcar o quitar la marca de sancionado en la tabla estudiante
    public boolean actualizarSancionado(int idUsuario, boolean sancionado, Connection con) {
        String sql = "UPDATE estudiante SET sancionado = ? WHERE id_usuario = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, sancionado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Arma el objeto Estudiante o Bibliotecario segun la tabla de detalle
    private Usuario leerUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String codigo = rs.getString("codigo");
        String dni = rs.getString("dni");
        String nombre = rs.getString("nombre");
        String correo = rs.getString("correo");
        String contrasena = rs.getString("contrasena");
        String estado = rs.getString("estado");

        // si tiene fila en estudiante es estudiante, si no es bibliotecario
        if (rs.getString("carrera") != null) {
            return new Estudiante(id, codigo, dni, nombre, correo, contrasena, estado,
                    rs.getString("carrera"), rs.getBoolean("sancionado"));
        }
        return new Bibliotecario(id, codigo, dni, nombre, correo, contrasena, estado,
                rs.getString("rol"));
    }
}
