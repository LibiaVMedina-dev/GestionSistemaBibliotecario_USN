-- Tabularis Dump
-- Database: USN
-- Date: 2026-10-01T16:53:21.463003300-05:00

-- Structure for table `bibliotecario`
DROP TABLE IF EXISTS `bibliotecario`;
CREATE TABLE `bibliotecario` (
  `id_usuario` int(10) unsigned NOT NULL,
  `rol` enum('ADMINISTRADOR','BIBLIOTECARIO') NOT NULL DEFAULT 'BIBLIOTECARIO',
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_bibliotecario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `bibliotecario`
INSERT INTO `bibliotecario` VALUES (1, 'ADMINISTRADOR', '2026-10-01 21:40:51', '2026-10-01 21:40:51'), (2, 'BIBLIOTECARIO', '2026-10-01 21:40:52', '2026-10-01 21:40:52');


-- Structure for table `estudiante`
DROP TABLE IF EXISTS `estudiante`;
CREATE TABLE `estudiante` (
  `id_usuario` int(10) unsigned NOT NULL,
  `carrera` varchar(120) NOT NULL,
  `sancionado` tinyint(1) NOT NULL DEFAULT 0,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_estudiante_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `estudiante`
INSERT INTO `estudiante` VALUES (3, 'Sistemas', 0, '2026-10-01 21:40:52', '2026-10-01 21:40:52'), (4, 'Sistemas', 1, '2026-10-01 21:40:52', '2026-10-01 21:40:55'), (5, 'Contabilidad', 0, '2026-10-01 21:40:52', '2026-10-01 21:45:43'), (6, 'Industrial', 0, '2026-10-01 21:40:53', '2026-10-01 21:40:53');


-- Structure for table `libro`
DROP TABLE IF EXISTS `libro`;
CREATE TABLE `libro` (
  `isbn` varchar(20) NOT NULL,
  `titulo` varchar(255) NOT NULL,
  `autor` varchar(200) NOT NULL,
  `stock` int(10) unsigned NOT NULL DEFAULT 0,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `anio` char(4) DEFAULT NULL,
  `categoria` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`isbn`),
  KEY `idx_libro_titulo` (`titulo`),
  KEY `idx_libro_autor` (`autor`),
  KEY `idx_libro_categoria` (`categoria`),
  CONSTRAINT `chk_libro_stock` CHECK (`stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `libro`
INSERT INTO `libro` VALUES ('9788497860011', 'Clean Code', 'Robert C. Martin', 4, '2026-10-01 21:43:28', '2026-10-01 21:43:28', '2008', 'Programacion'), ('9788497860028', 'Introduccion a la Programacion Orientada a Objetos', 'James Eckel', 3, '2026-10-01 21:43:28', '2026-10-01 21:43:28', '2013', 'Programacion'), ('9788497860035', 'Java Como Programar', 'Harvey Deitel', 2, '2026-10-01 21:43:28', '2026-10-01 21:43:28', '2018', 'Programacion'), ('9788497860042', 'Base de Datos Relacionales', 'Thomas Connolly', 5, '2026-10-01 21:43:29', '2026-10-01 21:43:29', '2015', 'Base de Datos'), ('9788497860059', 'Ingenieria de Software', 'Ian Sommerville', 0, '2026-10-01 21:43:29', '2026-10-01 21:43:29', '2016', 'Ingenieria');


-- Structure for table `prestamo`
DROP TABLE IF EXISTS `prestamo`;
CREATE TABLE `prestamo` (
  `id_prestamo` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int(10) unsigned NOT NULL,
  `isbn` varchar(20) NOT NULL,
  `fecha_salida` date NOT NULL,
  `fecha_limite` date NOT NULL,
  `fecha_devolucion` date DEFAULT NULL,
  `estado` enum('PENDIENTE','DEVUELTO','VENCIDO') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id_prestamo`),
  KEY `idx_prestamo_usuario_estado` (`id_usuario`,`estado`),
  KEY `idx_prestamo_libro_estado` (`isbn`,`estado`),
  KEY `idx_prestamo_estado_limite` (`estado`,`fecha_limite`),
  CONSTRAINT `fk_prestamo_estudiante` FOREIGN KEY (`id_usuario`) REFERENCES `estudiante` (`id_usuario`) ON UPDATE CASCADE,
  CONSTRAINT `fk_prestamo_libro` FOREIGN KEY (`isbn`) REFERENCES `libro` (`isbn`) ON UPDATE CASCADE,
  CONSTRAINT `chk_prestamo_fechas` CHECK (`fecha_limite` >= `fecha_salida`),
  CONSTRAINT `chk_prestamo_devolucion` CHECK (`fecha_devolucion` is null or `fecha_devolucion` >= `fecha_salida`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `prestamo`
INSERT INTO `prestamo` VALUES (10, 3, '9788497860011', '2026-09-30', '2026-10-03', NULL, 'PENDIENTE', '2026-10-01 21:43:29', '2026-10-01 21:43:29'), (11, 4, '9788497860035', '2026-09-25', '2026-09-28', NULL, 'PENDIENTE', '2026-10-01 21:43:29', '2026-10-01 21:43:29'), (12, 3, '9788497860042', '2026-09-21', '2026-09-24', '2026-09-23', 'DEVUELTO', '2026-10-01 21:43:29', '2026-10-01 21:43:29');


-- Structure for table `sancion`
DROP TABLE IF EXISTS `sancion`;
CREATE TABLE `sancion` (
  `id_sancion` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_prestamo` int(10) unsigned NOT NULL,
  `dias_retraso` int(10) unsigned NOT NULL,
  `monto_multa` decimal(10,2) unsigned NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `pagada` tinyint(4) NOT NULL,
  PRIMARY KEY (`id_sancion`),
  UNIQUE KEY `uq_sancion_prestamo` (`id_prestamo`),
  KEY `idx_sancion_pagada` (`pagada`),
  CONSTRAINT `fk_sancion_prestamo` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamo` (`id_prestamo`) ON UPDATE CASCADE,
  CONSTRAINT `chk_sancion_dias` CHECK (`dias_retraso` >= 0),
  CONSTRAINT `chk_sancion_monto` CHECK (`monto_multa` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `sancion`
INSERT INTO `sancion` VALUES (1, 11, 3, '15.00', '2026-10-01 21:44:31', '2026-10-01 21:44:31', 0);


-- Structure for table `usuario`
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id_usuario` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `codigo` varchar(30) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `correo` varchar(254) NOT NULL,
  `estado` enum('ACTIVO','INHABILITADO','SANCIONADO') NOT NULL DEFAULT 'ACTIVO',
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `dni` varchar(10) NOT NULL,
  `contrasena` char(64) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuario_codigo` (`codigo`),
  UNIQUE KEY `uq_usuario_correo` (`correo`),
  UNIQUE KEY `uq_usuario_dni` (`dni`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data for table `usuario`
INSERT INTO `usuario` VALUES (1, 'N00156923', 'Libia Elena Vasquez Medina', 'anaquispe@universidad.edu', 'ACTIVO', '2026-10-01 21:40:50', '2026-10-01 21:49:54', '70111233', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'), (2, 'N00569399', 'Cristhian Arturo Tacuri Rosales', 'marcoparedes@universidad.edu', 'ACTIVO', '2026-10-01 21:40:50', '2026-10-01 21:49:54', '70222344', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'), (3, 'N00476546', 'Diogo Enilson Tomas Espinoza', 'luisramos@universidad.edu', 'ACTIVO', '2026-10-01 21:40:50', '2026-10-01 21:49:54', '45011222', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'), (4, 'N00452118', 'David Samuel Salas Chuco   ', 'mariachavez@universidad.edu', 'SANCIONADO', '2026-10-01 21:40:51', '2026-10-01 21:49:54', '45011333', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'), (5, 'N00312345', 'Carlos Mamani Ortiz', 'carlosmamani@universidad.edu', 'SANCIONADO', '2026-10-01 21:40:51', '2026-10-01 21:49:54', '45011444', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92'), (6, 'N00423456', 'Ana Lucia Torres Paz', 'analucia@universidad.edu', 'INHABILITADO', '2026-10-01 21:40:51', '2026-10-01 21:49:53', '45011555', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');


