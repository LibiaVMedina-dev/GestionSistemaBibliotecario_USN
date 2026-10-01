package com.grupo7.modelos;

/**
 * Clase Estudiante que hereda de Usuario.
 * Representa a los alumnos que solicitan préstamos en la biblioteca.
 */
public class Estudiante extends Usuario {
    private String carrera;
    private boolean sancionado;

    // Constructor
    public Estudiante(int idUsuario, String codigo, String dni, String nombre, String correo,
                      String contrasena, String estado, String carrera, boolean sancionado) {
        super(idUsuario, codigo, dni, nombre, correo, contrasena, estado);
        this.carrera = carrera;
        this.sancionado = sancionado;
    }

    // Getters y Setters
    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public boolean isSancionado() {
        return sancionado;
    }

    // El RF-03 pide cambiar el estado a Sancionado cuando hay multas
    public void setSancionado(boolean sancionado) {
        this.sancionado = sancionado;
        if (sancionado) {
            setEstado("SANCIONADO");
        } else {
            setEstado("ACTIVO");
        }
    }

    // Se usa para que el JComboBox muestre el codigo y el nombre del estudiante
    @Override
    public String toString() {
        return getCodigo() + " - " + getNombre();
    }
}
