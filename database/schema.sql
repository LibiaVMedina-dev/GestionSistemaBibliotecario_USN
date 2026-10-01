CREATE DATABASE IF NOT EXISTS `PageTurner`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `PageTurner`;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(30)  NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    correo              VARCHAR(254) NOT NULL,
    estado              ENUM('ACTIVO', 'INHABILITADO', 'SANCIONADO')
                        NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuario_codigo UNIQUE (codigo),
    CONSTRAINT uq_usuario_correo UNIQUE (correo)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS estudiante (
    id_usuario          INT UNSIGNED PRIMARY KEY,
    carrera             VARCHAR(120) NOT NULL,
    sancionado          BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_estudiante_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bibliotecario (
    id_usuario          INT UNSIGNED PRIMARY KEY,
    rol                 ENUM('ADMINISTRADOR', 'BIBLIOTECARIO')
                        NOT NULL DEFAULT 'BIBLIOTECARIO',
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_bibliotecario_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS libro (
    isbn                VARCHAR(20)  PRIMARY KEY,
    titulo              VARCHAR(255) NOT NULL,
    autor               VARCHAR(200) NOT NULL,
    stock               INT UNSIGNED NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_libro_stock CHECK (stock >= 0),
    INDEX idx_libro_titulo (titulo),
    INDEX idx_libro_autor (autor)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS prestamo (
    id_prestamo         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_usuario          INT UNSIGNED NOT NULL,
    isbn                VARCHAR(20) NOT NULL,
    fecha_salida        DATE NOT NULL,
    fecha_limite        DATE NOT NULL,
    fecha_devolucion    DATE NULL,
    estado              ENUM('PENDIENTE', 'DEVUELTO', 'VENCIDO')
                        NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_prestamo_estudiante
        FOREIGN KEY (id_usuario) REFERENCES estudiante (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_libro
        FOREIGN KEY (isbn) REFERENCES libro (isbn)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_prestamo_fechas
        CHECK (fecha_limite >= fecha_salida),
    CONSTRAINT chk_prestamo_devolucion
        CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_salida),
    INDEX idx_prestamo_usuario_estado (id_usuario, estado),
    INDEX idx_prestamo_libro_estado (isbn, estado),
    INDEX idx_prestamo_estado_limite (estado, fecha_limite)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sancion (
    id_sancion          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_prestamo         INT UNSIGNED NOT NULL,
    dias_retraso        INT UNSIGNED NOT NULL,
    monto_multa         DECIMAL(10, 2) UNSIGNED NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_sancion_prestamo UNIQUE (id_prestamo),
    CONSTRAINT fk_sancion_prestamo
        FOREIGN KEY (id_prestamo) REFERENCES prestamo (id_prestamo)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_sancion_dias CHECK (dias_retraso >= 0),
    CONSTRAINT chk_sancion_monto CHECK (monto_multa >= 0)
) ENGINE = InnoDB;
