package com.grupo7.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Libro;
import com.grupo7.modelos.Prestamo;

/**
 * Clase PrestamoDAO maneja las operaciones de la tabla 'prestamo' en la BD.
 */
public class PrestamoDAO {

    // Registrar un nuevo préstamo
    public boolean insertar(Prestamo prestamo, Connection con) {
        String sql = "INSERT INTO prestamo (id_usuario, isbn, fecha_salida, fecha_limite, estado) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamo.getEstudiante().getIdUsuario());
            ps.setString(2, prestamo.getLibro().getIsbn());
            ps.setDate(3, Date.valueOf(prestamo.getFechaSalida()));
            ps.setDate(4, Date.valueOf(prestamo.getFechaLimite()));
            ps.setString(5, prestamo.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar estado del préstamo y guardar la fecha real de devolución (RF-08)
    public boolean finalizar(int idPrestamo, String nuevoEstado, Connection con) {
        String sql = "UPDATE prestamo SET estado = ?, fecha_devolucion = ? " +
                     "WHERE id_prestamo = ? AND estado = 'PENDIENTE'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            // el RF-08 pide verificar la fecha acordada, asi que la guardamos
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener historial de préstamos
    public List<Prestamo> listar(Connection con) {
        return consultar(con, null, null);
    }

    // Obtener los prestamos hechos dentro de un rango de fechas (RF-10 y HU-06)
    public List<Prestamo> listarPorRango(LocalDate desde, LocalDate hasta, Connection con) {
        return consultar(con, desde, hasta);
    }

    // Consulta los prestamos. Si desde y hasta vienen null trae todo.
    private List<Prestamo> consultar(Connection con, LocalDate desde, LocalDate hasta) {
        List<Prestamo> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT p.id_prestamo, p.fecha_salida, p.fecha_limite, p.fecha_devolucion, p.estado, " +
                "u.id_usuario, u.codigo, u.dni, u.nombre, u.correo, u.contrasena, " +
                "u.estado AS estado_usr, e.carrera, e.sancionado, " +
                "l.isbn, l.titulo, l.autor, l.anio, l.categoria, l.stock " +
                "FROM prestamo p " +
                "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
                "INNER JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "INNER JOIN libro l ON p.isbn = l.isbn ");

        // el filtro de fechas solo se agrega si el usuario eligio un rango
        if (desde != null && hasta != null) {
            sql.append("WHERE p.fecha_salida BETWEEN ? AND ? ");
        }
        sql.append("ORDER BY p.fecha_salida DESC, p.id_prestamo DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (desde != null && hasta != null) {
                ps.setDate(1, Date.valueOf(desde));
                ps.setDate(2, Date.valueOf(hasta));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Arma el objeto Prestamo con los datos de la consulta
    private Prestamo leerPrestamo(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante(
                rs.getInt("id_usuario"),
                rs.getString("codigo"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("contrasena"),
                rs.getString("estado_usr"),
                rs.getString("carrera"),
                rs.getBoolean("sancionado")
        );

        Libro libro = new Libro(
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio"),
                rs.getString("categoria"),
                rs.getInt("stock")
        );

        LocalDate fechaSalida = rs.getDate("fecha_salida").toLocalDate();
        LocalDate fechaLimite = rs.getDate("fecha_limite").toLocalDate();

        // calculamos los días del préstamo a partir de las fechas
        int dias = (int) java.time.temporal.ChronoUnit.DAYS.between(fechaSalida, fechaLimite);

        Prestamo prestamo = new Prestamo(
                rs.getInt("id_prestamo"),
                fechaSalida,
                dias,
                estudiante,
                libro
        );

        prestamo.setEstado(rs.getString("estado"));

        // la fecha de devolucion puede venir nula si el prestamo sigue pendiente
        Date devolucion = rs.getDate("fecha_devolucion");
        if (devolucion != null) {
            prestamo.setFechaDevolucion(devolucion.toLocalDate());
        }
        return prestamo;
    }
}
