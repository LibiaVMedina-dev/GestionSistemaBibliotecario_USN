package com.grupo7.controladores;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import com.grupo7.config.ConexionDB;
import com.grupo7.dao.LibroDAO;
import com.grupo7.dao.PrestamoDAO;
import com.grupo7.dao.SancionDAO;
import com.grupo7.dao.UsuarioDAO;
import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Libro;
import com.grupo7.modelos.Prestamo;
import com.grupo7.modelos.Sancion;
import com.grupo7.modelos.Usuario;

/** Prepara los datos operativos que consumen las vistas de reportes. */
public final class ReporteController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final SancionDAO sancionDAO = new SancionDAO();
    private final PrestamoController prestamoController = new PrestamoController();

    public List<Sancion> listarMultasPendientes() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return sancionDAO.listarPendientes(con);
        }
    }

    public List<Prestamo> listarPrestamos(LocalDate desde, LocalDate hasta) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return prestamoDAO.listarPorRango(desde, hasta, con);
        }
    }

    public List<Libro> listarStockBajo(int umbral) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return libroDAO.listar(con).stream()
                .filter(libro -> libro.getStock() <= umbral)
                .toList();
        }
    }

    public String generarResumen() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            List<Usuario> usuarios = usuarioDAO.listarTodos(con);
            List<Libro> libros = libroDAO.listar(con);
            List<Prestamo> prestamos = prestamoDAO.listar(con);
            List<Sancion> sanciones = sancionDAO.listar(con);

            int ejemplares = libros.stream().mapToInt(Libro::getStock).sum();
            long sinStock = libros.stream().filter(libro -> libro.getStock() == 0).count();
            long pendientes = prestamos.stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()))
                .filter(p -> !p.getFechaLimite().isBefore(LocalDate.now()))
                .count();
            long vencidos = prestamos.stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()))
                .filter(p -> p.getFechaLimite().isBefore(LocalDate.now()))
                .count();
            long devueltos = prestamos.stream()
                .filter(p -> "DEVUELTO".equals(p.getEstado()))
                .count();
            long sancionados = usuarios.stream()
                .filter(Estudiante.class::isInstance)
                .map(Estudiante.class::cast)
                .filter(Estudiante::isSancionado)
                .count();
            double deuda = sanciones.stream()
                .filter(sancion -> !sancion.isPagada())
                .mapToDouble(Sancion::getMontoMulta)
                .sum();

            return """
                REPORTE DE LA BIBLIOTECA UNIVERSIDAD SUPERIOR NOVA
                Fecha: %s

                USUARIOS
                  Total de usuarios      : %d
                  Estudiantes sancionados: %d

                CATÁLOGO DE LIBROS
                  Títulos registrados    : %d
                  Ejemplares disponibles : %d
                  Títulos sin ejemplares : %d

                PRÉSTAMOS
                  Total registrados : %d
                  Pendientes         : %d
                  Vencidos           : %d
                  Devueltos          : %d

                SANCIONES
                  Multas registradas : %d
                  Deuda total        : S/ %.2f
                """.formatted(
                    LocalDate.now(), usuarios.size(), sancionados, libros.size(), ejemplares,
                    sinStock, prestamos.size(), pendientes, vencidos, devueltos,
                    sanciones.size(), deuda
                );
        }
    }

    public ResultadoOperacion pagarSancion(Sancion sancion) throws Exception {
        return prestamoController.pagarSancion(sancion);
    }
}
