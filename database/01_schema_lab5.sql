-- =========================================================
-- IF0009 - Desarrollo de Software IV
-- Laboratorio 5: ExpresoFast Parte I
-- Esquema base (reconstruido a partir de las entidades JPA del proyecto)
-- Ejecutar PRIMERO, antes de 02_schema_lab6_extension.sql
-- =========================================================

IF DB_ID('ExpresoFast_C5H153') IS NULL
BEGIN
    CREATE DATABASE ExpresoFast_C5H153;
END
GO

USE ExpresoFast_C5H153;
GO

-- ---------------------------------------------------------
-- EmpresaLogistica
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.EmpresaLogistica', 'U') IS NOT NULL DROP TABLE dbo.EmpresaLogistica;
GO
CREATE TABLE EmpresaLogistica (
    empresa_id       INT IDENTITY(1,1) PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica  VARCHAR(20)  NOT NULL UNIQUE,
    telefono         VARCHAR(20)  NOT NULL,
    fecha_registro   DATETIME     NOT NULL
);
GO

-- ---------------------------------------------------------
-- Vehiculo
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.vehiculo', 'U') IS NOT NULL DROP TABLE dbo.vehiculo;
GO
CREATE TABLE vehiculo (
    vehiculo_id   INT IDENTITY(1,1) PRIMARY KEY,
    placa         VARCHAR(15) NOT NULL UNIQUE,
    capacidad_kg  DECIMAL(10,2) NOT NULL,
    estado        VARCHAR(20) NOT NULL,
    empresa_id    INT NOT NULL,
    CONSTRAINT FK_Vehiculo_Empresa FOREIGN KEY (empresa_id) REFERENCES EmpresaLogistica(empresa_id)
);
GO

-- ---------------------------------------------------------
-- Conductor
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.conductor', 'U') IS NOT NULL DROP TABLE dbo.conductor;
GO
CREATE TABLE conductor (
    conductor_id  INT IDENTITY(1,1) PRIMARY KEY,
    nombre        VARCHAR(50) NOT NULL,
    apellidos     VARCHAR(50) NOT NULL,
    licencia      VARCHAR(20) NOT NULL UNIQUE,
    telefono      VARCHAR(20) NOT NULL
);
GO

-- ---------------------------------------------------------
-- Envio
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.Envio', 'U') IS NOT NULL DROP TABLE dbo.Envio;
GO
CREATE TABLE Envio (
    envio_id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo     VARCHAR(30) NOT NULL UNIQUE,
    direccion_destino  VARCHAR(200) NOT NULL,
    peso_kg            DECIMAL(10,2) NOT NULL,
    costo              DECIMAL(10,2) NOT NULL,
    estado_envio       VARCHAR(20) NOT NULL,
    vehiculo_id        INT NOT NULL,
    conductor_id       INT NOT NULL,
    fecha_creacion     DATETIME NULL,
    fecha_modificacion DATETIME NULL,
    CONSTRAINT FK_Envio_Vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(vehiculo_id),
    CONSTRAINT FK_Envio_Conductor FOREIGN KEY (conductor_id) REFERENCES conductor(conductor_id)
);
GO
