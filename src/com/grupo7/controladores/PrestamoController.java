package com.grupo7.controladores;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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

/** Reúne las reglas y transacciones del módulo de circulación. */
public final class PrestamoController {
    public static final int DIAS_PRESTAMO = 3;
    public static final double TARIFA_POR_DIA = SancionDAO.TARIFA_POR_DIA;

    public record DatosPrestamo(List<Estudiante> estudiantes, List<Libro> libros) {}

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final SancionDAO sancionDAO = new SancionDAO();

    public DatosPrestamo cargarDatos() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            List<Estudiante> estudiantes = new ArrayList<>();
            for (Usuario usuario : usuarioDAO.listarTodos(con)) {
                if (usuario instanceof Estudiante estudiante) {
                    estudiantes.add(estudiante);
                }
            }
            return new DatosPrestamo(estudiantes, libroDAO.listar(con));
        }
    }

    public List<Prestamo> listarPrestamos() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return prestamoDAO.listar(con);
        }
    }

    public ResultadoOperacion registrarPrestamo(Estudiante seleccionado, Libro libroSeleccionado)
            throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Usuario usuarioActual = usuarioDAO.buscarPorCodigo(seleccionado.getCodigo(), con);
                if (!(usuarioActual instanceof Estudiante estudiante)) {
                    return cancelar(con, "El estudiante ya no está registrado.");
                }

                ResultadoOperacion elegibilidad = validarElegibilidad(estudiante, con);
                if (!elegibilidad.exitoso()) {
                    return cancelar(con, elegibilidad.mensaje());
                }

                Libro libroActual = libroDAO.buscarPorIsbn(libroSeleccionado.getIsbn(), con);
                if (libroActual == null) {
                    return cancelar(con, "El libro ya no está registrado.");
                }
                if (libroActual.getStock() <= 0) {
                    return cancelar(con, "El libro no tiene ejemplares disponibles.");
                }

                for (Prestamo prestamo : prestamoDAO.listar(con)) {
                    if (mismoEstudiante(prestamo, estudiante)
                            && "PENDIENTE".equals(prestamo.getEstado())
                            && libroActual.getIsbn().equals(prestamo.getLibro().getIsbn())) {
                        return cancelar(con, "El estudiante ya tiene pendiente este mismo libro.");
                    }
                }

                Prestamo nuevo = new Prestamo(
                    0, LocalDate.now(), DIAS_PRESTAMO, estudiante, libroActual
                );
                if (!prestamoDAO.insertar(nuevo, con)
                        || !libroDAO.ajustarStock(libroActual.getIsbn(), -1, con)) {
                    throw new SQLException("No se pudo registrar íntegramente el préstamo.");
                }

                con.commit();
                return ResultadoOperacion.exito(
                    "Préstamo registrado. Debe devolverse antes del " + nuevo.getFechaLimite() + "."
                );
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public ResultadoOperacion devolverLibro(int idPrestamo) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Prestamo prestamo = buscarPrestamo(idPrestamo, prestamoDAO.listar(con));
                if (prestamo == null) {
                    return cancelar(con, "El préstamo ya no existe.");
                }
                if (!"PENDIENTE".equals(prestamo.getEstado())) {
                    return cancelar(con, "El préstamo ya fue cerrado.");
                }

                int diasRetraso = prestamo.getDiasDeRetraso();
                if (!prestamoDAO.finalizar(idPrestamo, "DEVUELTO", con)
                        || !libroDAO.ajustarStock(prestamo.getLibro().getIsbn(), 1, con)) {
                    throw new SQLException("No se pudo registrar íntegramente la devolución.");
                }

                Sancion sancion = buscarSancionDelPrestamo(idPrestamo, sancionDAO.listar(con));
                if (diasRetraso > 0) {
                    if (sancion == null) {
                        sancion = sancionDAO.calcular(prestamo);
                        if (!sancionDAO.insertar(sancion, con)) {
                            throw new SQLException("No se pudo generar la multa por mora.");
                        }
                    }
                }

                boolean conservaBloqueo = sincronizarBloqueoEstudiante(
                    prestamo.getEstudiante(), con
                );

                con.commit();
                if (diasRetraso > 0) {
                    return ResultadoOperacion.exito(
                        "Devolución registrada con " + diasRetraso + " día(s) de retraso. "
                            + (conservaBloqueo
                                ? "El estudiante permanece bloqueado hasta pagar sus multas."
                                : "La multa ya estaba pagada y el estudiante volvió a estar habilitado.")
                    );
                }
                return ResultadoOperacion.exito(
                    "Devolución registrada dentro del plazo. No genera multa."
                );
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public ResultadoOperacion pagarSancion(Sancion sancion) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Prestamo prestamoActual = buscarPrestamo(
                    sancion.getPrestamo().getIdPrestamo(), prestamoDAO.listar(con)
                );
                if (prestamoActual == null) {
                    return cancelar(con, "El préstamo asociado a la multa ya no existe.");
                }
                if (!"DEVUELTO".equals(prestamoActual.getEstado())
                        || prestamoActual.getFechaDevolucion() == null) {
                    return cancelar(con,
                        "Primero registra la devolución del libro antes de pagar la multa."
                    );
                }
                if (sancion.isPagada()) {
                    return cancelar(con, "La multa ya figura como pagada.");
                }
                if (!sancionDAO.marcarPagada(sancion.getIdSancion(), con)) {
                    throw new SQLException("No se pudo registrar el pago de la multa.");
                }

                Estudiante estudiante = prestamoActual.getEstudiante();
                boolean conservaBloqueo = sincronizarBloqueoEstudiante(estudiante, con);

                con.commit();
                return ResultadoOperacion.exito(
                    conservaBloqueo
                        ? "Pago registrado. El estudiante conserva otro bloqueo pendiente."
                        : "Pago registrado. El estudiante volvió a estar habilitado."
                );
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public ResultadoOperacion validarElegibilidad(Estudiante estudiante, Connection con) {
        if (!"ACTIVO".equals(estudiante.getEstado())) {
            return ResultadoOperacion.error(
                "El estudiante está " + estudiante.getEstado() + " y no puede recibir préstamos."
            );
        }
        return validarObligacionesPendientes(estudiante, con);
    }

    public ResultadoOperacion validarObligacionesPendientes(
            Estudiante estudiante,
            Connection con) {
        if (estudiante.isSancionado()) {
            return ResultadoOperacion.error("El estudiante tiene una sanción activa.");
        }
        if (tieneMultaPendiente(estudiante, con)) {
            return ResultadoOperacion.error("El estudiante tiene una multa pendiente de pago.");
        }
        if (tienePrestamoVencido(estudiante, con)) {
            return ResultadoOperacion.error("El estudiante está en mora por un préstamo vencido.");
        }
        return ResultadoOperacion.exito("Estudiante habilitado.");
    }

    private boolean tieneMultaPendiente(Estudiante estudiante, Connection con) {
        for (Sancion sancion : sancionDAO.listarPendientes(con)) {
            if (mismoEstudiante(sancion.getPrestamo(), estudiante)) {
                return true;
            }
        }
        return false;
    }

    private boolean tienePrestamoVencido(Estudiante estudiante, Connection con) {
        for (Prestamo prestamo : prestamoDAO.listar(con)) {
            if (mismoEstudiante(prestamo, estudiante)
                    && "PENDIENTE".equals(prestamo.getEstado())
                    && prestamo.getFechaLimite().isBefore(LocalDate.now())) {
                return true;
            }
        }
        return false;
    }

    private void marcarEstudianteSancionado(Estudiante estudiante, Connection con)
            throws SQLException {
        if (!"SANCIONADO".equals(estudiante.getEstado())
                && !"INHABILITADO".equals(estudiante.getEstado())
                && !usuarioDAO.actualizarEstado(estudiante.getIdUsuario(), "SANCIONADO", con)) {
            throw new SQLException("No se pudo actualizar el estado del estudiante.");
        }
        if (!estudiante.isSancionado()
                && !usuarioDAO.actualizarSancionado(estudiante.getIdUsuario(), true, con)) {
            throw new SQLException("No se pudo marcar la sanción del estudiante.");
        }
    }

    /** Sincroniza el estado administrativo con las obligaciones reales en la BD. */
    private boolean sincronizarBloqueoEstudiante(Estudiante estudiante, Connection con)
            throws SQLException {
        boolean conservaBloqueo = tieneMultaPendiente(estudiante, con)
            || tienePrestamoVencido(estudiante, con);

        Usuario actual = usuarioDAO.buscarPorCodigo(estudiante.getCodigo(), con);
        if (!(actual instanceof Estudiante estudianteActual)) {
            throw new SQLException("No se encontró al estudiante asociado al préstamo.");
        }

        if (conservaBloqueo) {
            marcarEstudianteSancionado(estudianteActual, con);
            return true;
        }

        if (!"INHABILITADO".equals(estudianteActual.getEstado())
                && !"ACTIVO".equals(estudianteActual.getEstado())
                && !usuarioDAO.actualizarEstado(
                    estudianteActual.getIdUsuario(), "ACTIVO", con)) {
            throw new SQLException("No se pudo reactivar al estudiante.");
        }
        if (estudianteActual.isSancionado()
                && !usuarioDAO.actualizarSancionado(
                    estudianteActual.getIdUsuario(), false, con)) {
            throw new SQLException("No se pudo retirar la sanción del estudiante.");
        }
        return false;
    }

    private boolean mismoEstudiante(Prestamo prestamo, Estudiante estudiante) {
        return prestamo.getEstudiante().getIdUsuario() == estudiante.getIdUsuario();
    }

    private Prestamo buscarPrestamo(int idPrestamo, List<Prestamo> prestamos) {
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getIdPrestamo() == idPrestamo) return prestamo;
        }
        return null;
    }

    private Sancion buscarSancionDelPrestamo(int idPrestamo, List<Sancion> sanciones) {
        for (Sancion sancion : sanciones) {
            if (sancion.getPrestamo().getIdPrestamo() == idPrestamo) return sancion;
        }
        return null;
    }

    private ResultadoOperacion cancelar(Connection con, String mensaje) throws SQLException {
        con.rollback();
        return ResultadoOperacion.error(mensaje);
    }
}
