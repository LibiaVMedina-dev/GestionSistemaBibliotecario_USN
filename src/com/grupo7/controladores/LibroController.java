package com.grupo7.controladores;

import java.sql.Connection;
import java.util.List;

import com.grupo7.config.ConexionDB;
import com.grupo7.dao.LibroDAO;
import com.grupo7.modelos.Libro;

/** Coordina las operaciones del catálogo sin mezclar JDBC con Swing. */
public final class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();

    public List<Libro> listar() throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return libroDAO.listar(con);
        }
    }

    public List<Libro> buscar(String texto) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return libroDAO.buscar(texto, con);
        }
    }

    public ResultadoOperacion registrar(Libro libro) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            if (libroDAO.buscarPorIsbn(libro.getIsbn(), con) != null) {
                return ResultadoOperacion.error("El código ISBN ya se encuentra registrado.");
            }
            return libroDAO.insertar(libro, con)
                ? ResultadoOperacion.exito("El libro se registró correctamente.")
                : ResultadoOperacion.error("No se pudo registrar el libro.");
        }
    }

    public ResultadoOperacion actualizar(Libro libro) throws Exception {
        try (Connection con = ConexionDB.obtenerConexion()) {
            return libroDAO.actualizar(libro, con)
                ? ResultadoOperacion.exito("Los datos del libro se actualizaron.")
                : ResultadoOperacion.error("No se pudo actualizar el libro.");
        }
    }

}
