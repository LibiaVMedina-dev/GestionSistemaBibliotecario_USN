package com.grupo7.modelos;

/**
 * Clase Bibliotecario que hereda de Usuario.
 * Representa al personal encargado de la gestión de préstamos e inventario.
 */
public class Bibliotecario extends Usuario {
    private String rol; // "ADMINISTRADOR" o "BIBLIOTECARIO"

    // Constructor
    public Bibliotecario(int idUsuario, String codigo, String dni, String nombre, String correo,
                         String contrasena, String estado, String rol) {
        super(idUsuario, codigo, dni, nombre, correo, contrasena, estado);
        this.rol = (rol != null && !rol.trim().isEmpty()) ? rol : "BIBLIOTECARIO";
    }

    // Getters y Setters
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    // El RF-01 restringe el acceso segun el rol del bibliotecario
    public boolean esAdministrador() {
        return "ADMINISTRADOR".equals(rol);
    }

    // Se usa para que el JComboBox muestre el codigo, el nombre y el rol
    @Override
    public String toString() {
        return getCodigo() + " - " + getNombre() + " (" + rol + ")";
    }
}
