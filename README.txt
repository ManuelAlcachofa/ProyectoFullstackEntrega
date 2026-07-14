# ProyectoFullstack - Sistema de Gestión de Biblioteca

Manuel Escalona
Ruggieri Alarcón

## Microservicios y puertos

| Servicio | Puerto | Base de datos | Swagger |
|---|---:|---|---|
| API Gateway | 9090 | No aplica | http://localhost:9090/doc/swagger-ui.html |
| UsuarioService | 9091 | bd_biblioteca_usuarios | http://localhost:9091/doc/swagger-ui.html |
| LibroService | 9092 | bd_biblioteca_libros | http://localhost:9092/doc/swagger-ui.html |
| InventarioService | 9093 | bd_biblioteca_inventario | http://localhost:9093/doc/swagger-ui.html |
| CategoriaService | 9094 | bd_biblioteca_categorias | http://localhost:9094/doc/swagger-ui.html |
| AutorService | 9095 | bd_biblioteca_autores | http://localhost:9095/doc/swagger-ui.html |
| MultaService | 9096 | bd_biblioteca_multas | http://localhost:9096/doc/swagger-ui.html |
| ResenaService | 9097 | bd_biblioteca_resenas | http://localhost:9097/doc/swagger-ui.html |
| PrestamoService | 9098 | bd_biblioteca_prestamos | http://localhost:9098/doc/swagger-ui.html |
| ReservaService | 9099 | bd_biblioteca_reservas | http://localhost:9099/doc/swagger-ui.html |
| NotificacionService | 9100 | bd_biblioteca_notificaciones | http://localhost:9100/doc/swagger-ui.html |

## Orden para ejecutar localmente

Primero inicia MySQL en XAMPP. Las bases de datos se crean desde la conexión definida en `application.properties`, usando `createDatabaseIfNotExist=true` en cada `spring.datasource.url`. Luego abre una terminal por servicio y ejecuta:

```powershell
cd UsuarioService
.\mvnw spring-boot:run
```

Orden recomendado:

1. UsuarioService
2. CategoriaService
3. AutorService
4. LibroService
5. InventarioService
6. PrestamoService
7. ReservaService
8. MultaService
9. ResenaService
10. NotificacionService
11. ApiGateway

## Datos iniciales para Postman

Cada microservicio tiene un `DatosInicialesConfig.java` que carga datos de ejemplo automáticamente cuando la tabla está vacía. Por ejemplo:

- Usuario: `Pepe González`, `pepe@gmail.com`, rol `CLIENTE`.
- Libros: `Cien años de soledad`, `La casa de los espíritus`, `Harry Potter y la piedra filosofal`.
- Inventario: stock y copias prestadas por libro.
- Préstamos, reservas, multas, reseñas y notificaciones con datos relacionados por ID.

Para verlos en Postman, levanta cada microservicio y ejecuta los GET principales:

```txt
GET http://localhost:9091/usuarios
GET http://localhost:9092/libros
GET http://localhost:9093/inventario
GET http://localhost:9094/categorias
GET http://localhost:9095/autores
GET http://localhost:9096/multas
GET http://localhost:9097/resenas
GET http://localhost:9098/prestamos
GET http://localhost:9099/reservas
GET http://localhost:9100/notificaciones
```

## API Gateway

Rutas principales mediante Gateway:

```txt
http://localhost:9090/usuarios
http://localhost:9090/libros
http://localhost:9090/inventario
http://localhost:9090/categorias
http://localhost:9090/autores
http://localhost:9090/multas
http://localhost:9090/resenas
http://localhost:9090/prestamos
http://localhost:9090/reservas
http://localhost:9090/notificaciones
```

## Compilar y limpiar

```powershell
.\build-all.bat
.\clean-all.bat
```

## Notas importantes

- Todos los microservicios usan Spring Boot `4.1.0`.
- Todos los microservicios con base de datos incluyen la dependencia exacta `spring-boot-starter-liquibase`.
- Liquibase está desactivado en `application.properties` con `spring.liquibase.enabled=false` para que el proyecto levante con `ddl-auto=update` sin pedir changelogs.
- Swagger/OpenAPI queda disponible en `/doc/swagger-ui.html`.
- Los 3 microservicios nuevos desarrollados son `PrestamoService`, `ReservaService` y `NotificacionService`.

## Documentación Swagger/OpenAPI

Los controladores principales están documentados con anotaciones OpenAPI:

- `@Tag` para agrupar los endpoints por microservicio.
- `@Operation` con resumen y descripción funcional.
- `@ApiResponses` y `@ApiResponse` con códigos HTTP esperados.
- `@Parameter` para IDs, filtros, estados, roles y criterios de búsqueda.
- `@Schema` en todos los DTO, incluyendo descripciones, ejemplos y restricciones.
- `@ExampleObject` con cuerpos JSON válidos y ejemplos de errores.

En Swagger se pueden revisar las rutas, parámetros, modelos, ejemplos de entrada, respuestas exitosas y respuestas de error de cada microservicio.

## Pruebas unitarias

Se incorporaron pruebas unitarias con JUnit 5 y Mockito para la lógica de negocio de los 10 microservicios. Las pruebas usan repositorios y servicios remotos simulados mediante `@Mock`, por lo que no requieren levantar Docker, XAMPP ni una base de datos real.

Casos principales cubiertos:

- Registro, login, actualización y eliminación de usuarios.
- Categorías duplicadas, búsquedas y actualizaciones.
- Creación y actualización de autores y libros.
- Control de stock, préstamos y devoluciones.
- Creación, disponibilidad y cancelación de reservas.
- Emisión, pago y anulación de multas.
- Reseñas duplicadas y cálculo de promedio.
- Creación, validación y lectura de notificaciones.

Para ejecutar las pruebas de un microservicio:

```powershell
cd InventarioService
.\mvnw test
```

El reporte de cobertura JaCoCo se genera en:

```text
target/site/jacoco/index.html
```

El mismo procedimiento se puede repetir en cada microservicio.
