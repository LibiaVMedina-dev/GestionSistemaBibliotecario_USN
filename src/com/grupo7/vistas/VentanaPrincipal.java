package com.grupo7.vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.grupo7.modelos.Bibliotecario;

/**
 * Ventana principal del sistema. Se abre despues de pasar el ingreso y
 * muestra cada modulo del proyecto en una pestana.
 */
public class VentanaPrincipal extends JFrame {

    // Recordamos quien ingreso para saludarlo y para el pie de la ventana
    private final Bibliotecario usuario;

    public VentanaPrincipal(Bibliotecario usuario) {
        this.usuario = usuario;

        setTitle("Biblioteca PageTurner - Menu principal");
        setSize(1020, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearPestanas(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        // Ajustamos el tamano de las ventanas internas segun la pantalla
        setMinimumSize(new java.awt.Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // Saludo con el nombre del usuario que ingreso
    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(38, 79, 120));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel saludo = new JLabel("Bienvenido, " + usuario.getNombre());
        saludo.setForeground(Color.WHITE);
        saludo.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel rol = new JLabel("Codigo: " + usuario.getCodigo() + "  |  Rol: " + usuario.getRol());
        rol.setForeground(Color.LIGHT_GRAY);
        rol.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(saludo, BorderLayout.WEST);

        JPanel ladoDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ladoDerecho.setOpaque(false);
        ladoDerecho.add(rol);
        panel.add(ladoDerecho, BorderLayout.EAST);

        return panel;
    }

    // Un modulo del proyecto por pestana
    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab("Libros", new PanelLibros());
        pestanas.addTab("Prestamos", new PanelPrestamos());
        if ("ADMINISTRADOR".equals(usuario.getRol())) {
            pestanas.addTab("Usuarios", new PanelUsuarios());
        }
        pestanas.addTab("Reportes", new PanelReportes());

        return pestanas;
    }

    // Boton para volver a la ventana de ingreso
    private JPanel crearPie() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnCerrar = new JButton("Cerrar sesion");
        btnCerrar.addActionListener(e -> {
            dispose();
            new VentanaLogin().setVisible(true);
        });

        panel.add(btnCerrar);
        return panel;
    }
}
