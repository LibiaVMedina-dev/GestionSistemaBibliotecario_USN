-- =============================================================
-- Actualizacion v2 del esquema - Sistema BIBLIOTECA UNIVERSIDAD SUPERIOR NOVA
-- -------------------------------------------------------------
-- El schema.sql original (de Arturo) creaba las tablas, pero le
-- faltaban columnas que pide el documento del proyecto:
--
--   RF-01  la autenticacion necesita codigo Y contrasena
--   RF-02  el alta de usuarios pide DNI y correo sin duplicados
--   RF-04  el inventario pide anio de edicion y categoria
--   RNF-02 las contrasenas no se guardan en texto plano
--
-- Este script agrega esas columnas. Se puede correr las veces que
-- quieras porque usa "IF NOT EXISTS".
-- =============================================================

USE `PageTurner`;

-- -------------------------------------------------------------
-- 1. CONTRA + DNI EN LA TABLA USUARIO
-- -------------------------------------------------------------
-- contrasena guarda el hash SHA-256, nunca la clave real
ALTER TABLE `usuario`
    ADD COLUMN IF NOT EXISTS `dni` VARCHAR(8) NULL AFTER `codigo`,
    ADD COLUMN IF NOT EXISTS `contrasena` VARCHAR(64) NULL AFTER `correo`;

-- El documento pide que el DNI no se repita entre usuarios
ALTER TABLE `usuario`
    ADD UNIQUE INDEX IF NOT EXISTS `uq_usuario_dni` (`dni`);

-- -------------------------------------------------------------
-- 2. ANIO Y CATEGORIA EN LA TABLA LIBRO
-- -------------------------------------------------------------
ALTER TABLE `libro`
    ADD COLUMN IF NOT EXISTS `anio` SMALLINT UNSIGNED NULL AFTER `autor`,
    ADD COLUMN IF NOT EXISTS `categoria` VARCHAR(80) NULL AFTER `anio`;

-- Sirve para el filtro de busqueda por categoria (RF-05)
ALTER TABLE `libro`
    ADD INDEX IF NOT EXISTS `idx_libro_categoria` (`categoria`);

-- -------------------------------------------------------------
-- 3. MORA CALCULADA EN LA TABLA SANCION
-- -------------------------------------------------------------
-- El documento pide(days_retraso, monto_multa). Agregamos la
-- fecha en que se registro para que el reporte de HU-06 pueda
-- mostrar desde cuando se debe la deuda.
ALTER TABLE `sancion`
    ADD COLUMN IF NOT EXISTS `pagada` BOOLEAN NOT NULL DEFAULT FALSE;

-- Indice para el reporte de multas pendientes de HU-06
ALTER TABLE `sancion`
    ADD INDEX IF NOT EXISTS `idx_sancion_pagada` (`pagada`);
