import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.grupo7.config.ConexionDB;

/**
 * Health check mínimo para comprobar la conexión JDBC con PageTurner.
 */
public final class PruebaBD {
    private static final String CONSULTA = """
        SELECT DATABASE() AS base_datos,
                VERSION() AS version_servidor,
                CURRENT_TIMESTAMP AS fecha_servidor
        """;

    private PruebaBD() {
        // Clase ejecutable: no debe instanciarse.
    }

    public static void main(String[] args) {
        long inicio = System.nanoTime();

        try (Connection conexion = ConexionDB.obtenerConexion();
            PreparedStatement sentencia = conexion.prepareStatement(CONSULTA);
            ResultSet resultado = sentencia.executeQuery()) {

            if (!resultado.next()) {
                throw new SQLException("El servidor no devolvió el resultado esperado.");
            }

            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println("=== Health check PageTurner ===");
            System.out.println("Estado: OK");
            System.out.println("Motor: " + conexion.getMetaData().getDatabaseProductName());
            System.out.println("Base de datos: " + resultado.getString("base_datos"));
            System.out.println("Versión: " + resultado.getString("version_servidor"));
            System.out.println("Fecha del servidor: " + resultado.getTimestamp("fecha_servidor"));
            System.out.println("Tiempo de respuesta: " + duracionMs + " ms");
        } catch (SQLException | IllegalStateException e) {
            System.err.println("=== Health check PageTurner ===");
            System.err.println("Estado: ERROR");
            System.err.println("Detalle: " + e.getMessage());
            System.exit(1);
        }
    }
}
