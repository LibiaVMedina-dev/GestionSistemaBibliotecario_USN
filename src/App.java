import javax.swing.SwingUtilities;

import com.grupo7.vistas.VentanaLogin;

/**
 * Punto de entrada del sistema. Solo abre la ventana de ingreso,
 * de ahi el usuario pasa al menu principal.
 */
public class App {
    public static void main(String[] args) {
        // Swing pide que las ventanas se creen en un hilo especial
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
