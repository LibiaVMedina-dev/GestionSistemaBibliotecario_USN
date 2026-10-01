package com.grupo7.util;

import javax.swing.JOptionPane;

/**
 * Clase auxiliar que reune los mensajes de alerta que se repiten
 * en varias ventanas, para no escribirlos tan largos cada vez.
 */
public class Utilidades {

    // Muestra un aviso de que la operacion salio bien
    public static void info(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Informacion",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Muestra un aviso cuando algo no se pudo hacer
    public static void error(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // Pregunta al bibliotecario si realmente quiere continuar con la accion
    public static boolean confirmar(String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(null, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
    }
}
