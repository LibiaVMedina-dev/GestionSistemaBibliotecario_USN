# Sistema de BIBLIOTECA UNIVERSIDAD SUPERIOR NOVA

Proyecto Java Swing desarrollado con Java 21 y JDBC. La base de datos puede
ejecutarse en MariaDB o MySQL.

## Preparar la base de datos

Los scripts se corren en este orden:

1. `database/schema.sql` crea la base y las tablas.
2. `database/actualizacion_v2.sql` agrega las columnas que el documento
   del proyecto necesita y que faltaban: `contrasena`, `dni`, `anio`,
   `categoria` y `sancion.pagada`.
3. `database/datos_iniciales.sql` llena la base con usuarios y libros de
   prueba. Se puede correr varias veces sin duplicar registros.
4. `database/actualizacion_v3.sql` reconcilia estudiantes con préstamos
   vencidos o multas pendientes.

Después copia `config/database.properties.example` como
`config/database.properties`, completa el usuario y la contraseña, y elige
`db.vendor=mariadb` o `db.vendor=mysql`. Los dos conectores JDBC ya están
incluidos en `lib/` y se cargan automáticamente.

## Códigos y contraseñas de prueba

El ingreso pide el código institucional **y** la contraseña.

| Código   | Contraseña | Perfil                  | Estado       |
| -------- | ---------- | ----------------------- | ------------ |
| `LIB001` | `admin123` | Administrador           | ACTIVO       |
| `LIB002` | `admin123` | Bibliotecario           | ACTIVO       |
| `EST001` | `123456`   | Estudiante Sistemas     | ACTIVO       |
| `EST002` | `123456`   | Estudiante Sistemas     | SANCIONADO   |
| `EST003` | `123456`   | Estudiante Contabilidad | SANCIONADO   |
| `EST004` | `123456`   | Estudiante Industrial   | INHABILITADO |

Solo pueden iniciar sesión los perfiles `ADMINISTRADOR` y `BIBLIOTECARIO` que
estén activos. Los estudiantes se administran desde el módulo de circulación,
pero no ingresan a la aplicación administrativa. Las contraseñas se guardan
como huella SHA-256, nunca en texto plano (RNF-02).

El administrador ve todos los módulos. El bibliotecario puede gestionar
libros, préstamos y reportes, pero no puede administrar usuarios.

`config/database.properties` está ignorado por Git para evitar publicar
credenciales. También es posible configurar la aplicación con las variables
de entorno `DB_URL`, `DB_VENDOR`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y
`DB_PASSWORD`. Las variables de entorno tienen prioridad sobre el archivo.

La conexión se obtiene mediante:

```java
try (Connection con = ConexionDB.obtenerConexion()) {
    // Usar los DAO con la conexión.
}
```

## Módulos de la interfaz

| Pestaña    | Requisitos que cubre |
| ---------- | -------------------- |
| Libros     | RF-04, RF-05, HU-02, HU-03 |
| Préstamos  | RF-06, RF-07, RF-08, RF-09, HU-04, HU-05, RNF-03 |
| Usuarios   | RF-02, RF-03 |
| Reportes   | RF-10, HU-06 (criterios 1, 2, 3 y 4) |

Reglas de negocio aplicadas: el préstamo dura 3 días (RF-07), la multa es de
S/ 5.00 por cada día de retraso (RF-09) y un estudiante sancionado o
inhabilitado no puede pedir préstamos (HU-04).

## Estructura

- `src`: código fuente Java.
- `src/com/grupo7/config`: conexión a la base y protección de contraseñas.
- `src/com/grupo7/modelos`: clases del modelo.
- `src/com/grupo7/dao`: acceso a datos.
- `src/com/grupo7/vistas`: ventanas Swing del sistema.
- `database`: scripts SQL.
- `config`: plantilla de configuración local.
- `lib`: conectores JDBC.
- `bin`: clases compiladas.

## Ejecutar la aplicación

1. Compila el proyecto apuntando los conectores de `lib/`.
2. Levanta la clase `App`, que abre la ventana de ingreso.

```bash
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src -name "*.java")
java -cp "bin;lib/*" App
```

## Comprobar la conexión

La clase `PruebaBD` hace un health check de la conexión sin abrir la
interfaz gráfica:

```bash
java -cp "bin;lib/*" PruebaBD
```

## Conectores JDBC incluidos

- MariaDB Connector/J 3.5.10: `mariadb-java-client-3.5.10.jar`.
- MySQL Connector/J 26.7.0: `mysql-connector-j-26.7.0.jar`.

Los `.jar` no están excluidos por `.gitignore` y deben mantenerse en el
repositorio para que el proyecto funcione sin una descarga adicional.


## Credenciales de Prueba para Revisión

Para probar el sistema de escritorio Java Swing y la base de datos MariaDB/MySQL:


