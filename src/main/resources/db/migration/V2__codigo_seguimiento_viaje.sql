-- Migración V2: Incorporación de código de seguimiento único por viaje.
-- Permite a los clientes consultar su envío de forma segura mediante un código unívoco.

alter table viaje add column codigo_seguimiento varchar(32);

-- Asigna un código único por defecto a cualquier viaje existente
update viaje set codigo_seguimiento = 'TRK-' || lpad(id::text, 6, '0') where codigo_seguimiento is null;

alter table viaje alter column codigo_seguimiento set not null;

create unique index idx_viaje_codigo_seguimiento on viaje (codigo_seguimiento);
