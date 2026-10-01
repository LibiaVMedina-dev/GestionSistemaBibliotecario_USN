package com.grupo7.vistas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.grupo7.controladores.ReporteController;
import com.grupo7.controladores.ResultadoOperacion;
import com.grupo7.modelos.Libro;
import com.grupo7.modelos.Prestamo;
import com.grupo7.modelos.Sancion;
import com.grupo7.util.TareaSwing;
import com.grupo7.util.Utilidades;


/**
 * Panel de reportes. El HU-06 pide cuatro consultas distintas,
 * por eso cada una vive en su propia pestana.
 *
 *   Pestana 1: usuarios sancionados y multas pendientes (criterio 1)
 *   Pestana 2: historial de prestamos por rango de fechas (criterio 2)
 *   Pestana 3: titulos con baja disponibilidad (criterio 3)
 *   Pestana 4: resumen general de la biblioteca
 */
public class PanelReportes extends JPanel {

    // Mensaje que el HU-06 pide mostrar cuando no hay resultados
    private static final String SIN_RESULTADOS =
            "No se encontraron registros para los criterios seleccionados";

    private final ReporteController controlador = new ReporteController();
    private final List<Sancion> sancionesCargadas = new ArrayList<>();

    // Pestana 2: los dos campos de fecha
    private final JSpinner spDesde = new JSpinner(new SpinnerDateModel());
    private final JSpinner spHasta = new JSpinner(new SpinnerDateModel());

    // Pestana 3: el umbral de stock minimo
    private final JSpinner spUmbral = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JTextArea areaResumen = new JTextArea();
    private final JButton btnPagarSancion = new JButton("Registrar pago de multa");

    // Tablas de las pestañas 1, 2 y 3
    private final DefaultTableModel modeloSanciones = new DefaultTableModel(
            new Object[]{"Codigo", "Estudiante", "Dias de retraso", "Libro adeudado", "Monto", "Pagada"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final DefaultTableModel modeloPrestamos = new DefaultTableModel(
            new Object[]{"N.", "Usuario", "Libro", "Salida", "Limite", "Devolucion", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final DefaultTableModel modeloStock = new DefaultTableModel(
            new Object[]{"ISBN", "Titulo", "Autor", "Categoria", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaSanciones = new JTable(modeloSanciones);

    public PanelReportes() {
        setLayout(new BorderLayout());

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Morosidad", crearPestanaSanciones());
        pestanas.addTab("Prestamos por fechas", crearPestanaPrestamos());
        pestanas.addTab("Baja disponibilidad", crearPestanaStock());
        pestanas.addTab("Resumen", crearPestanaResumen());

        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedIndex() == 0) {
                cargarSanciones();
            } else if (pestanas.getSelectedIndex() == 3) {
                cargarResumen();
            }
        });

        add(pestanas, BorderLayout.CENTER);

        // dejamos el reporte de morosidad listo al abrir el panel
        cargarSanciones();
    }

    // ---------------------------------------------------------
    // Pestana 1: usuarios sancionados y multas pendientes
    // ---------------------------------------------------------
    private JPanel crearPestanaSanciones() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPagarSancion.addActionListener(e -> pagarSancionSeleccionada());
        btnPagarSancion.setEnabled(false);
        arriba.add(btnPagarSancion);
        arriba.add(new JLabel(
            "  Solo se puede pagar una multa después de registrar la devolución."
        ));

        tablaSanciones.setRowHeight(22);
        tablaSanciones.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaSanciones), BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------
    // Pestana 2: historial de prestamos por rango de fechas
    // ---------------------------------------------------------
    private JPanel crearPestanaPrestamos() {
        JPanel panel = new JPanel(new BorderLayout());

        spDesde.setEditor(new JSpinner.DateEditor(spDesde, "yyyy-MM-dd"));
        spHasta.setEditor(new JSpinner.DateEditor(spHasta, "yyyy-MM-dd"));
        spDesde.setValue(java.sql.Date.valueOf(LocalDate.now().minusMonths(1)));
        spHasta.setValue(java.sql.Date.valueOf(LocalDate.now()));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGenerar = new JButton("Generar Reporte");

        btnGenerar.addActionListener(e -> cargarPrestamosPorRango());

        arriba.add(new JLabel("Fecha de inicio:"));
        arriba.add(spDesde);
        arriba.add(new JLabel("Fecha de fin:"));
        arriba.add(spHasta);
        arriba.add(btnGenerar);

        JTable tabla = new JTable(modeloPrestamos);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------
    // Pestana 3: titulos con baja disponibilidad
    // ---------------------------------------------------------
    private JPanel crearPestanaStock() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGenerar = new JButton("Generar reporte");
        btnGenerar.addActionListener(e -> cargarStockBajo());

        arriba.add(new JLabel("Mostrar titulos con stock menor o igual a:"));
        arriba.add(spUmbral);
        arriba.add(btnGenerar);

        JTable tabla = new JTable(modeloStock);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------
    // Pestana 4: resumen general
    // ---------------------------------------------------------
    private JPanel crearPestanaResumen() {
        JPanel panel = new JPanel(new BorderLayout());

        areaResumen.setEditable(false);
        areaResumen.setFont(new Font("Monospaced", Font.PLAIN, 13));

        panel.add(new JScrollPane(areaResumen), BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------

    // Criterio 1 del HU-06: sancionados y multas pendientes
    private void cargarSanciones() {
        TareaSwing.ejecutar(this, null, "No se pudo generar el reporte",
            controlador::listarMultasPendientes,
            sanciones -> {
                modeloSanciones.setRowCount(0);
                sancionesCargadas.clear();
                sancionesCargadas.addAll(sanciones);
                btnPagarSancion.setEnabled(!sancionesCargadas.isEmpty());

                if (sancionesCargadas.isEmpty()) {
                    modeloSanciones.addRow(new Object[]{
                        "No hay multas pendientes", "", "", "", "", ""
                    });
                    return;
                }

                for (Sancion sancion : sancionesCargadas) {
                    modeloSanciones.addRow(new Object[]{
                            sancion.getPrestamo().getEstudiante().getCodigo(),
                            sancion.getPrestamo().getEstudiante().getNombre(),
                            sancion.getDiasRetraso(),
                            sancion.getPrestamo().getLibro().getTitulo(),
                            String.format("S/ %.2f", sancion.getMontoMulta()),
                            "NO"
                    });
                }
            });
    }

    private void pagarSancionSeleccionada() {
        int fila = tablaSanciones.getSelectedRow();
        if (fila < 0 || sancionesCargadas.isEmpty()) {
            Utilidades.error("Selecciona una multa pendiente de la tabla.");
            return;
        }
        Sancion sancion = sancionesCargadas.get(fila);
        if (!"DEVUELTO".equals(sancion.getPrestamo().getEstado())) {
            Utilidades.error(
                "Primero registra la devolución del libro antes de pagar la multa."
            );
            return;
        }
        if (!Utilidades.confirmar("¿Confirmas el pago de la multa de "
                + sancion.getPrestamo().getEstudiante().getNombre() + "?")) {
            return;
        }
        TareaSwing.ejecutar(this, btnPagarSancion, "No se pudo registrar el pago",
            () -> controlador.pagarSancion(sancion),
            resultado -> {
                mostrarResultado(resultado);
                if (resultado.exitoso()) {
                    cargarSanciones();
                    cargarResumen();
                }
            });
    }

    // Criterio 2 del HU-06: prestamos dentro de un rango de fechas
    private void cargarPrestamosPorRango() {
        modeloPrestamos.setRowCount(0);

        LocalDate desde = aFecha(spDesde);
        LocalDate hasta = aFecha(spHasta);

        if (desde == null || hasta == null) {
            Utilidades.error("Completa las dos fechas del rango.");
            return;
        }

        if (desde.isAfter(hasta)) {
            Utilidades.error("La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        TareaSwing.ejecutar(this, null, "No se pudo generar el reporte",
            () -> controlador.listarPrestamos(desde, hasta),
            prestamos -> {
                modeloPrestamos.setRowCount(0);
                for (Prestamo prestamo : prestamos) {
                    modeloPrestamos.addRow(new Object[]{
                            prestamo.getIdPrestamo(), prestamo.getEstudiante().getNombre(),
                            prestamo.getLibro().getTitulo(), prestamo.getFechaSalida(),
                            prestamo.getFechaLimite(),
                            prestamo.getFechaDevolucion() == null ? "-" : prestamo.getFechaDevolucion(),
                            prestamo.getEstado()
                    });
                }
                if (prestamos.isEmpty()) Utilidades.info(SIN_RESULTADOS);
            });
    }

    // Criterio 3 del HU-06: titulos con stock igual a cero o bajo el umbral
    private void cargarStockBajo() {
        modeloStock.setRowCount(0);

        int umbral = (Integer) spUmbral.getValue();

        TareaSwing.ejecutar(this, null, "No se pudo generar el reporte",
            () -> controlador.listarStockBajo(umbral),
            libros -> {
                modeloStock.setRowCount(0);
                for (Libro libro : libros) {
                    modeloStock.addRow(new Object[]{
                            libro.getIsbn(), libro.getTitulo(), libro.getAutor(),
                            libro.getCategoria(), libro.getStock()
                    });
                }
                if (libros.isEmpty()) Utilidades.info(SIN_RESULTADOS);
            });
    }

    // Resumen general de como esta la biblioteca
    private void cargarResumen() {
        TareaSwing.ejecutar(this, null, "No se pudo generar el reporte",
            controlador::generarResumen,
            texto -> {
                areaResumen.setText(texto);
                areaResumen.setCaretPosition(0);
            });
    }

    private void mostrarResultado(ResultadoOperacion resultado) {
        if (resultado.exitoso()) Utilidades.info(resultado.mensaje());
        else Utilidades.error(resultado.mensaje());
    }

    // Convierte el valor del JSpinner a una fecha de Java
    private LocalDate aFecha(JSpinner spinner) {
        Object valor = spinner.getValue();

        if (valor instanceof java.util.Date) {
            return ((java.util.Date) valor).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }
}
