-- Esquema inicial de OmniTrucks.
--
-- Convenciones:
--   * Los instantes se guardan en timestamptz (UTC) y se mapean a Instant en Java.
--   * El chofer no es una tabla aparte: es un usuario con rol CHOFER.
--   * Los enumerados se guardan como texto con un check, para que la base siga
--     siendo legible sin la aplicación.

create table usuario (
    id            bigint generated always as identity primary key,
    email         varchar(120) not null unique,
    password_hash varchar(100) not null,
    nombre        varchar(60)  not null,
    apellido      varchar(60)  not null,
    rol           varchar(20)  not null,
    activo        boolean      not null default true,
    created_at    timestamptz  not null default now(),

    constraint usuario_rol_valido check (rol in ('ADMIN', 'CHOFER', 'CLIENTE'))
);

create table camion (
    id           bigint generated always as identity primary key,
    patente      varchar(10) not null unique,
    marca        varchar(40) not null,
    modelo       varchar(40) not null,
    anio         integer,
    capacidad_kg integer,
    activo       boolean     not null default true,
    created_at   timestamptz not null default now(),

    constraint camion_anio_valido      check (anio is null or anio between 1950 and 2100),
    constraint camion_capacidad_valida check (capacidad_kg is null or capacidad_kg > 0)
);

create table viaje (
    id                bigint generated always as identity primary key,
    camion_id         bigint       not null references camion (id),
    chofer_id         bigint       not null references usuario (id),
    cliente_id        bigint       references usuario (id),

    origen_nombre     varchar(120) not null,
    origen_lat        double precision not null,
    origen_lon        double precision not null,
    destino_nombre    varchar(120) not null,
    destino_lat       double precision not null,
    destino_lon       double precision not null,

    estado            varchar(20)  not null,
    descripcion_carga varchar(255),

    salida_programada timestamptz,
    salida_real       timestamptz,
    llegada_estimada  timestamptz,
    llegada_real      timestamptz,
    created_at        timestamptz  not null default now(),

    constraint viaje_estado_valido check (
        estado in ('PROGRAMADO', 'EN_CURSO', 'PAUSADO', 'FINALIZADO', 'CANCELADO')
    ),
    constraint viaje_origen_valido  check (origen_lat between -90 and 90 and origen_lon between -180 and 180),
    constraint viaje_destino_valido check (destino_lat between -90 and 90 and destino_lon between -180 and 180)
);

create index idx_viaje_estado on viaje (estado);
create index idx_viaje_camion on viaje (camion_id);
create index idx_viaje_chofer on viaje (chofer_id);

-- Un camión no puede estar en dos viajes a la vez. El índice parcial deja que
-- la base garantice la regla, además de la validación en el servicio.
create unique index idx_viaje_camion_en_ruta
    on viaje (camion_id)
    where estado in ('EN_CURSO', 'PAUSADO');

create table posicion (
    id            bigint generated always as identity primary key,
    viaje_id      bigint not null references viaje (id) on delete cascade,
    latitud       double precision not null,
    longitud      double precision not null,
    velocidad_kmh double precision,
    rumbo_grados  double precision,
    precision_m   double precision,

    -- Momento en el dispositivo y momento en el servidor. Guardar los dos
    -- permite detectar posiciones que llegaron tarde por falta de señal.
    registrado_en timestamptz not null,
    recibido_en   timestamptz not null default now(),

    constraint posicion_coordenada_valida check (latitud between -90 and 90 and longitud between -180 and 180),
    constraint posicion_rumbo_valido      check (rumbo_grados is null or rumbo_grados between 0 and 360),
    constraint posicion_velocidad_valida  check (velocidad_kmh is null or velocidad_kmh >= 0)
);

-- Índice principal de consulta: la última posición de un viaje y su recorrido.
create index idx_posicion_viaje_tiempo on posicion (viaje_id, registrado_en desc);
