package com.grupo7.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase auxiliar para proteger las contrasenas de los usuarios.
 *
 * El RNF-02 del documento pide que las contrasenas NO se guarden
 * en texto plano. Por eso guardamos el resultado de aplicar
 * SHA-256 a la contrasena, que es una huella de 64 caracteres.
 */
public class ClaveUtil {

    // No se instancia porque solo usamos metodos estaticos
    private ClaveUtil() {
    }

    // Convierte la contrasena en su huella SHA-256 en hexadecimal
    public static String encriptar(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            return "";
        }
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            byte[] bytes = resumen.digest(contrasena.getBytes(StandardCharsets.UTF_8));

            // vamos pasando cada byte a hexadecimal de dos digitos
            StringBuilder huella = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    huella.append('0');
                }
                huella.append(hex);
            }
            return huella.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre viene en Java, pero por si acaso
            throw new IllegalStateException("No se pudo proteger la contrasena.", e);
        }
    }

    // Compara la contrasena escrita con la huella guardada
    public static boolean verificar(String contrasenaIngresada, String huellaGuardada) {
        if (contrasenaIngresada == null || huellaGuardada == null || huellaGuardada.isEmpty()) {
            return false;
        }
        return encriptar(contrasenaIngresada).equalsIgnoreCase(huellaGuardada);
    }
}
