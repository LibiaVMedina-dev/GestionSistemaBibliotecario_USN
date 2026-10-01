package com.grupo7.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

/**
 * Crea conexiones JDBC usando variables de entorno o un archivo properties.
 *
 * Debe existir config/database.properties para poder funcionar!!!
 */
public final class ConexionDB {
    private static final Path ARCHIVO_CONFIGURACION = Path.of(
        System.getenv().getOrDefault("DB_CONFIG_FILE", "config/database.properties")
    );
    private static final Properties CONFIGURACION = cargarConfiguracion();

    private ConexionDB() {}

    public static Connection obtenerConexion() throws SQLException {
        String usuario = valor("DB_USER", "db.user", "user");
        String clave = valor("DB_PASSWORD", "db.password", "pass");
        String url = valor("DB_URL", "db.url", null);

        if (url == null || url.isBlank()) {
            url = construirUrl();
        }

        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new SQLException(
                "No se pudo conectar a la base de datos en " + url
                    + ". Verifica la red, las credenciales y el driver JDBC.",
                e
            );
        }
    }

    private static String construirUrl() {
        String vendor = valor("DB_VENDOR", "db.vendor", "mariadb")
            .toLowerCase(Locale.ROOT);
        if (!vendor.equals("mariadb") && !vendor.equals("mysql")) {
            throw new IllegalStateException(
                "db.vendor debe ser 'mariadb' o 'mysql', pero se recibió: " + vendor
            );
        }

        String host = valor("DB_HOST", "db.host", "161.132.37.119");
        String puerto = valor("DB_PORT", "db.port", "1724");
        String baseDatos = valor("DB_NAME", "db.name", "PageTurner");

        validarPuerto(puerto);
        return "jdbc:" + vendor + "://" + host + ":" + puerto + "/" + baseDatos;
    }

    private static void validarPuerto(String puerto) {
        try {
            int valorPuerto = Integer.parseInt(puerto);
            if (valorPuerto < 1 || valorPuerto > 65_535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Puerto de base de datos inválido: " + puerto, e);
        }
    }

    private static String valor(
        String nombreEntorno,
        String nombrePropiedad,
        String valorPredeterminado
    ) {
        String valorEntorno = System.getenv(nombreEntorno);
        if (valorEntorno != null && !valorEntorno.isBlank()) {
            return valorEntorno.trim();
        }

        String valorSistema = System.getProperty(nombrePropiedad);
        if (valorSistema != null && !valorSistema.isBlank()) {
            return valorSistema.trim();
        }

        String valorArchivo = CONFIGURACION.getProperty(nombrePropiedad);
        if (valorArchivo != null && !valorArchivo.isBlank()) {
            return valorArchivo.trim();
        }

        return valorPredeterminado;
    }

    private static Properties cargarConfiguracion() {
        Properties propiedades = new Properties();
        if (!Files.exists(ARCHIVO_CONFIGURACION)) {
            return propiedades;
        }

        try (Reader reader = Files.newBufferedReader(
            ARCHIVO_CONFIGURACION,
            StandardCharsets.UTF_8
        )) {
            propiedades.load(reader);
            return propiedades;
        } catch (IOException e) {
            throw new IllegalStateException(
                "No se pudo leer " + ARCHIVO_CONFIGURACION.toAbsolutePath(),
                e
            );
        }
    }
}
