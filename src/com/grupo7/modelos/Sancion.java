package com.grupo7.modelos;

/**
 * Clase Sancion que calcula las penalizaciones por devoluciones fuera de fecha.
 */
public class Sancion {
    private int idSancion;
    private int diasRetraso;
    private double montoMulta;
    private boolean pagada; // el HU-06 pide ver las multas pendientes
    private Prestamo prestamo;

    // Constructor
    public Sancion(int idSancion, int diasRetraso, double montoMulta, boolean pagada, Prestamo prestamo) {
        if (prestamo == null) {
            throw new IllegalArgumentException("La sanción debe estar vinculada a un préstamo válido.");
        }
        this.idSancion = idSancion;
        this.diasRetraso = Math.max(diasRetraso, 0);
        this.montoMulta = montoMulta;
        this.pagada = pagada;
        this.prestamo = prestamo;
    }

    // Calcula el monto a pagar según la tarifa de cada día de retraso
    public static double calcularMulta(int diasRetraso, double tarifaPorDia) {
        return Math.max(diasRetraso, 0) * tarifaPorDia;
    }

    // Getters y Setters
    public int getIdSancion() {
        return idSancion;
    }

    public void setIdSancion(int idSancion) {
        this.idSancion = idSancion;
    }

    public int getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(int diasRetraso) {
        this.diasRetraso = Math.max(diasRetraso, 0);
    }

    public double getMontoMulta() {
        return montoMulta;
    }

    public void setMontoMulta(double montoMulta) {
        this.montoMulta = montoMulta;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }
}
