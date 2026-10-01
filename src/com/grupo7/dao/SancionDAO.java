package com.grupo7.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.grupo7.modelos.Prestamo;
import com.grupo7.modelos.Sancion;

/**
 * Clase SancionDAO maneja el cálculo y guardado de las multas por mora.
 *
 * El RF-09 pide cobrar un monto por cada día de retraso y el HU-05
 * pide que esa multa se calcule sola al registrar la devolución.
 */
public class SancionDAO {

    // Tarifa por cada día de retraso que define la universidad
    public static final double TARIFA_POR_DIA = 5.00;

    // Guardar la sanción de un préstamo devuelto fuera de plazo
    public boolean insertar(Sancion sancion, Connection con) {
        String sql = "INSERT INTO sancion (id_prestamo, dias_retraso, monto_multa, pagada) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sancion.getPrestamo().getIdPrestamo());
            ps.setInt(2, sancion.getDiasRetraso());
            ps.setBigDecimal(3, BigDecimal.valueOf(sancion.getMontoMulta()));
            ps.setBoolean(4, sancion.isPagada());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // la tabla tiene una restriccion unica, un prestamo solo genera una sancion
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // Calcular la multa de un préstamo a partir de sus días de retraso
    public Sancion calcular(Prestamo prestamo) {
        int dias = prestamo.getDiasDeRetraso();
        double monto = Sancion.calcularMulta(dias, TARIFA_POR_DIA);
        return new Sancion(0, dias, monto, false, prestamo);
    }

    // Marcar una sanción como pagada para que salga del reporte de deudas
    public boolean marcarPagada(int idSancion, Connection con) {
        String sql = "UPDATE sancion SET pagada = TRUE " +
                     "WHERE id_sancion = ? AND pagada = FALSE";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSancion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Listar las sanciones de todos los préstamos
    public List<Sancion> listar(Connection con) {
        return consultar(con, false);
    }

    // Listar solo las multas que todavía no se pagaron (HU-06 criterio 1)
    public List<Sancion> listarPendientes(Connection con) {
        return consultar(con, true);
    }

    // Consulta las sanciones agregando los datos del estudiante y del libro
    private List<Sancion> consultar(Connection con, boolean soloPendientes) {
        List<Sancion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT s.id_sancion, s.dias_retraso, s.monto_multa, s.pagada, " +
                "p.id_prestamo, p.fecha_salida, p.fecha_limite, p.estado, " +
                "u.id_usuario, u.codigo, u.dni, u.nombre, u.correo, u.contrasena, " +
                "u.estado AS estado_usr, e.carrera, e.sancionado, " +
                "l.isbn, l.titulo, l.autor, l.anio, l.categoria, l.stock " +
                "FROM sancion s " +
                "INNER JOIN prestamo p ON s.id_prestamo = p.id_prestamo " +
                "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
                "INNER JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "INNER JOIN libro l ON p.isbn = l.isbn ");

        if (soloPendientes) {
            sql.append("WHERE s.pagada = FALSE ");
        }
        sql.append("ORDER BY s.monto_multa DESC");

        try (PreparedStatement ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(leerSancion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Arma el objeto Sancion con los datos de la consulta
    private Sancion leerSancion(ResultSet rs) throws SQLException {
        // el Constructor calcula solo el monto, aqui se pasa la tarifa 1
        // porque el monto real ya viene guardado en la base de datos
        Prestamo prestamo = new Prestamo(
                rs.getInt("id_prestamo"),
                rs.getDate("fecha_salida").toLocalDate(),
                (int) java.time.temporal.ChronoUnit.DAYS.between(
                        rs.getDate("fecha_salida").toLocalDate(),
                        rs.getDate("fecha_limite").toLocalDate()),
                new com.grupo7.modelos.Estudiante(
                        rs.getInt("id_usuario"),
                        rs.getString("codigo"),
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("contrasena"),
                        rs.getString("estado_usr"),
                        rs.getString("carrera"),
                        rs.getBoolean("sancionado")
                ),
                new com.grupo7.modelos.Libro(
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("anio"),
                        rs.getString("categoria"),
                        rs.getInt("stock")
                )
        );

        prestamo.setEstado(rs.getString("estado"));

        Sancion sancion = new Sancion(
                rs.getInt("id_sancion"),
                rs.getInt("dias_retraso"),
                rs.getDouble("monto_multa"),
                rs.getBoolean("pagada"),
                prestamo
        );
        return sancion;
    }
}
