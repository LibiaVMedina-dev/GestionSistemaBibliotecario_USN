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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.grupo7.controladores.LibroController;
import com.grupo7.controladores.ResultadoOperacion;
import com.grupo7.modelos.Libro;
import com.grupo7.util.TareaSwing;
import com.grupo7.util.Utilidades;

/**
 * Panel para administrar el catalogo de libros.
 * Muestra la tabla libro y permite registrar y editar sus datos.
 */
public class PanelLibros extends JPanel {

    // Instanciamos una sola vez el DAO que nos entrego el companero
    private final LibroController controlador = new LibroController();

    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtAnio = new JTextField();
    private final JTextField txtCategoria = new JTextField();
    private final JTextField txtStock = new JTextField();

    // Barra de busqueda del RF-05 y el HU-03
    private final JTextField txtBuscar = new JTextField();
    private final JLabel lblResultado = new JLabel(" ");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnRegistrar = new JButton("Registrar libro");
    private final JButton btnEditar = new JButton("Guardar cambios");

    // Guardamos los libros cargados para no volver a la base en cada ajuste
    private List<Libro> librosCargados;

    // La tabla no se edita a mano, solo muestra lo que hay en la base de datos
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ISBN", "Titulo", "Autor", "Anio", "Categoria", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public PanelLibros() {
        setLayout(new BorderLayout());
        add(crearBuscador(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);

        // apenas se abre el panel ya mostramos los libros registrados
        cargarLibros();
    }

    // Barra de busqueda que pide el RF-05
    private JPanel crearBuscador() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnLimpiar = new JButton("Limpiar busqueda");

        btnBuscar.addActionListener(e -> buscarLibros());
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarLibros();
        });

        lblResultado.setFont(new Font("Arial", Font.PLAIN, 12));

        txtBuscar.setPreferredSize(new Dimension(260, 25));

        panel.add(new JLabel("Buscar por titulo, autor, categoria o ISBN:"));
        panel.add(txtBuscar);
        panel.add(btnBuscar);
        panel.add(btnLimpiar);
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

    // Campos para registrar un libro nuevo
    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(265, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del libro"));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(12, 8, 4, 8);
        r.fill = GridBagConstraints.HORIZONTAL;

        // una etiqueta arriba de cada campo para que se lean en fila
        agregarCampo(panel, r, 0, "ISBN:", txtIsbn);
        agregarCampo(panel, r, 2, "Titulo:", txtTitulo);
        agregarCampo(panel, r, 4, "Autor:", txtAutor);
        agregarCampo(panel, r, 6, "Anio de edicion:", txtAnio);
        agregarCampo(panel, r, 8, "Categoria:", txtCategoria);
        agregarCampo(panel, r, 10, "Stock:", txtStock);

        // botones de accion uno debajo del otro
        JButton btnLimpiar = new JButton("Limpiar formulario");

        btnRegistrar.addActionListener(e -> registrarLibro());
        btnEditar.addActionListener(e -> editarLibro());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        r.gridx = 0; r.gridy = 12;
        r.gridwidth = 2;
        r.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnRegistrar, r);
        r.gridy = 13;
        panel.add(btnEditar, r);
        r.gridy = 14;
        panel.add(btnLimpiar, r);

        return panel;
    }

    // Coloca una etiqueta en una fila y el campo en la siguiente
    private void agregarCampo(JPanel panel, GridBagConstraints r, int fila, String etiqueta, JTextField campo) {
        r.gridwidth = 2;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;
        r.gridy = fila;
        if (fila == 0) r.insets = new Insets(10, 8, 2, 8);
        else r.insets = new Insets(4, 8, 2, 8);
        r.anchor = GridBagConstraints.LINE_START;
        panel.add(new JLabel(etiqueta), r);

        campo.setPreferredSize(new Dimension(215, 25));
        r.gridy = fila + 1;
        panel.add(campo, r);
    }

    // Tabla con los libros que vienen de la base de datos
    private JPanel crearTabla() {
        tabla.getColumnModel().getColumn(0).setPreferredWidth(115);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(210);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(60);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));

        // al hacer clic se llenan los campos para poder editar
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarFormularioDesdeTabla();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Catalogo de libros"));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // Consulta la tabla libro y llena el JTable
    private void cargarLibros() {
        TareaSwing.ejecutar(this, null, "No se pudo cargar el catálogo de libros",
            controlador::listar,
            libros -> {
                librosCargados = libros;
                llenarTabla(librosCargados);
                lblResultado.setText(librosCargados.size() + " título(s) en el catálogo.");
            });
    }

    // Busca por titulo, autor, categoria o ISBN (RF-05)
    private void buscarLibros() {
        String texto = txtBuscar.getText().trim();

        if (texto.isEmpty()) {
            cargarLibros();
            return;
        }

        TareaSwing.ejecutar(this, btnBuscar, "No se pudo buscar",
            () -> controlador.buscar(texto),
            libros -> {
                librosCargados = libros;
                llenarTabla(librosCargados);
                if (librosCargados.isEmpty()) {
                    lblResultado.setText("No se encontraron resultados coincidentes");
                    Utilidades.info("No se encontraron resultados coincidentes.");
                } else {
                    lblResultado.setText(librosCargados.size() + " resultado(s).");
                }
            });
    }

    // Pasa una lista de libros al JTable
    private void llenarTabla(List<Libro> libros) {
        modelo.setRowCount(0);

        for (Libro libro : libros) {
            modelo.addRow(new Object[]{
                    libro.getIsbn(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getAnio(),
                    libro.getCategoria(),
                    libro.getStock()
            });
        }
    }

    // Pasa los datos de la fila seleccionada a los campos del formulario
    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            return;
        }

        txtIsbn.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtTitulo.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtAutor.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtAnio.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtCategoria.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        txtStock.setText(String.valueOf(modelo.getValueAt(fila, 5)));
    }

    // Devuelve el libro que esta en la fila seleccionada
    private Libro libroSeleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            Utilidades.error("Primero selecciona un libro de la tabla.");
            return null;
        }
        return librosCargados.get(fila);
    }

    // Valida los campos y guarda el libro con el DAO
    private void registrarLibro() {
        String isbn = txtIsbn.getText().trim();
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String anioTexto = txtAnio.getText().trim();
        String categoria = txtCategoria.getText().trim();
        String stockTexto = txtStock.getText().trim();

        if (isbn.isEmpty() || titulo.isEmpty() || autor.isEmpty()) {
            Utilidades.error("El ISBN, el titulo y el autor son obligatorios.");
            return;
        }

        // el anio y el stock tienen que ser numeros enteros
        int anio = 0;
        if (!anioTexto.isEmpty()) {
            try {
                anio = Integer.parseInt(anioTexto);
            } catch (NumberFormatException e) {
                Utilidades.error("El anio de edicion debe ser un numero.");
                return;
            }
        }

        int stock;
        try {
            stock = Integer.parseInt(stockTexto);
        } catch (NumberFormatException e) {
            Utilidades.error("La cantidad de ejemplares debe ser un numero, por ejemplo 3.");
            return;
        }

        if (stock < 0) {
            Utilidades.error("La cantidad de ejemplares no puede ser negativa.");
            return;
        }

        Libro nuevo = new Libro(isbn, titulo, autor, anio, categoria, stock);
        TareaSwing.ejecutar(this, btnRegistrar, "Error al registrar el libro",
            () -> controlador.registrar(nuevo),
            resultado -> procesarResultado(resultado, true));
    }

    // Guarda los cambios de un libro que ya estaba registrado
    private void editarLibro() {
        Libro seleccionado = libroSeleccionado();

        if (seleccionado == null) {
            return;
        }

        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String categoria = txtCategoria.getText().trim();

        if (titulo.isEmpty() || autor.isEmpty()) {
            Utilidades.error("El titulo y el autor son obligatorios.");
            return;
        }

        int anio = seleccionado.getAnio();
        if (!txtAnio.getText().trim().isEmpty()) {
            try {
                anio = Integer.parseInt(txtAnio.getText().trim());
            } catch (NumberFormatException e) {
                Utilidades.error("El anio de edicion debe ser un numero.");
                return;
            }
        }

        int stock;
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            Utilidades.error("El stock debe ser un número entero, por ejemplo 3.");
            return;
        }

        if (stock < 0) {
            Utilidades.error("El stock no puede ser negativo.");
            return;
        }

        Libro actualizado = new Libro(seleccionado.getIsbn(), titulo, autor, anio,
                categoria, stock);

        TareaSwing.ejecutar(this, btnEditar, "Error al actualizar el libro",
            () -> controlador.actualizar(actualizado),
            resultado -> procesarResultado(resultado, true));
    }

    private void procesarResultado(ResultadoOperacion resultado, boolean recargar) {
        if (resultado.exitoso()) {
            Utilidades.info(resultado.mensaje());
            limpiarFormulario();
            if (recargar) cargarLibros();
        } else {
            Utilidades.error(resultado.mensaje());
        }
    }

    // Limpia los campos y la seleccion de la tabla
    private void limpiarFormulario() {
        txtIsbn.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtAnio.setText("");
        txtCategoria.setText("");
        txtStock.setText("");
        tabla.clearSelection();
    }
}
