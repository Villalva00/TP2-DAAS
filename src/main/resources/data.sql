SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE cliente;
TRUNCATE TABLE cuenta_financiera;
TRUNCATE TABLE transaccion;
TRUNCATE TABLE caja_ahorro;
TRUNCATE TABLE cuenta_corriente;

SET FOREIGN_KEY_CHECKS = 1;

-- Clientes
INSERT INTO cliente (id, nombre, razon_social, cuil, email, direccion, telefono, cliente_padre_id, tipo_cliente, parentesco, created_date, last_modified_date)
VALUES
    (UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), 'Juan Pérez', NULL, 20301112229, 'juan.perez@email.com', 'Av. Principal 123', '3881234567', NULL, 'TITULAR', NULL, NOW(), NOW()),
    (UUID_TO_BIN('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'), 'María Gómez', NULL, 27312223334, 'maria.gomez@email.com', 'Calle Falsa 456', '3887654321', NULL, 'TITULAR', NULL, NOW(), NOW()),
    (UUID_TO_BIN('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'), 'Carlos López', NULL, 20323334445, 'carlos.lopez@email.com', 'Belgrano 789', '3889876543', NULL, 'TITULAR', NULL, NOW(), NOW()),
    (UUID_TO_BIN('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44'), 'Ana Martínez', NULL, 27334445556, 'ana.martinez@email.com', 'San Martín 321', '3884567890', NULL, 'TITULAR', NULL, NOW(), NOW()),

-- Adherentes de Juan Pérez
    (UUID_TO_BIN('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55'), 'Laura Ruiz', NULL, 27345556667, 'laura.ruiz@email.com', 'Av. Principal 123', '3881112233', UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), 'ADHERENTE', 'CONYUGE', NOW(), NOW()),
    (UUID_TO_BIN('e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a66'), 'Tomás Pérez', NULL, 20456667778, 'tomas.perez@email.com', 'Av. Principal 123', '3884445566', UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), 'ADHERENTE', 'HIJO', NOW(), NOW());
-- Tabla padre de cuentas
INSERT INTO cuenta_financiera (id, cbu, alias, saldo_operativo, estado, cliente_id, created_date, last_modified_date)
VALUES
    (UUID_TO_BIN('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01'), 31000000000001, 'JUAN.PEREZ.ARS', 150000.50, 'ACTIVA', UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), NOW(), NOW()),
    (UUID_TO_BIN('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02'), 31000000000002, 'JUAN.PEREZ.USD', 85000.00, 'ACTIVA', UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), NOW(), NOW()),
    (UUID_TO_BIN('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03'), 31000000000003, 'MARIA.GOMEZ.ARS', 320000.75, 'ACTIVA', UUID_TO_BIN('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'), NOW(), NOW()),
    (UUID_TO_BIN('f4eebc99-9c0b-4ef8-bb6d-6bb9bd380f04'), 31000000000004, 'CARLOS.LOPEZ.ARS', 45000.00, 'SUSPENDIDA', UUID_TO_BIN('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'), NOW(), NOW());

-- Tablas hijas (JOINED): mismo id que la fila padre
INSERT INTO caja_ahorro (id, tasa_interes_anual, limite_extraccion) VALUES
                                                                        (UUID_TO_BIN('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01'), 40.0, 5),
                                                                        (UUID_TO_BIN('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02'), 40.0, 5),
                                                                        (UUID_TO_BIN('f4eebc99-9c0b-4ef8-bb6d-6bb9bd380f04'), 40.0, 5);

INSERT INTO cuenta_corriente (id, margen_descubierto_autorizado, costo_comision_mantenimiento) VALUES
    (UUID_TO_BIN('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03'), 100000.0, 5000.0);

-- Transacciones (ahora con tipo)
INSERT INTO transaccion (id, fecha_hora, monto, estado_transaccion, tipo, cuenta_financiera_id, created_date, last_modified_date)
VALUES
    (1, NOW(), 50000.00, 'COMPLETADA', 'DEPOSITO', UUID_TO_BIN('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01'), NOW(), NOW()),
    (2, NOW(), 12000.50, 'COMPLETADA', 'EXTRACCION', UUID_TO_BIN('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380f01'), NOW(), NOW()),
    (3, NOW(), 100000.00, 'COMPLETADA', 'DEPOSITO', UUID_TO_BIN('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380f02'), NOW(), NOW()),
    (4, NOW(), 150000.00, 'COMPLETADA', 'DEPOSITO', UUID_TO_BIN('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380f03'), NOW(), NOW());