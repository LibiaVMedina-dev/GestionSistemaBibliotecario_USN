package com.grupo7.modelos;

/**
 * Clase Libro representa los ejemplares bibliográficos.
 * Incluye el año y la categoría que pide el RF-04.
 */
public class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private int anio;
    private String categoria;
    private int stock;

    // Constructor
    public Libro(String isbn, String titulo, String autor, int anio, String categoria, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo.");
        }
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
        this.categoria = categoria;
        this.stock = stock;
    }

    // Getters y Setters
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock;
    }

    // Se usa para que el JComboBox muestre el titulo y los ejemplares disponibles
    @Override
    public String toString() {
        return titulo + " (" + stock + " ejemplares)";
    }
}
