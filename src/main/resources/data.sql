-- Desactivar temporalmente restricciones de clave foránea
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE transaccion;
TRUNCATE TABLE cuenta_financiera;
TRUNCATE TABLE cliente;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. INSERCIÓN DE CLIENTES (Usando UUID para el id)
-- =============================================================================
INSERT INTO cliente (id, nombre, razon_social, cuil, email, direccion, telefono, cliente_padre_id, created_date, last_modified_date) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Juan Pérez', NULL, 20301112229, 'juan.perez@email.com', 'Av. Principal 123', '3881234567', NULL, NOW(), NOW()),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'María Gómez', NULL, 27312223334, 'maria.gomez@email.com', 'Calle Falsa 456', '3887654321', NULL, NOW(), NOW()),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Carlos López', NULL, 20323334445, 'carlos.lopez@email.com', 'Belgrano 789', '3889876543', NULL, NOW(), NOW()),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Ana Martínez', NULL, 27334445556, 'ana.martinez@email.com', 'San Martín 321', '3884567890', NULL, NOW(), NOW());

-- =============================================================================
-- 2. INSERCIÓN DE CUENTAS FINANCIERAS (UUID para id, vinculadas a cliente_id)
-- =============================================================================
INSERT INTO cuenta_financiera (id, cbu, alias, saldo_operativo, estado, cliente_id, created_date, last_modified_date) VALUES
('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01', 0000003100000000000001, 'JUAN.PEREZ.ARS', 150000.50, 'ACTIVA', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02', 0000003100000000000002, 'JUAN.PEREZ.USD', 85000.00, 'ACTIVA', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03', 0000003100000000000003, 'MARIA.GOMEZ.ARS', 320000.75, 'ACTIVA', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', NOW(), NOW()),
('f4eebc99-9c0b-4ef8-bb6d-6bb9bd380f04', 0000003100000000000004, 'CARLOS.LOPEZ.ARS', 45000.00, 'SUSPENDIDA', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', NOW(), NOW());

-- =============================================================================
-- 3. INSERCIÓN DE TRANSACCIONES (ID long autoincremental, vinculadas a cuenta_financiera_id)
-- =============================================================================
INSERT INTO transaccion (id, fecha_hora, monto, estado_transaccion, cuenta_financiera_id, created_date, last_modified_date) VALUES
(1, NOW(), 50000.00, 'COMPLETADA', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01', NOW(), NOW()),
(2, NOW(), 12000.50, 'COMPLETADA', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01', NOW(), NOW()),
(3, NOW(), 100000.00, 'COMPLETADA', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02', NOW(), NOW()),
(4, NOW(), 150000.00, 'COMPLETADA', 'f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03', NOW(), NOW());