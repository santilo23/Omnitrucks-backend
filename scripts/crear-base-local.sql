-- Prepara la base de OmniTrucks en un PostgreSQL instalado localmente.
--
-- Son dos pasos porque el segundo tiene que ejecutarse ya conectado a la base
-- nueva. En pgAdmin no sirve el comando \connect de psql: hay que abrir un
-- Query Tool sobre la base correspondiente.
--
-- OJO CON PGADMIN: ejecuta todo el bloque dentro de una transacción, y
-- CREATE DATABASE no puede correr así. Falla con:
--   ERROR: CREATE DATABASE cannot run inside a transaction block
--
-- Hay dos formas de evitarlo:
--   a) Crear el usuario y la base desde la interfaz, con click derecho sobre
--      "Login/Group Roles" y sobre "Databases" → Create. Es lo más simple.
--   b) Activar "Auto commit" en las opciones de ejecución del Query Tool
--      (último botón de la barra) y ejecutar las sentencias de a una.
--
-- Desde la consola con psql el script corre tal cual, sin este problema.
--
-- No se crean las tablas acá: de eso se encarga Flyway la primera vez que
-- arranca la aplicación. Esto solo crea el usuario y la base vacía.


-- ---------------------------------------------------------------------------
-- PASO 1 — Ejecutar conectado a la base "postgres"
--
--   En pgAdmin: click derecho sobre la base "postgres" → Query Tool,
--   pegar este bloque y ejecutar con F5.
-- ---------------------------------------------------------------------------

-- Usuario de la aplicación. La contraseña es solo para desarrollo local;
-- en producción se pasa por variables de entorno.
CREATE ROLE omnitrucks WITH LOGIN PASSWORD 'omnitrucks';

CREATE DATABASE omnitrucks
    WITH OWNER = omnitrucks
         ENCODING = 'UTF8';


-- ---------------------------------------------------------------------------
-- PASO 2 — Ejecutar conectado a la base "omnitrucks"
--
--   Refrescar el nodo "Databases", después click derecho sobre la base
--   "omnitrucks" recién creada → Query Tool, y ejecutar este bloque.
--
--   Desde PostgreSQL 15 los usuarios comunes ya no pueden crear tablas en el
--   esquema public por defecto. Sin esto, Flyway falla al aplicar la primera
--   migración con un error de permisos.
-- ---------------------------------------------------------------------------

GRANT ALL ON SCHEMA public TO omnitrucks;
ALTER SCHEMA public OWNER TO omnitrucks;
