package com.grupo7.util;

import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.SwingWorker;

/** Ejecuta acceso remoto sin congelar el hilo de eventos de Swing. */
public final class TareaSwing {
    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar() throws Exception;
    }

    private TareaSwing() {}

    public static <T> void ejecutar(
            Component vista,
            AbstractButton boton,
            String mensajeError,
            Trabajo<T> trabajo,
            Consumer<T> alCompletar) {
        if (boton != null) boton.setEnabled(false);
        vista.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    alCompletar.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Utilidades.error(mensajeError + ": operación interrumpida.");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause() == null ? e : e.getCause();
                    Utilidades.error(mensajeError + ": " + causa.getMessage());
                } finally {
                    vista.setCursor(Cursor.getDefaultCursor());
                    if (boton != null) boton.setEnabled(true);
                }
            }
        }.execute();
    }
}
