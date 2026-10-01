package com.grupo7.modelos;

import java.time.LocalDate;

/**
 * Clase Prestamo gestiona las transacciones de salida y limite de devolucion de un libro.
 * Utiliza LocalDate para el manejo moderno de fechas en Java.
 */
public class Prestamo {
    private int idPrestamo;
    private LocalDate fechaSalida;
    private LocalDate fechaLimite;
    private LocalDate fechaDevolucion; // queda en null si el prestamo sigue pendiente
    private String estado; // "PENDIENTE", "DEVUELTO", "VENCIDO"
    private Estudiante estudiante;
    private Libro libro;

    // Constructor 
    public Prestamo(int idPrestamo, LocalDate fechaSalida, int diasPrestamo, Estudiante estudiante, Libro libro) {
        if (estudiante == null || libro == null) {
            throw new IllegalArgumentException("El préstamo requiere un estudiante y un libro válidos.");
        }
        this.idPrestamo = idPrestamo;
        this.fechaSalida = (fechaSalida != null) ? fechaSalida : LocalDate.now();
        this.fechaLimite = this.fechaSalida.plusDays(diasPrestamo);
        this.estado = "PENDIENTE";
        this.estudiante = estudiante;
        this.libro = libro;
    }

    // Getters y Setters
    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    // El RF-09 pide contar los días de mora de un préstamo
    // Si la devolución aún no ocurrió usamos la fecha de hoy
    public int getDiasDeRetraso() {
        LocalDate fin = (fechaDevolucion != null) ? fechaDevolucion : LocalDate.now();
        if (!fin.isAfter(fechaLimite)) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaLimite, fin);
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }
}
