-- =============================================================
-- Laboratorio 10 - Migración a SPA Angular
-- Agrega el destinatario del envío y permite registrar guías
-- sin vehículo/conductor asignado (se asignan posteriormente).
-- =============================================================
USE ExpresoFast_C5H153;
GO

IF COL_LENGTH('dbo.envio', 'destinatario') IS NULL
    ALTER TABLE dbo.envio ADD destinatario VARCHAR(120) NULL;
GO

ALTER TABLE dbo.envio ALTER COLUMN vehiculo_id INT NULL;
GO

ALTER TABLE dbo.envio ALTER COLUMN conductor_id INT NULL;
GO

ALTER TABLE dbo.envio ALTER COLUMN peso_kg DECIMAL(10,2) NULL;
GO

-- Los envíos existentes toman como destinatario el nombre del conductor asignado
UPDATE e
SET e.destinatario = c.nombre + ' ' + c.apellidos
FROM dbo.envio e
INNER JOIN dbo.conductor c ON c.conductor_id = e.conductor_id
WHERE e.destinatario IS NULL;
GO
