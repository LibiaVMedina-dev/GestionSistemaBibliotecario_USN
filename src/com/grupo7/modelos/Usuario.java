package com.grupo7.modelos;

/**
 * Clase abstracta Usuario
 * Representa la entidad base para Estudiante y Bibliotecario.
 *
 * Los atributos siguen lo que pide el documento del proyecto:
 * codigo institucional, DNI, nombre, correo, contrasena y estado.
 */
public abstract class Usuario {
    private int idUsuario;
    private String codigo;
    private String dni;
    private String nombre;
    private String correo;
    private String contrasena; // siempre guardada como huella SHA-256
    private String estado; // "ACTIVO", "INHABILITADO", "SANCIONADO"

    // Constructor
    public Usuario(int idUsuario, String codigo, String dni, String nombre, String correo,
                   String contrasena, String estado) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código institucional no puede estar vacío.");
        }
        this.idUsuario = idUsuario;
        this.codigo = codigo;
        this.dni = dni;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.estado = (estado != null && !estado.trim().isEmpty()) ? estado : "ACTIVO";
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede ser nulo o vacío.");
        }
        this.codigo = codigo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Método de autenticación básica (RF-01)
    // El RF-01 y el HU-01 piden validar código institucional Y contraseña
    public boolean login(String codigoIngresado, String contrasenaIngresada) {
        if (!this.codigo.equalsIgnoreCase(codigoIngresado)) {
            return false;
        }
        return com.grupo7.config.ClaveUtil.verificar(contrasenaIngresada, this.contrasena);
    }
}
