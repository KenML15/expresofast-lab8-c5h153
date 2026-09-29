# ExpresoFast - Sistema de Gestión Logística

Sistema web desarrollado para la administración y control de envíos de paquetería, flotas de vehículos y conductores, implementando seguridad basada en tokens JWT y control de accesos por roles.

##  Tecnologías Utilizadas

* **Backend:** Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA / Hibernate.
* **Base de Datos:** Microsoft SQL Server.
* **Frontend:** HTML5, CSS3, JavaScript (Vanilla JS), Fetch API.

---

##  Roles de Usuario y Permisos

* **`ROLE_ADMIN` (Administrador):** Acceso total al sistema, visualización de estadísticas KPI y consulta de la bitácora de auditoría de cambios.
* **`ROLE_OPERADOR` (Operador):** Registro de nuevos envíos y actualización de estados iniciales (de Pendiente a En Tránsito).
* **`ROLE_CONDUCTOR` (Conductor):** Visualización de rutas asignadas y confirmación de entrega final (de En Tránsito a Entregado).

---

##  Instrucciones de Configuración y Ejecución Local

### 1. Base de Datos (SQL Server)
1. Crea una base de datos en SQL Server con el nombre: `ExpresoFast_C5H153`.
2. Ejecuta los scripts de inserción semilla para poblar las tablas de empresa, vehículos, conductores y usuarios con los datos iniciales requeridos.

### 2. Configuración del Backend
1. Abre el proyecto backend en tu IDE de preferencia (IntelliJ IDEA, Eclipse, etc.).
2. Verifica las credenciales de tu base de datos en el archivo `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ExpresoFast_C5H153;encrypt=true;trustServerCertificate=true
   spring.datasource.username=
   spring.datasource.password=
   ```
3. La contraseña se lee de la variable de entorno `DB_PASSWORD`.

---

## Laboratorio 10 — Consola logística SPA con Angular

A partir de este laboratorio el cliente web es una **Single Page Application en Angular Standalone** (`expresofast-frontend/`) que consume la API RESTful de Spring Boot (`backend/expresofast/`). La consola Vanilla JS de laboratorios anteriores se conserva en `frontend/` solo como referencia.

### Estructura

```
backend/expresofast/            <- Spring Boot (Java 21)
  domain/Envio.java             Entidad JPA (codigoRastreo único, destinatario, dirección, flete, estado, fechaCreacion)
  data/EnvioRepository.java     Búsqueda por código de rastreo y filtrado por estado
  dto/EnvioDTO.java             Respuesta de la API
  dto/CrearEnvioDTO.java        Payload de registro (destinatario, dirección, montoFlete)
  business/EnvioService.java    Reglas de negocio; genera códigos EXP-AAAA-XXXX
  controller/EnvioController.java
expresofast-frontend/           <- Angular Standalone
  src/environments/environment.ts   API_URL = http://localhost:8080/api/v1/
  src/app/models/envio.model.ts     Envio, CrearEnvioPayload
  src/app/services/envio.service.ts HttpClient: GET, POST, PATCH
  src/app/components/envio-list/    /envios
  src/app/components/envio-form/    /nuevo-envio
  src/app/components/envio-tracking/ /rastreo
  src/app/app.config.ts             provideHttpClient(withFetch(), ...)
  src/app/app.routes.ts             Redirección por defecto a /envios
```

### Endpoints (`/api/v1/envios`, requieren JWT)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/envios` | Lista de todos los envíos (con `?page=` retorna la versión paginada del Lab 9) |
| `GET` | `/api/v1/envios/rastreo/{codigo}` | Detalle del envío por código de rastreo |
| `POST` | `/api/v1/envios` | Registra un envío desde `CrearEnvioDTO`; genera `EXP-2026-XXXX` y estado `PENDIENTE` |
| `PATCH` | `/api/v1/envios/{id}/estado` | Actualiza el estado (`PENDIENTE`, `EN_TRANSITO`, `ENTREGADO`, `CANCELADO`) |

CORS: `@CrossOrigin(origins = "http://localhost:4200")` en el controlador y en `SecurityConfig`.

### Ejecución

1. **Base de datos:** ejecutar en orden los scripts de `database/`, incluido `04_schema_lab10_destinatario.sql` (agrega la columna `destinatario`).
2. **Backend** (puerto 8080):
   ```bash
   cd backend/expresofast
   ./mvnw spring-boot:run
   ```
3. **Frontend** (puerto 4200):
   ```bash
   cd expresofast-frontend
   npm install
   ng serve
   ```
4. Abrir `http://localhost:4200`, iniciar sesión con un usuario de `03_data_seeds.sql` y navegar con la barra superior entre **Envíos**, **Nuevo envío** y **Rastrear guía**.

### Pruebas del frontend

```bash
cd expresofast-frontend
ng test --watch=false
```
