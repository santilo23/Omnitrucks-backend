# OmniTrucks — Backend

API REST del sistema **OmniTrucks**, una aplicación para el seguimiento en tiempo real de camiones que trasladan mercadería entre provincias.

Este repositorio contiene el servidor. La aplicación móvil vive en un repositorio separado: [Omnitrucks-frontend](https://github.com/santilo23/Omnitrucks-frontend) (React Native + Expo).

---

## ¿Qué hace?

Un transporte tiene camiones, choferes y viajes. Cuando un viaje se inicia, el camión empieza a reportar su posición GPS periódicamente. El backend recibe esas posiciones, las almacena y las expone para que la app móvil pueda mostrar:

- Dónde está cada camión en curso, en un mapa.
- El recorrido que hizo hasta el momento.
- El estado del viaje y su hora estimada de llegada.

Según el rol del usuario, la vista cambia:

| Rol | Qué puede hacer |
|---|---|
| `ADMIN` | Gestionar camiones, choferes, clientes y viajes. Ver todo. |
| `CHOFER` | Ver sus viajes asignados y reportar su posición. |
| `CLIENTE` | Seguir en el mapa los envíos que le corresponden. |

---

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4.1.1 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL 17 |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT |
| Build | Maven (con wrapper incluido) |
| Utilidades | Lombok, Bean Validation |

---

## Requisitos previos

- **JDK 25** — verificar con `java -version`
- **Docker Desktop** — para levantar PostgreSQL sin instalarlo en el sistema
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`)

---

## Cómo levantar el proyecto

> Todos los comandos se ejecutan desde la raíz del repositorio.

**1. Levantar la base de datos**

```bash
docker compose up -d
```

Esto arranca PostgreSQL 17 en `localhost:5433` con la base `omnitrucks` (usuario y contraseña `omnitrucks`). Los datos persisten en un volumen de Docker entre reinicios.

> Se usa el puerto **5433** y no el 5432 por defecto para no chocar con un PostgreSQL instalado localmente. Si preferís usar otra base, sobreescribí `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con variables de entorno.

**2. Arrancar la aplicación**

```bash
./mvnw spring-boot:run
```

En Windows con PowerShell:

```bash
.\mvnw.cmd spring-boot:run
```

Si no se indica perfil, arranca con `dev`. Para elegir otro: `-Dspring-boot.run.profiles=dev,sim`.

**3. Verificar que anda**

```bash
curl http://localhost:8080/actuator/health
```

Debería responder `"status":"UP"`, con el detalle del componente `db` también en `UP`.

---

## Problemas conocidos

**`invalid value for parameter "TimeZone": "America/Buenos_Aires"`** — En Windows con zona horaria de Argentina, Java informa un nombre de zona antiguo que PostgreSQL rechaza al conectar. Ya está resuelto: la aplicación fija la JVM en UTC al iniciar (`OmnitrucksApplication`). Si aparece al correr otra herramienta Java contra la base, agregar `-Duser.timezone=UTC`.

**La app móvil no llega al backend** — El servidor ya escucha en toda la red (`server.address: 0.0.0.0` en el perfil `dev`). Si igual no responde desde el celular:
- Verificar la IP actual de la PC con `ipconfig`: el router puede cambiarla de un día para otro, y hay que actualizar el `.env` de la app.
- Permitir Java en el Firewall de Windows para **redes privadas**. Desde la propia PC la conexión funciona igual aunque el firewall esté bloqueando, así que la prueba válida es abrir `http://IP_DE_LA_PC:8080/actuator/health` en Safari desde el celular.

---

## Perfiles de Spring

| Perfil | Para qué sirve |
|---|---|
| `dev` | Desarrollo local. Base de datos en Docker, SQL visible en consola, datos de ejemplo precargados. |
| `sim` | Activa el simulador de recorridos: mueve solos a los camiones de los viajes en curso, sin necesidad de un GPS real. Solo para desarrollo. |
| `prod` | Producción. Toda la configuración sensible viene de variables de entorno. |

Se pueden combinar: `dev,sim` es la configuración habitual mientras se desarrolla la app móvil.

---

## Modelo de datos

```
usuario   ──┐
            ├──< viaje >──── camion
usuario   ──┘        │
(cliente)            └──< posicion
```

| Tabla | Descripción |
|---|---|
| `usuario` | Personas del sistema. El rol (`ADMIN`, `CHOFER`, `CLIENTE`) determina los permisos. Un chofer no es una tabla aparte, es un usuario con rol `CHOFER`. |
| `camion` | Flota. Patente, marca, modelo, capacidad. |
| `viaje` | Un traslado entre un origen y un destino, con un camión y un chofer asignados. |
| `posicion` | Cada coordenada GPS reportada durante un viaje. Es la tabla que más crece. |

**Estados de un viaje:**

```
PROGRAMADO ──> EN_CURSO ──> FINALIZADO
                  ↕
               PAUSADO

(cualquiera) ──> CANCELADO
```

Las transiciones válidas se validan en la capa de servicio.

---

## Estructura del proyecto

El código está organizado **por funcionalidad**, no por capa técnica. Cada paquete contiene su entidad, repositorio, servicio, controlador y DTOs.

```
src/main/java/um/edu/ar/omnitrucks/
├── auth/          Login, emisión y validación de tokens JWT
├── camion/        Gestión de la flota
├── common/        Manejo global de errores, utilidades geográficas
├── config/        Configuración de Spring (seguridad, CORS, WebSocket)
├── posicion/      Ingesta y consulta de coordenadas GPS
├── simulacion/    Simulador de recorridos (solo perfil `sim`)
├── usuario/       Usuarios y roles
└── viaje/         Viajes y su máquina de estados
```

---

## Convenciones del proyecto

- **El esquema lo maneja Flyway**, no Hibernate. `ddl-auto` está en `validate`: si una entidad no coincide con la base, la aplicación no arranca. Todo cambio de esquema es una migración nueva en `src/main/resources/db/migration`.
- **Los DTOs nunca son entidades.** Los controladores reciben y devuelven `record`s dedicados. Exponer entidades JPA filtra campos sensibles y provoca errores de serialización con las relaciones lazy.
- **Las fechas son `Instant` y se guardan en `timestamptz`** (UTC). Nunca `LocalDateTime` para un instante en el tiempo.
- **La lógica de negocio vive en los servicios.** Los controladores solo traducen HTTP.
- **Los errores se devuelven como `ProblemDetail`** (RFC 7807), con un manejador global.

---

## Roadmap

El proyecto se construye por etapas, cada una verificable de forma independiente.

**Fase 1 — Backend con datos simulados**

- [x] Etapa 0 — Docker Compose y preparación del entorno
- [x] Etapa 1 — Cimientos: perfiles, Flyway, Actuator, manejo de errores
- [ ] Etapa 2 — CRUD de camiones
- [ ] Etapa 3 — Viajes y máquina de estados
- [ ] Etapa 4 — Ingesta y consulta de posiciones
- [ ] Etapa 5 — Simulador de recorridos

**Fase 2 — App móvil**

- [ ] Etapa 6 — Proyecto Expo funcionando en el dispositivo
- [ ] Etapa 7 — Mapa con seguimiento en vivo

**Fase 3 — Producción**

- [ ] Etapa 8 — Autenticación JWT y roles
- [ ] Etapa 9 — Modo chofer con GPS real
- [ ] Etapa 10 — Tiempo real con WebSocket
- [ ] Etapa 11 — ETA, geofencing y notificaciones
- [ ] Etapa 12 — Tests, CI y despliegue

---

## Tests

```bash
./mvnw verify
```

---

## Autor

Santiago — Universidad de Mendoza
