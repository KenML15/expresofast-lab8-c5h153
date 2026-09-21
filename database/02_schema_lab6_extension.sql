-- =========================================================
-- IF0009 - Desarrollo de Software IV
-- Laboratorio 6: ExpresoFast Parte II
-- Extensión de esquema: Usuarios, Roles, UsuarioRol, BitacoraEnvio
-- Ejecutar sobre la misma base de datos del Laboratorio 5
-- (ExpresoFast_C5H153) DESPUÉS de 01_schema_lab5.sql
-- =========================================================

USE ExpresoFast_C5H153;
GO

-- ---------------------------------------------------------
-- 1. Tabla Usuario
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.Usuario', 'U') IS NOT NULL DROP TABLE dbo.Usuario;
GO
CREATE TABLE Usuario (
    usuario_id       INT IDENTITY(1,1) PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    nombre_completo   VARCHAR(100) NOT NULL,
    email             VARCHAR(100) NOT NULL UNIQUE,
    activo            BIT          NOT NULL DEFAULT 1
);
GO

-- ---------------------------------------------------------
-- 2. Tabla Rol
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.Rol', 'U') IS NOT NULL DROP TABLE dbo.Rol;
GO
CREATE TABLE Rol (
    rol_id      INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol  VARCHAR(30) NOT NULL UNIQUE
);
GO

-- ---------------------------------------------------------
-- 3. Tabla UsuarioRol (muchos a muchos)
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.UsuarioRol', 'U') IS NOT NULL DROP TABLE dbo.UsuarioRol;
GO
CREATE TABLE UsuarioRol (
    usuario_id INT NOT NULL,
    rol_id     INT NOT NULL,
    CONSTRAINT PK_UsuarioRol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT FK_UsuarioRol_Usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    CONSTRAINT FK_UsuarioRol_Rol FOREIGN KEY (rol_id) REFERENCES Rol(rol_id)
);
GO

-- ---------------------------------------------------------
-- 4. Tabla BitacoraEnvio
-- ---------------------------------------------------------
IF OBJECT_ID('dbo.BitacoraEnvio', 'U') IS NOT NULL DROP TABLE dbo.BitacoraEnvio;
GO
CREATE TABLE BitacoraEnvio (
    bitacora_id      INT IDENTITY(1,1) PRIMARY KEY,
    envio_id         INT NOT NULL,
    estado_anterior  VARCHAR(20) NOT NULL,
    estado_nuevo     VARCHAR(20) NOT NULL,
    fecha_cambio     DATETIME NOT NULL,
    usuario_id       INT NOT NULL,
    observaciones    VARCHAR(250) NULL,
    CONSTRAINT FK_Bitacora_Envio FOREIGN KEY (envio_id) REFERENCES Envio(envio_id),
    CONSTRAINT FK_Bitacora_Usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);
GO
