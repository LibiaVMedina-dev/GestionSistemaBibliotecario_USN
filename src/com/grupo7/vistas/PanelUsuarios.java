package com.grupo7.vistas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.grupo7.controladores.ResultadoOperacion;
import com.grupo7.controladores.UsuarioController;
import com.grupo7.modelos.Bibliotecario;
import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Usuario;
import com.grupo7.util.TareaSwing;
import com.grupo7.util.Utilidades;

/**
 * Panel de usuarios que permite registrar, buscar y cambiar su estado.
 */
public class PanelUsuarios extends JPanel {

    // Usamos el mismo DAO que el companero uso para validar el ingreso
    private final UsuarioController controlador = new UsuarioController();

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtDni = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JTextField txtCarrera = new JTextField();
    private final JComboBox<String> comboPerfil = new JComboBox<>(new String[]{"ESTUDIANTE", "BIBLIOTECARIO"});
    private final JComboBox<String> comboRol = new JComboBox<>(new String[]{"BIBLIOTECARIO", "ADMINISTRADOR"});

    private final JTextField txtBuscar = new JTextField();
    private final JLabel lblResultado = new JLabel(" ");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnRegistrar = new JButton("Registrar usuario");
    private final JButton btnActivar = new JButton("Marcar Activo");
    private final JButton btnInactivar = new JButton("Marcar Inactivo");

    // Guardamos los usuarios cargados para no ir a la base en cada accion
    private List<Usuario> usuariosCargados;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Codigo", "DNI", "Nombre", "Correo", "Perfil", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public PanelUsuarios() {
        setLayout(new BorderLayout());
        add(crearBuscador(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);

        cargarUsuarios();
    }

    // Barra de busqueda por codigo institucional
    private JPanel crearBuscador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnTodos = new JButton("Ver todos");

        btnBuscar.addActionListener(e -> buscarUsuario());
        btnTodos.addActionListener(e -> cargarUsuarios());

        txtBuscar.setPreferredSize(new Dimension(180, 25));
        lblResultado.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(new JLabel("Buscar por codigo:"));
        panel.add(txtBuscar);
        panel.add(btnBuscar);
        panel.add(btnTodos);
        panel.add(lblResultado);

        return panel;
    }

    // Formulario a la izquierda y tabla a la derecha
    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(crearFormulario(), BorderLayout.WEST);
        centro.add(crearTabla(), BorderLayout.CENTER);
        return centro;
    }

    // Campos para dar de alta un usuario (RF-02)
    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(285, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(4, 8, 4, 8);
        r.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(panel, r, 0, "Codigo institucional:", txtCodigo);
        agregarCampo(panel, r, 2, "DNI:", txtDni);
        agregarCampo(panel, r, 4, "Nombres y apellidos:", txtNombre);
        agregarCampo(panel, r, 6, "Correo institucional:", txtCorreo);
        agregarCampo(panel, r, 8, "Contrasena:", txtContrasena);
        agregarCampo(panel, r, 10, "Carrera (solo estudiante):", txtCarrera);

        // el perfil decide si es estudiante o bibliotecario
        r.gridx = 0; r.gridy = 12; r.gridwidth = 2;
        r.anchor = GridBagConstraints.LINE_START;
        panel.add(new JLabel("Perfil:"), r);
        r.gridy = 13;
        panel.add(comboPerfil, r);

        // el rol solo aplica a los bibliotecarios
        r.gridy = 14;
        panel.add(new JLabel("Rol (solo bibliotecario):"), r);
        r.gridy = 15;
        panel.add(comboRol, r);

        // si es estudiante no tiene sentido ver el rol
        comboPerfil.addActionListener(e -> {
            boolean esBibliotecario = "BIBLIOTECARIO".equals(comboPerfil.getSelectedItem());
            comboRol.setEnabled(esBibliotecario);
            txtCarrera.setEnabled(!esBibliotecario);
        });

        JButton btnLimpiar = new JButton("Limpiar formulario");

        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnActivar.addActionListener(e -> cambiarEstado(true));
        btnInactivar.addActionListener(e -> cambiarEstado(false));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        r.gridy = 16;
        panel.add(btnRegistrar, r);
        r.gridy = 17;
        panel.add(btnActivar, r);
        r.gridy = 18;
        panel.add(btnInactivar, r);
        r.gridy = 19;
        panel.add(btnLimpiar, r);

        return panel;
    }

    // Coloca una etiqueta en una fila y el campo en la siguiente
    private void agregarCampo(JPanel panel, GridBagConstraints r, int fila, String etiqueta, JTextField campo) {
        r.gridwidth = 2;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;
        r.gridy = fila;
        if (fila == 0) {
            r.insets = new Insets(16, 8, 2, 8);
        } else {
            r.insets = new Insets(4, 8, 2, 8);
        }
        r.anchor = GridBagConstraints.LINE_START;
        panel.add(new JLabel(etiqueta), r);

        campo.setPreferredSize(new Dimension(235, 25));
        r.gridy = fila + 1;
        r.insets = new Insets(2, 8, 4, 8);
        panel.add(campo, r);
    }

    // Tabla con los usuarios registrados
    private JPanel crearTabla() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(105);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));

        // al hacer clic llenamos el formulario con los datos de la fila
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFormularioDesdeTabla();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Usuarios registrados"));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // Carga la lista completa de usuarios
    private void cargarUsuarios() {
        TareaSwing.ejecutar(this, null, "No se pudo cargar la lista de usuarios",
            controlador::listar,
            usuarios -> {
                usuariosCargados = usuarios;
                llenarTabla(usuariosCargados);
                lblResultado.setText(usuariosCargados.size() + " usuario(s) registrado(s).");
            });
    }

    // Busca un solo usuario por su codigo
    private void buscarUsuario() {
        String codigo = txtBuscar.getText().trim();

        if (codigo.isEmpty()) {
            Utilidades.error("Escribe el codigo que quieres buscar.");
            return;
        }

        TareaSwing.ejecutar(this, btnBuscar, "No se pudo buscar el usuario",
            () -> controlador.buscar(codigo),
            encontrado -> {
                if (encontrado == null) {
                    modelo.setRowCount(0);
                    lblResultado.setText("No se encontraron resultados coincidentes");
                    Utilidades.info("No se encontraron resultados coincidentes.");
                    return;
                }
                usuariosCargados = List.of(encontrado);
                llenarTabla(usuariosCargados);
                lblResultado.setText("1 resultado encontrado.");
            });
    }

    // Pasa una lista de usuarios al JTable
    private void llenarTabla(List<Usuario> usuarios) {
        modelo.setRowCount(0);

        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{
                    usuario.getCodigo(),
                    usuario.getDni(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    tipoDe(usuario),
                    usuario.getEstado()
            });
        }
    }

    // Pasa los datos de la fila seleccionada a los campos del formulario
    private void cargarFormularioDesdeTabla() {
        Usuario usuario = usuarioSeleccionado();

        if (usuario == null) {
            return;
        }

        txtCodigo.setText(usuario.getCodigo());
        txtDni.setText(usuario.getDni() == null ? "" : usuario.getDni());
        txtNombre.setText(usuario.getNombre());
        txtCorreo.setText(usuario.getCorreo());
        txtCarrera.setText("");

        boolean esBibliotecario = usuario instanceof Bibliotecario;

        comboPerfil.setSelectedItem(esBibliotecario ? "BIBLIOTECARIO" : "ESTUDIANTE");
        comboRol.setEnabled(esBibliotecario);
        txtCarrera.setEnabled(!esBibliotecario);

        if (esBibliotecario) {
            comboRol.setSelectedItem(((Bibliotecario) usuario).getRol());
        } else {
            txtCarrera.setText(((Estudiante) usuario).getCarrera());
        }
    }

    // Devuelve el usuario que esta en la fila seleccionada
    private Usuario usuarioSeleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            return null;
        }
        return usuariosCargados.get(fila);
    }

    // Valida los campos y registra el usuario (RF-02)
    private void registrarUsuario() {
        String codigo = txtCodigo.getText().trim();
        String dni = txtDni.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String carrera = txtCarrera.getText().trim();
        boolean esBibliotecario = "BIBLIOTECARIO".equals(comboPerfil.getSelectedItem());
        String rol = String.valueOf(comboRol.getSelectedItem());

        if (codigo.isEmpty() || nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Utilidades.error("El codigo, el nombre, el correo y la contrasena son obligatorios.");
            return;
        }

        if (!esBibliotecario && carrera.isEmpty()) {
            Utilidades.error("Un estudiante debe tener una carrera.");
            return;
        }

        Usuario nuevo;
        if (esBibliotecario) {
            nuevo = new Bibliotecario(0, codigo, dni, nombre, correo, contrasena, "ACTIVO", rol);
        } else {
            nuevo = new Estudiante(0, codigo, dni, nombre, correo, contrasena, "ACTIVO",
                    carrera, false);
        }

        TareaSwing.ejecutar(this, btnRegistrar, "Error al registrar el usuario",
            () -> controlador.registrar(nuevo),
            resultado -> procesarResultado(resultado, true));
    }

    // Cambia el estado entre Activo e Inactivo (RF-03)
    private void cambiarEstado(boolean activo) {
        Usuario usuario = usuarioSeleccionado();

        if (usuario == null) {
            Utilidades.error("Primero selecciona un usuario de la tabla.");
            return;
        }

        String nuevoEstado = activo ? "ACTIVO" : "INHABILITADO";

        if (nuevoEstado.equals(usuario.getEstado())) {
            Utilidades.info("El usuario ya esta en estado " + nuevoEstado + ".");
            return;
        }

        JButton boton = activo ? btnActivar : btnInactivar;
        TareaSwing.ejecutar(this, boton, "Error al cambiar el estado",
            () -> controlador.cambiarEstado(usuario, nuevoEstado),
            resultado -> procesarResultado(resultado, true));
    }

    // Nos dice si la persona es estudiante o bibliotecario
    private String tipoDe(Usuario usuario) {
        if (usuario instanceof Bibliotecario) {
            return "Bibliotecario";
        }
        return "Estudiante";
    }

    private void procesarResultado(ResultadoOperacion resultado, boolean recargar) {
        if (resultado.exitoso()) {
            Utilidades.info(resultado.mensaje());
            limpiarFormulario();
            if (recargar) cargarUsuarios();
        } else {
            Utilidades.error(resultado.mensaje());
        }
    }

    // Limpia los campos y la seleccion de la tabla
    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtDni.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        txtCarrera.setText("");
        comboPerfil.setSelectedItem("ESTUDIANTE");
        comboRol.setSelectedItem("BIBLIOTECARIO");
        comboRol.setEnabled(false);
        txtCarrera.setEnabled(true);
        tabla.clearSelection();
    }
}
