package com.grupo7.vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.grupo7.controladores.PrestamoController;
import com.grupo7.controladores.ResultadoOperacion;
import com.grupo7.modelos.Estudiante;
import com.grupo7.modelos.Libro;
import com.grupo7.modelos.Prestamo;
import com.grupo7.util.TareaSwing;
import com.grupo7.util.Utilidades;

/**
 * Panel de prestamos y devoluciones.
 *
 * El RNF-03 pide que el bibliotecario complete un prestamo en tres
 * pasos: seleccionar estudiante, seleccionar libro y confirmar.
 */
public class PanelPrestamos extends JPanel {

    // El RF-07 y el HU-04 fijan el prestamo en 3 dias
    public static final int DIAS_PRESTAMO = PrestamoController.DIAS_PRESTAMO;

    private final PrestamoController controlador = new PrestamoController();

    // Listas que llenan los combo box y las tablas
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Libro> libros = new ArrayList<>();
    private final List<Prestamo> prestamos = new ArrayList<>();

    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();
    private final JComboBox<Libro> comboLibro = new JComboBox<>();
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnRegistrar = new JButton("Registrar prestamo");
    private final JButton btnDevolver = new JButton("Registrar devolucion");
    private final JButton btnActualizar = new JButton("Actualizar tabla");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"N.", "Estudiante", "Libro", "Salida", "Limite", "Devolucion", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public PanelPrestamos() {
        setLayout(new BorderLayout());
        add(crearCentro(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        comboEstudiante.addActionListener(e -> actualizarResumen());
        comboLibro.addActionListener(e -> actualizarResumen());

        // al abrir el panel llenamos los combo box y el historial
        cargarListas();
        cargarPrestamos();
    }

    // Formulario a la izquierda y tabla a la derecha
    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(crearFormulario(), BorderLayout.WEST);
        centro.add(crearTabla(), BorderLayout.CENTER);
        return centro;
    }

    // Los tres pasos del RNF-03, uno debajo del otro
    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(390, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Pasos del prestamo"));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(4, 10, 4, 10);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridwidth = 2;
        r.weightx = 1.0;

        // paso 1
        r.gridx = 0; r.gridy = 0;
        r.anchor = GridBagConstraints.LINE_START;
        r.insets = new Insets(12, 10, 4, 10); 
        panel.add(new JLabel("Paso 1: seleccione al estudiante"), r);
        r.insets = new Insets(4, 10, 4, 10);    

        comboEstudiante.setBackground(Color.WHITE);
        comboEstudiante.setOpaque(true);
        comboEstudiante.setPreferredSize(new Dimension(350, 28));
        r.gridy = 1;
        panel.add(comboEstudiante, r);

        // paso 2
        r.gridy = 3;
        r.insets = new Insets(10, 10, 4, 10);
        panel.add(new JLabel("Paso 2: seleccione el libro"), r);
        r.insets = new Insets(4, 10, 4, 10);

        comboLibro.setBackground(Color.WHITE);
        comboLibro.setOpaque(true);
        comboLibro.setPreferredSize(new Dimension(350, 28));
        r.gridy = 4;
        panel.add(comboLibro, r);

        // paso 3
        r.gridy = 6;
        r.insets = new Insets(10, 10, 4, 10);
        panel.add(new JLabel("Paso 3: confirme el registro"), r);
        r.insets = new Insets(4, 10, 4, 10);

        lblResumen.setFont(new Font("Arial", Font.PLAIN, 12));
        lblResumen.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        lblResumen.setPreferredSize(new Dimension(350, 62));
        r.gridy = 7;
        panel.add(lblResumen, r);

        btnRegistrar.addActionListener(e -> registrarPrestamo());
        btnDevolver.addActionListener(e -> devolverLibro());
        btnActualizar.addActionListener(e -> {
            cargarListas();
            cargarPrestamos();
        });

        r.gridy = 8;
        btnRegistrar.setPreferredSize(new Dimension(350, 30));
        panel.add(btnRegistrar, r);
        r.gridy = 9;
        btnDevolver.setPreferredSize(new Dimension(350, 30));
        panel.add(btnDevolver, r);
        r.gridy = 10;
        btnActualizar.setPreferredSize(new Dimension(350, 30));
        panel.add(btnActualizar, r);

        // La fila flexible mantiene los controles alineados arriba al maximizar.
        r.gridy = 11;
        r.weighty = 1.0;
        r.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), r);

        return panel;
    }

    // Historial de prestamos que viene de la tabla prestamo
    private JPanel crearTabla() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(95);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(90);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Historial de prestamos"));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // Recordatorio de la politica de prestamos
    private JPanel crearPie() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel politica = new JLabel("Politica: prestamo de " + DIAS_PRESTAMO
                + " dias. Multa de S/ " + String.format("%.2f", PrestamoController.TARIFA_POR_DIA)
                + " por cada dia de retraso.");
        politica.setFont(new Font("Arial", Font.ITALIC, 12));
        politica.setForeground(new java.awt.Color(90, 90, 90));

        panel.add(politica);
        return panel;
    }

    // Trae estudiantes y libros de la base para llenar los combo box
    private void cargarListas() {
        TareaSwing.ejecutar(this, btnActualizar, "No se pudieron cargar los datos del préstamo",
            controlador::cargarDatos,
            datos -> {
                estudiantes.clear();
                estudiantes.addAll(datos.estudiantes());
                libros.clear();
                libros.addAll(datos.libros());

                comboEstudiante.removeAllItems();
                estudiantes.forEach(comboEstudiante::addItem);
                comboLibro.removeAllItems();
                libros.forEach(comboLibro::addItem);
                actualizarResumen();
            });
    }

    // Muestra que se va a prestar y hasta que fecha
    private void actualizarResumen() {
        Estudiante estudiante = (Estudiante) comboEstudiante.getSelectedItem();
        Libro libro = (Libro) comboLibro.getSelectedItem();

        if (estudiante == null || libro == null) {
            lblResumen.setText(" ");
            return;
        }

        LocalDate limite = LocalDate.now().plusDays(DIAS_PRESTAMO);
        lblResumen.setText("<html>Se entregara <b>" + libro.getTitulo() + "</b> a <b>"
                + estudiante.getNombre() + "</b>.<br>Debe devolverse el <b>" + limite
                + "</b>.</html>");
    }

    // Consulta la tabla prestamo y llena el JTable
    private void cargarPrestamos() {
        TareaSwing.ejecutar(this, btnActualizar, "No se pudo cargar el historial de préstamos",
            controlador::listarPrestamos,
            resultado -> {
                modelo.setRowCount(0);
                prestamos.clear();
                prestamos.addAll(resultado);
                for (Prestamo prestamo : prestamos) {
                    modelo.addRow(new Object[]{
                            prestamo.getIdPrestamo(),
                            prestamo.getEstudiante().getNombre(),
                            prestamo.getLibro().getTitulo(),
                            prestamo.getFechaSalida(),
                            prestamo.getFechaLimite(),
                            (prestamo.getFechaDevolucion() == null) ? "-" : prestamo.getFechaDevolucion(),
                            estadoDe(prestamo)
                    });
                }
            });
    }

    // Entrega el libro al estudiante que este seleccionado
    private void registrarPrestamo() {
        Estudiante estudiante = (Estudiante) comboEstudiante.getSelectedItem();
        Libro libro = (Libro) comboLibro.getSelectedItem();

        if (estudiante == null || libro == null) {
            Utilidades.error("Selecciona un estudiante y un libro.");
            return;
        }

        TareaSwing.ejecutar(this, btnRegistrar, "Error al registrar el préstamo",
            () -> controlador.registrarPrestamo(estudiante, libro),
            resultado -> procesarResultado(resultado, true));
    }

    // Marca el prestamo como devuelto, repone el stock y calcula la multa
    private void devolverLibro() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            Utilidades.error("Selecciona un prestamo de la tabla.");
            return;
        }

        Prestamo prestamo = prestamos.get(fila);

        // solo se puede devolver un prestamo que siga pendiente
        if (!"PENDIENTE".equals(prestamo.getEstado())) {
            Utilidades.error("Ese prestamo ya fue cerrado, no se puede devolver otra vez.");
            return;
        }

        if (!Utilidades.confirmar("Confirmas la devolucion de\n"
                + prestamo.getLibro().getTitulo() + "?")) {
            return;
        }

        TareaSwing.ejecutar(this, btnDevolver, "Error al registrar la devolución",
            () -> controlador.devolverLibro(prestamo.getIdPrestamo()),
            resultado -> procesarResultado(resultado, true));
    }

    private void procesarResultado(ResultadoOperacion resultado, boolean recargar) {
        if (resultado.exitoso()) {
            Utilidades.info(resultado.mensaje());
            if (recargar) {
                cargarListas();
                cargarPrestamos();
            }
        } else {
            Utilidades.error(resultado.mensaje());
        }
    }

    // Nos dice si el prestamo sigue pendiente o ya esta vencido
    private String estadoDe(Prestamo prestamo) {
        if (!"PENDIENTE".equals(prestamo.getEstado())) {
            return prestamo.getEstado();
        }
        if (prestamo.getFechaLimite().isBefore(LocalDate.now())) {
            return "VENCIDO";
        }
        return "PENDIENTE";
    }
}
