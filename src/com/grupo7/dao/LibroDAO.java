package com.grupo7.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.grupo7.modelos.Libro;

/**
 * Clase LibroDAO maneja las operaciones CRUD de la tabla 'libro' en la BD.
 * Utiliza PreparedStatement para evitar inyección SQL.
 */
public class LibroDAO {

    // Registrar un nuevo libro en la base de datos (RF-04)
    public boolean insertar(Libro libro, Connection con) {
        String sql = "INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setInt(4, libro.getAnio());
            ps.setString(5, libro.getCategoria());
            ps.setInt(6, libro.getStock());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // el HU-02 pide avisar cuando el ISBN ya esta registrado
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // Buscar un libro por su código ISBN
    public Libro buscarPorIsbn(String isbn, Connection con) {
        String sql = "SELECT * FROM libro WHERE isbn = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerLibro(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Actualizar los datos de un libro ya registrado
    public boolean actualizar(Libro libro, Connection con) {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, anio = ?, categoria = ?, stock = ? " +
                     "WHERE isbn = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setInt(3, libro.getAnio());
            ps.setString(4, libro.getCategoria());
            ps.setInt(5, libro.getStock());
            ps.setString(6, libro.getIsbn());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Ajuste atómico: evita perder actualizaciones y nunca permite stock negativo.
    public boolean ajustarStock(String isbn, int cantidad, Connection con) {
        String sql = "UPDATE libro SET stock = stock + ? " +
                     "WHERE isbn = ? AND stock + ? >= 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, isbn);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener la lista completa de libros registrados
    public List<Libro> listar(Connection con) {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT * FROM libro ORDER BY titulo";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leerLibro(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Buscar libros por titulo, autor, categoria o ISBN (RF-05 y HU-03)
    // Si el texto va vacio devuelve todos los libros
    public List<Libro> buscar(String texto, Connection con) {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT * FROM libro " +
                     "WHERE titulo LIKE ? OR autor LIKE ? OR categoria LIKE ? OR isbn LIKE ? " +
                     "ORDER BY titulo";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            // agregamos los % para que el LIKE busque coincidencias parciales
            String patron = "%" + texto.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerLibro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Arma el objeto Libro con los datos que trae la consulta
    private Libro leerLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio"),
                rs.getString("categoria"),
                rs.getInt("stock")
        );
    }
}
