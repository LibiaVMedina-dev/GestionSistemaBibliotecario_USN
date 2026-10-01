-- =============================================================
-- Datos iniciales del Sistema de Biblioteca PageTurner
-- -------------------------------------------------------------
-- El schema.sql solo crea las tablas y el actualizacion_v2.sql
-- agrega las columnas de contrasena, dni, anio y categoria.
--
-- Este archivo llena la base con datos de prueba para poder
-- levantar la aplicacion y probarla.
--
-- Se puede correr las veces que quieras: cada INSERT revisa
-- primero si el registro ya existe, asi que no duplica nada.
--
-- CORRE ESTE ARCHIVO DESPUES de schema.sql y actualizacion_v2.sql
-- =============================================================

USE `PageTurner`;

-- -------------------------------------------------------------
-- 1. USUARIOS
--
-- Las contrasenas NO van en texto plano (RNF-02). Cada valor es
-- la huella SHA-256 de la contrasena real:
--
--   LIB001 -> admin123
--   LIB002 -> admin123
--   EST00x -> 123456
--
-- El hash de "admin123" es
--   240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- y el de "123456" es
--   8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- -------------------------------------------------------------

INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'LIB001', '70111233', 'Ana Quispe Rojas', 'anaquispe@universidad.edu',
       '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'LIB001');

INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'LIB002', '70222344', 'Marco Paredes Vega', 'marcoparedes@universidad.edu',
       '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'LIB002');

INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'EST001', '45011222', 'Luis Ramos Vega', 'luisramos@universidad.edu',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'EST001');

INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'EST002', '45011333', 'Maria Chavez Diaz', 'mariachavez@universidad.edu',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'ACTIVO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'EST002');

-- Sancionado a proposito, para probar el bloqueo del RF-03 y el HU-04
INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'EST003', '45011444', 'Carlos Mamani Ortiz', 'carlosmamani@universidad.edu',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'SANCIONADO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'EST003');

-- Inhabilitado a proposito, para probar que el ingreso lo rechace
INSERT INTO usuario (codigo, dni, nombre, correo, contrasena, estado)
SELECT 'EST004', '45011555', 'Ana Lucia Torres Paz', 'analucia@universidad.edu',
       '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'INHABILITADO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE codigo = 'EST004');

-- -------------------------------------------------------------
-- 2. BIBLIOTECARIOS
-- -------------------------------------------------------------

INSERT INTO bibliotecario (id_usuario, rol)
SELECT id_usuario, 'ADMINISTRADOR' FROM usuario
WHERE codigo = 'LIB001'
  AND NOT EXISTS (SELECT 1 FROM bibliotecario WHERE id_usuario = usuario.id_usuario);

INSERT INTO bibliotecario (id_usuario, rol)
SELECT id_usuario, 'BIBLIOTECARIO' FROM usuario
WHERE codigo = 'LIB002'
  AND NOT EXISTS (SELECT 1 FROM bibliotecario WHERE id_usuario = usuario.id_usuario);

-- -------------------------------------------------------------
-- 3. ESTUDIANTES
-- -------------------------------------------------------------

INSERT INTO estudiante (id_usuario, carrera, sancionado)
SELECT id_usuario, 'Sistemas', FALSE FROM usuario
WHERE codigo = 'EST001'
  AND NOT EXISTS (SELECT 1 FROM estudiante WHERE id_usuario = usuario.id_usuario);

INSERT INTO estudiante (id_usuario, carrera, sancionado)
SELECT id_usuario, 'Sistemas', FALSE FROM usuario
WHERE codigo = 'EST002'
  AND NOT EXISTS (SELECT 1 FROM estudiante WHERE id_usuario = usuario.id_usuario);

INSERT INTO estudiante (id_usuario, carrera, sancionado)
SELECT id_usuario, 'Contabilidad', TRUE FROM usuario
WHERE codigo = 'EST003'
  AND NOT EXISTS (SELECT 1 FROM estudiante WHERE id_usuario = usuario.id_usuario);

INSERT INTO estudiante (id_usuario, carrera, sancionado)
SELECT id_usuario, 'Industrial', FALSE FROM usuario
WHERE codigo = 'EST004'
  AND NOT EXISTS (SELECT 1 FROM estudiante WHERE id_usuario = usuario.id_usuario);

-- -------------------------------------------------------------
-- 4. LIBROS
-- El ultimo tiene stock 0 a proposito, para que el reporte de
-- baja disponibilidad (HU-06 criterio 3) tenga algo que mostrar.
-- -------------------------------------------------------------

INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock)
SELECT '9788497860011', 'Clean Code', 'Robert C. Martin', 2008, 'Programacion', 4
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE isbn = '9788497860011');

INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock)
SELECT '9788497860028', 'Introduccion a la Programacion Orientada a Objetos', 'James Eckel',
       2013, 'Programacion', 3
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE isbn = '9788497860028');

INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock)
SELECT '9788497860035', 'Java Como Programar', 'Harvey Deitel', 2018, 'Programacion', 2
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE isbn = '9788497860035');

INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock)
SELECT '9788497860042', 'Base de Datos Relacionales', 'Thomas Connolly', 2015, 'Base de Datos', 5
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE isbn = '9788497860042');

INSERT INTO libro (isbn, titulo, autor, anio, categoria, stock)
SELECT '9788497860059', 'Ingenieria de Software', 'Ian Sommerville', 2016, 'Ingenieria', 0
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE isbn = '9788497860059');

-- -------------------------------------------------------------
-- 5. PRESTAMOS DE EJEMPLO
-- El prestamo dura 3 dias segun el RF-07.
-- -------------------------------------------------------------

-- Prestado ayer, todavia esta a tiempo
INSERT INTO prestamo (id_usuario, isbn, fecha_salida, fecha_limite, estado)
SELECT u.id_usuario, '9788497860011',
       DATE_SUB(CURDATE(), INTERVAL 1 DAY),
       DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 3 DAY),
       'PENDIENTE'
FROM usuario u
WHERE u.codigo = 'EST001'
  AND NOT EXISTS (
      SELECT 1 FROM prestamo p
      WHERE p.id_usuario = u.id_usuario
        AND p.isbn = '9788497860011'
        AND p.estado = 'PENDIENTE'
  );

-- Salio hace 6 dias y sigue pendiente, entonces ya esta vencido
INSERT INTO prestamo (id_usuario, isbn, fecha_salida, fecha_limite, estado)
SELECT u.id_usuario, '9788497860035',
       DATE_SUB(CURDATE(), INTERVAL 6 DAY),
       DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 3 DAY),
       'PENDIENTE'
FROM usuario u
WHERE u.codigo = 'EST002'
  AND NOT EXISTS (
      SELECT 1 FROM prestamo p
      WHERE p.id_usuario = u.id_usuario
        AND p.isbn = '9788497860035'
        AND p.estado = 'PENDIENTE'
  );

-- Este ya se devolvio
INSERT INTO prestamo (id_usuario, isbn, fecha_salida, fecha_limite, fecha_devolucion, estado)
SELECT u.id_usuario, '9788497860042',
       DATE_SUB(CURDATE(), INTERVAL 10 DAY),
       DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 10 DAY), INTERVAL 3 DAY),
       DATE_SUB(CURDATE(), INTERVAL 8 DAY),
       'DEVUELTO'
FROM usuario u
WHERE u.codigo = 'EST001'
  AND NOT EXISTS (
      SELECT 1 FROM prestamo p
      WHERE p.id_usuario = u.id_usuario
        AND p.isbn = '9788497860042'
        AND p.estado = 'DEVUELTO'
  );

-- -------------------------------------------------------------
-- 6. SANCION POR EL PRESTAMO VENCIDO
-- Sale 3 dias de retraso a S/ 5.00 por dia, o sea S/ 15.00
-- -------------------------------------------------------------

INSERT INTO sancion (id_prestamo, dias_retraso, monto_multa, pagada)
SELECT p.id_prestamo, 3, 15.00, FALSE
FROM prestamo p
INNER JOIN usuario u ON u.id_usuario = p.id_usuario
WHERE u.codigo = 'EST002'
  AND p.estado = 'PENDIENTE'
  AND NOT EXISTS (SELECT 1 FROM sancion s WHERE s.id_prestamo = p.id_prestamo);

-- Sincronizamos el estado del estudiante de prueba con su mora.
UPDATE estudiante e
INNER JOIN usuario u ON u.id_usuario = e.id_usuario
SET e.sancionado = TRUE,
    u.estado = 'SANCIONADO'
WHERE u.codigo = 'EST002';
