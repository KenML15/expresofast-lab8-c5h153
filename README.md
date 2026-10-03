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


---

## Laboratorio 11 — Formularios Reactivos Avanzados y Consolidación Full-Stack

Registro de un envío con **N paquetes** en una sola operación (relación 1:N), validación cruzada de fechas
y validación asíncrona del número de rastreo contra la base de datos.

### Cambios realizados

| Capa | Archivo | Descripción |
|---|---|---|
| BD | `database/05_schema_lab11_paquetes.sql` | Tabla `PAQUETES` (FK a `envio` con `ON DELETE CASCADE`) y columnas `fecha_despacho` / `fecha_entrega_estimada` |
| JPA | `domain/Paquete.java`, `domain/Envio.java` | Relación bidireccional `@OneToMany(mappedBy, cascade = ALL, orphanRemoval)` / `@ManyToOne` |
| DTO | `PaqueteDTO`, `EnvioRegistroDTO`, `EnvioConPaquetesDTO`, `TrackingCheckDTO` | `List<@Valid PaqueteDTO>` + `@AssertTrue` para validar fechas también en el servidor |
| Servicio | `EnvioService.registrarEnvioConPaquetes()` | `@Transactional`: el envío y todos sus paquetes se insertan o se revierten juntos |
| API | `EnvioController` | `POST /api/v1/envios/avanzado` y `GET /api/v1/envios/check-tracking/{trackingNumber}` |
| Angular | `validators/envio.validators.ts` | `fechasEnvioValidator` (síncrono, FormGroup) y `trackingDisponibleValidator` (asíncrono) |
| Angular | `components/envio-avanzado-form/` | `EnvioAvanzadoFormComponent`: Typed Forms + `FormArray` (ruta `/envio-avanzado`) |

> **Nota sobre el script oficial:** el enunciado referencia `ENVIOS(id) BIGINT`, pero en este proyecto la tabla
> existente es `dbo.envio` con llave primaria `envio_id INT`. Como SQL Server exige que la FK tenga el mismo tipo
> que la PK referenciada, la columna `PAQUETES.envio_id` se declaró `INT` y apunta a `envio(envio_id)`.

### Endpoints nuevos (requieren JWT)

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/v1/envios/check-tracking/{trackingNumber}` | `200 { "numeroTracking": "...", "existe": true/false }` |
| POST | `/api/v1/envios/avanzado` | `201` envío con paquetes · `400` validación · `409` tracking duplicado |

### Ejecución

1. Ejecutar `database/05_schema_lab11_paquetes.sql` sobre `ExpresoFast_C5H153`.
2. Backend: `cd expresofast-backend/expresofast` → `$env:DB_PASSWORD="..."` → `.\mvnw.cmd spring-boot:run`
3. Frontend: `cd expresofast-frontend` → `npm install` → `npm start`
4. Abrir `http://localhost:4200`, iniciar sesión y entrar a **Envío con paquetes**.

---

## Fundamentación teórica

### 1. UX y escalabilidad: ¿por qué `FormArray` + formularios reactivos y no 10 campos estáticos ocultos?

**Modelo de datos fiel al dominio.** Un envío tiene *N* paquetes, donde N no se conoce de antemano. Un `FormArray<FormGroup<{ descripcion; pesoKg }>>`
representa exactamente esa cardinalidad 1:N, la misma que existe en la base de datos (`envio` 1 — N `PAQUETES`) y en el DTO
(`List<PaqueteDTO>`). Con 10 campos estáticos se impone un **límite artificial** (¿qué pasa con el paquete 11?) y una
estructura que no corresponde al modelo: habría que traducir `descripcion1…descripcion10` a una lista antes de enviarla.

**Experiencia de usuario.**
- El operador ve **solo los bloques que necesita**. No hay 9 bloques vacíos ocultos que se puedan "colar" en el envío,
  ni que confundan a lectores de pantalla o a la navegación con Tab.
- La validación es **por bloque y en tiempo real**: cada `FormGroup` del arreglo tiene sus propios validadores
  (`required`, `min(0.01)`, `max(999.99)`), y el error aparece junto al paquete exacto que lo tiene.
- El estado del formulario (`valid`, `invalid`, `pending`) se **calcula de forma agregada**: si un solo paquete es inválido,
  el `FormArray` es inválido y, por propagación, también el `FormGroup` raíz. Así el botón Submit se deshabilita sin
  escribir lógica adicional.
- La regla de negocio "mínimo 1 paquete" se aplica antes de llamar a `removeAt()` y además se refleja en la UI
  deshabilitando el botón X.

**Mantenibilidad.**
- **Fuente única de verdad en TypeScript.** La estructura, los valores iniciales y las validaciones viven en una
  definición declarativa (`fb.group` / `fb.array`) y no repartidos en atributos HTML. Agregar un campo nuevo a los
  paquetes (por ejemplo `fragil: boolean`) se hace en **un solo lugar**, `crearPaquete()`, y aplica a todos los
  bloques. Con campos estáticos habría que copiarlo 10 veces en HTML, en validación y en el mapeo.
- **Tipado estricto (Typed Forms).** `FormControl<number | null>` hace que `pesoKg.setValue('abc')` sea un **error de
  compilación**, y `getRawValue()` retorna un objeto tipado que encaja con `EnvioRegistroPayload`. Los errores se
  detectan en el editor, no en producción.
- **Flujo de datos síncrono y explícito.** A diferencia de `[(ngModel)]` (template-driven), donde el modelo se crea
  de forma implícita desde la plantilla y se actualiza de forma asíncrona, en los formularios reactivos el componente
  es dueño del modelo. Eso permite **probarlo sin DOM** (pruebas unitarias sobre el `FormGroup`) y reaccionar a cambios
  con `valueChanges` (Observables).
- **Composición y reutilización.** Los validadores son funciones puras (`ValidatorFn`, `AsyncValidatorFn`) en un
  archivo aparte, reutilizables en otros formularios y probables de forma aislada.
- **Rendimiento del DOM.** `@for (...; track paquete)` rastrea cada bloque por la referencia de su control. Al eliminar
  el paquete 2 de 3, Angular remueve **solo ese nodo** y conserva el estado (valor y foco) de los demás. Con
  campos ocultos, el DOM siempre carga los 10 bloques aunque se usen 2.

### 2. Ciclo de eventos: validador síncrono vs. validador asíncrono

JavaScript se ejecuta en **un solo hilo** con una **pila de llamadas (call stack)**. Las operaciones lentas (temporizadores,
red) las delega a las **Web APIs** del navegador. Cuando terminan, sus callbacks se encolan: los de `setTimeout` en la
**cola de tareas (macrotasks)** y los de Promesas en la **cola de microtareas**. El **Event Loop** toma un callback
de las colas solo cuando la pila está vacía, y siempre vacía todas las microtareas antes de pasar a la siguiente macrotarea.

**Validador cruzado de fechas (síncrono): `fechasEnvioValidator`**
1. El usuario cambia una fecha. El evento `input` entra a la pila y Angular llama a `setValue()` →
   `updateValueAndValidity()` del control y luego del `FormGroup` padre.
2. Dentro de esa misma ejecución, Angular invoca `fechasEnvioValidator(grupo)`. La función lee dos valores que ya
   están en memoria, los compara y **retorna inmediatamente** `null` o `{ rangoFechasInvalido: true }`.
3. Cuando el handler del evento termina, el estado del formulario ya es `VALID` o `INVALID`. No se encoló nada ni
   hubo espera: todo ocurrió **en el mismo tick, de principio a fin en la pila**. Por eso puede retornar un valor directo
   (`ValidationErrors | null`): el resultado existe en el momento en que se pide.

**Validador de tracking (asíncrono): `trackingDisponibleValidator`**
1. Primero se ejecutan los validadores síncronos del control (`required`, `pattern`, `maxLength`). **Solo si todos
   pasan**, Angular ejecuta el asíncrono, así que no se consulta el API con datos inválidos.
2. Angular marca el control como **`PENDING`** y **se suscribe** al Observable que retornó el validador. La función
   retorna de inmediato y **la pila queda libre**: la UI sigue respondiendo, el usuario puede seguir escribiendo y la
   plantilla muestra "Verificando…".
3. `timer(400)` registra un `setTimeout` en las Web APIs. Si el usuario escribe otra tecla antes de que se cumpla,
   Angular **cancela (unsubscribe)** el Observable anterior, lo que limpia el temporizador, y crea uno nuevo. Ese es el
   *debounce*: solo sobrevive la última consulta.
4. Al vencer el temporizador, su callback entra a la **cola de macrotareas**. Cuando la pila está vacía, el Event Loop lo
   ejecuta y `switchMap` dispara la petición HTTP (`HttpClient` con `withFetch()`, que usa `fetch`). La red la atiende el
   navegador, **fuera del hilo de JavaScript**.
5. Cuando llega la respuesta, la Promesa de `fetch` se resuelve, su continuación entra a la **cola de microtareas** y
   el Observable emite `existe: true/false`. `map` lo convierte en `{ trackingTomado: true }` o `null`, `take(1)` completa
   el flujo, y Angular aplica el resultado con `setErrors()`, que cambia el estado de `PENDING` a `VALID` o `INVALID` y
   programa la detección de cambios para repintar el mensaje.

**¿Por qué Angular exige un `Observable` o `Promise` en el validador asíncrono?**
- Porque **el resultado no existe en el momento en que se invoca la función**: depende de una respuesta de red que
  llegará en el futuro. Una función de JavaScript solo puede retornar un valor ya calculado. Para retornar un `boolean`
  "real" tendría que **bloquear el único hilo** hasta que responda el servidor, y eso congelaría la página entera
  (sin escribir, sin clics, sin repintar).
- `Observable` y `Promise` son **contratos de valor futuro**: le permiten a Angular registrar un callback
  (suscripción / `.then`) que se ejecutará cuando el Event Loop entregue la respuesta, y mientras tanto exponer el
  estado intermedio `PENDING`. Ese estado es el que usamos para deshabilitar el Submit (`form.pending`).
- El `Observable` tiene además una ventaja sobre la `Promise`: **es cancelable**. Angular se desuscribe de la validación
  anterior en cada cambio de valor, lo que evita *race conditions*: una respuesta lenta de una consulta vieja
  ("EXP-1") no puede sobrescribir el resultado de la consulta actual ("EXP-123").
- Angular espera que el Observable **se complete**. Si nunca se completa, el control queda en `PENDING`
  indefinidamente. Por eso el validador termina con `take(1)`.