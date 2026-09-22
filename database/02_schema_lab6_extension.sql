-- =========================================================
-- IF0009 - Desarrollo de Software IV
-- Laboratorio 6: ExpresoFast Parte II
-- Extensión de esquema actualizada (consistente con snake_case y JPA)
-- Ejecutar sobre la misma base de datos del Laboratorio 5
-- (ExpresoFast_C5H153) DESPUÉS del script de esquema base
-- =========================================================

USE ExpresoFast_C5H153;
GO

-- ---------------------------------------------------------
-- 1. Tabla usuario
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
GO
CREATE TABLE usuario (
    usuario_id          INT IDENTITY(1,1) PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    nombre_completo     VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    activo              BIT          NOT NULL DEFAULT 1
);
GO

-- ---------------------------------------------------------
-- 2. Tabla rol
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.rol', 'U') IS NOT NULL DROP TABLE dbo.rol;
GO
CREATE TABLE rol (
    rol_id        INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol    VARCHAR(30) NOT NULL UNIQUE
);
GO

-- ---------------------------------------------------------
-- 3. Tabla usuario_rol (muchos a muchos)
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.usuario_rol', 'U') IS NOT NULL DROP TABLE dbo.usuario_rol;
GO
CREATE TABLE usuario_rol (
    usuario_id INT NOT NULL,
    rol_id     INT NOT NULL,
    CONSTRAINT PK_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT FK_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id),
    CONSTRAINT FK_usuario_rol_rol FOREIGN KEY (rol_id) REFERENCES rol(rol_id)
);
GO

-- ---------------------------------------------------------
-- 4. Tabla bitacora_envio
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.bitacora_envio', 'U') IS NOT NULL DROP TABLE dbo.bitacora_envio;
GO
CREATE TABLE bitacora_envio (
    bitacora_id         INT IDENTITY(1,1) PRIMARY KEY,
    envio_id            INT NOT NULL,
    estado_anterior     VARCHAR(20) NOT NULL,
    estado_nuevo        VARCHAR(20) NOT NULL,
    fecha_cambio        DATETIME NOT NULL,
    usuario_id          INT NOT NULL,
    observaciones       VARCHAR(250) NULL,
    CONSTRAINT FK_bitacora_envio FOREIGN KEY (envio_id) REFERENCES envio(envio_id),
    CONSTRAINT FK_bitacora_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);
GO