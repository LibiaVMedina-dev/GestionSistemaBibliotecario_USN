package com.grupo7.vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.grupo7.controladores.LoginController;
import com.grupo7.controladores.ResultadoLogin;
import com.grupo7.util.TareaSwing;

/**
 * Ventana de ingreso del sistema.
 *
 * El RF-01 y el HU-01 piden validar el codigo institucional Y la
 * contraseña antes de dejar entrar a los modulos de gestion.
 */
public class VentanaLogin extends JFrame {

    private final LoginController controlador = new LoginController();

    private final JTextField txtCodigo = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(16);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JLabel lblAviso = new JLabel(" ");

    public VentanaLogin() {
        setTitle("BIBLIOTECA UNIVERSIDAD SUPERIOR NOVA - Ingreso");
        setSize(440, 330);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        // Asi el usuario tambien puede ingresar presionando Enter
        getRootPane().setDefaultButton(btnIngresar);
    }

    // Parte de arriba con el nombre del sistema
    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(38, 79, 120));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel titulo = new JLabel("BIBLIOTECA UNIVERSIDAD SUPERIOR NOVA", JLabel.CENTER);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel subtitulo = new JLabel("Universidad Superior Nova", JLabel.CENTER);
        subtitulo.setForeground(Color.LIGHT_GRAY);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(titulo, BorderLayout.CENTER);
        panel.add(subtitulo, BorderLayout.SOUTH);
        return panel;
    }

    // Cuadro con el codigo y la contrasena, uno debajo del otro
    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(6, 6, 6, 6);
        r.anchor = GridBagConstraints.LINE_END;

        JLabel lblCodigo = new JLabel("Codigo institucional:");
        JLabel lblContrasena = new JLabel("Contrasena:");
        lblCodigo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblContrasena.setFont(new Font("Arial", Font.PLAIN, 14));

        // primera fila: el codigo
        r.gridx = 0; r.gridy = 0;
        panel.add(lblCodigo, r);
        r.gridx = 1;
        r.anchor = GridBagConstraints.LINE_START;
        panel.add(txtCodigo, r);

        // segunda fila: la contrasena
        r.gridx = 0; r.gridy = 1;
        r.anchor = GridBagConstraints.LINE_END;
        panel.add(lblContrasena, r);
        r.gridx = 1;
        r.anchor = GridBagConstraints.LINE_START;
        panel.add(txtContrasena, r);

        // tercera fila: el boton
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 13));
        btnIngresar.addActionListener(e -> validarIngreso());
        r.gridx = 1; r.gridy = 2;
        panel.add(btnIngresar, r);

        // cuarta fila: el aviso de error en rojo
        lblAviso.setForeground(new Color(180, 0, 0));
        lblAviso.setFont(new Font("Arial", Font.PLAIN, 12));
        lblAviso.setHorizontalAlignment(JLabel.CENTER);
        lblAviso.setVerticalAlignment(JLabel.TOP);
        lblAviso.setPreferredSize(new Dimension(340, 42));
        lblAviso.setMinimumSize(new Dimension(340, 42));
        r.gridx = 0; r.gridy = 3;
        r.gridwidth = 2;
        r.anchor = GridBagConstraints.CENTER;
        r.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblAviso, r);

        return panel;
    }

    // Recordatorio pequeno de abajo
    private JPanel crearPie() {
        JPanel panel = new JPanel();
        JLabel ayuda = new JLabel("El acceso se restringe segun el rol del usuario.");
        ayuda.setFont(new Font("Arial", Font.ITALIC, 11));
        ayuda.setForeground(Color.GRAY);
        panel.add(ayuda);
        return panel;
    }

    // Busca el codigo en la base de datos y decide si deja pasar al menu
    private void validarIngreso() {
        String codigo = txtCodigo.getText().trim();
        char[] clave = txtContrasena.getPassword();

        lblAviso.setText(" ");

        if (codigo.isEmpty() || clave.length == 0) {
            lblAviso.setText("Completa el codigo y la contrasena.");
            Arrays.fill(clave, '\0');
            return;
        }

        String contrasena = new String(clave);
        Arrays.fill(clave, '\0');

        TareaSwing.ejecutar(
            this,
            btnIngresar,
            "No se pudo validar el ingreso",
            () -> controlador.autenticar(codigo, contrasena),
            resultado -> procesarResultado(resultado)
        );
    }

    private void procesarResultado(ResultadoLogin resultado) {
        if (!resultado.exitoso()) {
            lblAviso.setText("<html><div style='width:280px; text-align:center'>"
                    + resultado.mensaje() + "</div></html>");
            if (resultado.mensaje().startsWith("Credenciales")) {
                txtContrasena.setText("");
                txtContrasena.requestFocusInWindow();
            }
            return;
        }

        dispose();
        new VentanaPrincipal(resultado.usuario()).setVisible(true);
    }
}
