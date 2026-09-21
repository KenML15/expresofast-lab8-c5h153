-- =========================================================
-- IF0009 - Desarrollo de Software IV
-- Laboratorio 6: ExpresoFast Parte II
-- Datos semilla: Roles y Usuarios de prueba
-- Contraseña en texto plano para TODOS los usuarios: Password123!
-- (los hashes fueron generados con BCrypt, factor de costo 10)
-- =========================================================

USE ExpresoFast_C5H153;
GO

-- Roles del sistema
INSERT INTO Rol (nombre_rol) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OPERADOR'),
    ('ROLE_CONDUCTOR');
GO

-- Usuarios de prueba (password_hash = BCrypt de 'Password123!')
INSERT INTO Usuario (username, password_hash, nombre_completo, email, activo) VALUES
    ('admin',      '$2b$10$/KCcyRMlInX6Hz9AIe0BDOGk6b.LCKLNbGi0NLLAlcOTVOZHnrFD6', 'Carlos Alvarado',  'admin@expresofast.cr',      1),
    ('operador1',  '$2b$10$WkkqZJEDQqVD1Ke6GcaS5e.C6CPl25OQ33GS7DysivKisUi/mFzmm', 'Maria Rodriguez',  'operador1@expresofast.cr',  1),
    ('conductor1', '$2b$10$hHeP3tLycKb.spKxAtyD7eULbt9uy.HxaRIT1O4VAsTPsXH6Bg7rq', 'Jose Fernandez',   'conductor1@expresofast.cr', 1);
GO

-- Asignación de roles (usuario_id 1=admin, 2=operador1, 3=conductor1 -> rol_id 1=ADMIN, 2=OPERADOR, 3=CONDUCTOR)
INSERT INTO UsuarioRol (usuario_id, rol_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3);
GO
