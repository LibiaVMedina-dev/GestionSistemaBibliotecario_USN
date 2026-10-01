-- =============================================================
-- Actualización v3 - coherencia de mora y sanciones
-- -------------------------------------------------------------
-- Conserva la tabla bibliotecario porque representa al personal
-- autorizado y soporta los roles ADMINISTRADOR/BIBLIOTECARIO.
--
-- Este script reconcilia datos existentes. La aplicación vuelve a
-- validar la mora y las multas justo antes de cada préstamo.
-- Es idempotente y puede ejecutarse más de una vez.
-- =============================================================

USE `PageTurner`;

START TRANSACTION;

-- Un estudiante queda marcado si tiene una multa pendiente o un
-- préstamo pendiente cuya fecha límite ya venció.
UPDATE estudiante e
SET e.sancionado = EXISTS (
        SELECT 1
        FROM prestamo p
        WHERE p.id_usuario = e.id_usuario
          AND p.estado = 'PENDIENTE'
          AND p.fecha_limite < CURDATE()
    ) OR EXISTS (
        SELECT 1
        FROM prestamo p
        INNER JOIN sancion s ON s.id_prestamo = p.id_prestamo
        WHERE p.id_usuario = e.id_usuario
          AND s.pagada = FALSE
    );

-- No se sobreescribe INHABILITADO: es un bloqueo administrativo
-- diferente de una sanción por mora.
UPDATE usuario u
INNER JOIN estudiante e ON e.id_usuario = u.id_usuario
SET u.estado = 'SANCIONADO'
WHERE e.sancionado = TRUE
  AND u.estado = 'ACTIVO';

COMMIT;
