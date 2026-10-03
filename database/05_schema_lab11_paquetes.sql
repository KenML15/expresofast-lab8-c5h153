-- =============================================================
-- Laboratorio 11 - Formularios Reactivos Avanzados
-- Relación 1:N Envio -> Paquetes y fechas operativas del envío
-- =============================================================
USE ExpresoFast_C5H153;
GO

-- Fechas operativas (validación cruzada en Angular)
IF COL_LENGTH('dbo.envio', 'fecha_despacho') IS NULL
    ALTER TABLE dbo.envio ADD fecha_despacho DATE NULL;
GO

IF COL_LENGTH('dbo.envio', 'fecha_entrega_estimada') IS NULL
    ALTER TABLE dbo.envio ADD fecha_entrega_estimada DATE NULL;
GO

-- Tabla de paquetes (script oficial adaptado al esquema existente:
-- la tabla padre es dbo.envio y su PK es envio_id INT)
IF OBJECT_ID('dbo.PAQUETES', 'U') IS NULL
BEGIN
    CREATE TABLE PAQUETES (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        envio_id    INT NOT NULL,
        descripcion VARCHAR(255) NOT NULL,
        peso_kg     DECIMAL(5,2) NOT NULL,
        CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id)
            REFERENCES dbo.envio(envio_id) ON DELETE CASCADE
    );
END
GO