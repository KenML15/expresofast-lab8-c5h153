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
