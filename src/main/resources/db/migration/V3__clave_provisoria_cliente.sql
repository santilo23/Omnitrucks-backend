-- Migración V3: Soporte para claves provisorias y cambio obligatorio de contraseña
alter table usuario add column debe_cambiar_password boolean not null default false;
