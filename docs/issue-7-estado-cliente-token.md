# TP5-B · Issue #7 — Estado de cliente y entidad TokenActivacion

> **Rama:** `feature/issue-7-estado-cliente-token`
> **Asignado:** @gonzalo · **Estado:** implementado y verificado
> **Fecha de documentación:** 07/10/2026

---

## 1. Contexto

Todo cliente nuevo se registra en estado `PENDIENTE_ACTIVACION` y recibe un
**Token de Activación** (UUID) válido por **24 horas**. El token es el mecanismo
que en el futuro (issue #8) permitirá pasar el cliente a `ACTIVO`; viaja
**únicamente por email** y nunca se expone en la API.

---

## 2. Cambios de funcionamiento (comportamiento observable)

### 2.1 Alta de clientes (titulares)

- `POST /api/v1/clientes` ahora da de alta al cliente en
  **`PENDIENTE_ACTIVACION`** (antes no existía el concepto de estado).
- La respuesta (`ClienteResponseDto`) **incluye el nuevo campo `estado`**
  (`"PENDIENTE_ACTIVACION"`) y **no expone el token** bajo ninguna forma.
- En la misma transacción se persiste un registro en la tabla
  `token_activacion` con:
  - `token` = `UUID.randomUUID()` (único),
  - `fecha_expiracion` = `now() + 24 horas`,
  - `usado` = `false`,
  - relación **@OneToOne** con el cliente (FK única: un cliente = un token activo).
- La validación de duplicados (CUIL/email → 400) no cambia.

### 2.2 Alta de adherentes (TP5-A #3)

- `POST /api/v1/clientes/{titularId}/adherentes` sigue las mismas reglas que el
  alta de titulares: el adherente nace en **`PENDIENTE_ACTIVACION`** y recibe su
  propio token de 24 h.
- El titular que da de alta adherentes debe existir y ser `TITULAR` (reglas previas sin cambios).

### 2.3 Operaciones con dinero (recomendado de la issue)

- Un cliente **no `ACTIVO` no puede operar**. La validación está centralizada en
  `TransaccionServiceImpl.resolverEjecutor(...)`, por lo que cubre **depósitos y
  extracciones**, tanto si el ejecutor viene explícito (`clienteEjecutorId`) como
  si se asume al titular de la cuenta.
- Ante un cliente no activo se lanza `OperacionNoPermitidaException` →
  **HTTP 403** con mensaje:
  `"El cliente no está activo y no puede realizar operaciones"`.
- El saldo y la cuenta **no se modifican** cuando la operación se rechaza por estado.
- Orden de validaciones en `resolverEjecutor`: primero autorización
  (titular/adherente de la cuenta) y después estado `ACTIVO`.

### 2.4 Eliminación de clientes

- `DELETE /api/v1/clientes/{id}` ahora elimina primero los tokens de activación
  del cliente (`tokenActivacionRepository.deleteByClienteId(id)`) para respetar la
  clave foránea, y luego el cliente (204 sin cambios en el código de estado).

### 2.5 Datos de prueba (`data.sql`)

- La tabla `token_activacion` se agrega al `TRUNCATE` inicial.
- La columna `estado` se agrega al `INSERT` de clientes y **todos los clientes de
  ejemplo (4 titulares + 2 adherentes) se cargan como `ACTIVO`**, de modo que las
  pruebas manuales y la E2E previa (tope diario, adherentes, etc.) siguen
  funcionando: los datos de prueba no pasan por el flujo de activación.

### 2.6 Consultas de clientes

- `GET /api/v1/clientes` y `GET /api/v1/clientes/{id}` devuelven el campo `estado`.
- El endpoint `GET /api/v1/clientes/{id}` permite consultar el estado de un
  cliente en particular (por ejemplo, para verificar que quedó `PENDIENTE_ACTIVACION`).

### 2.7 Modelo de datos (Hibernate `ddl-auto=update`)

Se crea/altera la tabla:

```sql
CREATE TABLE token_activacion (
    id                 BINARY(16)  NOT NULL,          -- UUID (PK)
    token              BINARY(16)  NOT NULL UNIQUE,   -- UUID único
    fecha_expiracion   DATETIME(6) NOT NULL,
    usado              BIT(1)      NOT NULL,
    cliente_id         BINARY(16)  NOT NULL UNIQUE,   -- FK 1-1 hacia cliente
    created_date       DATETIME(6) NOT NULL,          -- EntidadAuditable
    last_modified_date DATETIME(6) NOT NULL,          -- EntidadAuditable
    PRIMARY KEY (id),
    CONSTRAINT FK_token_activacion_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);
```

Y en la tabla `cliente`:

```sql
ALTER TABLE cliente ADD estado VARCHAR(30) NOT NULL;  -- valores: PENDIENTE_ACTIVACION | ACTIVO
```

---

## 3. Cambios en el código

### 3.1 Archivos nuevos

| Archivo | Descripción |
| :--- | :--- |
| `src/main/java/.../model/EstadoCliente.java` | Enum `EstadoCliente { PENDIENTE_ACTIVACION, ACTIVO }` con Javadoc de cada valor. Convención: paquete `model`, mayúsculas, igual que `TipoCliente`, `EstadoCuenta`, etc. |
| `src/main/java/.../model/TokenActivacion.java` | Entidad `@Entity @Table(name="token_activacion")` que **extiende `EntidadAuditable`**. Campos: `UUID id` (`@Id @GeneratedValue`), `UUID token` (`nullable=false, unique=true`), `LocalDateTime fechaExpiracion` (`nullable=false`), `boolean usado` (`@Builder.Default false`, `nullable=false`), `@OneToOne(optional=false, fetch=LAZY) @JoinColumn(name="cliente_id", nullable=false, unique=true) Cliente cliente`. Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`. |
| `src/main/java/.../repository/TokenActivacionRepository.java` | Interfaz `@Repository extends JpaRepository<TokenActivacion, UUID>` con: `Optional<TokenActivacion> findByToken(UUID token)` (listo para el canje del token) y `void deleteByClienteId(UUID clienteId)` (uso en `eliminarPorId`). |

### 3.2 Archivos modificados

#### `model/Cliente.java`

- Nuevo campo (líneas ~102-109):

```java
@Enumerated(EnumType.STRING)
@Column(name = "estado", nullable = false, length = 30)
@Builder.Default
private EstadoCliente estado = EstadoCliente.PENDIENTE_ACTIVACION;
```

- `@Builder.Default` garantiza que **cualquier** `Cliente.builder()...build()`
  sin `estado` explícito quede en `PENDIENTE_ACTIVACION`.

#### `dto/ClienteResponseDto.java`

- Nuevo campo `private String estado;` (documentado: *el token de activación
  nunca se expone en este DTO*). No se agregó ningún campo relacionado con el token.

#### `service/impl/ClienteServiceImpl.java`

- Nueva dependencia: `private final TokenActivacionRepository tokenActivacionRepository`
  (inyección por `@RequiredArgsConstructor`).
- `crearCliente(...)`: construye el cliente con `.estado(EstadoCliente.PENDIENTE_ACTIVACION)`
  y luego de persistirlo llama a `generarTokenActivacion(clienteGuardado)`.
- `crearAdherente(...)`: ídem para el adherente (`.estado(PENDIENTE_ACTIVACION)` +
  `generarTokenActivacion(guardado)`). Se reemplazó el TODO de la issue #7 por la
  implementación; se conserva el TODO #8 (evento de dominio para el email).
- Nuevo método privado `generarTokenActivacion(Cliente)`:

```java
private void generarTokenActivacion(Cliente cliente) {
    TokenActivacion token = TokenActivacion.builder()
            .token(UUID.randomUUID())
            .fechaExpiracion(LocalDateTime.now().plusHours(24))
            .usado(false)
            .cliente(cliente)
            .build();
    tokenActivacionRepository.save(token);
    ...
}
```

- `eliminarPorId(...)`: ahora ejecuta `tokenActivacionRepository.deleteByClienteId(id)`
  antes de `clienteRepository.delete(cliente)`.
- `mapearAResponseDto(...)`: agrega `.estado(cliente.getEstado() != null
  ? cliente.getEstado().name() : null)`.

#### `service/impl/TransaccionServiceImpl.java`

- `resolverEjecutor(CuentaFinanciera, UUID)` refactorizada (líneas ~257-293):
  - Antes: si `ejecutorId == null` retornaba el titular **sin ninguna validación**;
    ahora el titular también pasa por el chequeo de estado.
  - Al final del método (común a ambos caminos):

```java
if (ejecutor.getEstado() != EstadoCliente.ACTIVO) {
    log.warn("Operación denegada: el cliente {} se encuentra en estado {}",
             ejecutor.getId(), ejecutor.getEstado());
    throw new OperacionNoPermitidaException("El cliente no está activo y no puede realizar operaciones");
}
```

- Como `registrarDeposito` y `registrarExtraccion` llaman a `resolverEjecutor`,
  **ambas operaciones** quedan cubiertas por la validación.

#### `resources/data.sql`

- `TRUNCATE TABLE token_activacion;` (con `FOREIGN_KEY_CHECKS = 0`).
- Columna `estado` agregada a los `INSERT` de `cliente` (6 filas, todas `'ACTIVO'`).

---

## 4. Cambios en los tests

### `service/ClienteServiceImplTest.java`

- Nuevo mock: `@Mock TokenActivacionRepository tokenActivacionRepository`
  (sin él, `crearCliente`, `crearAdherente` y `eliminarPorId` fallaban con NPE).
- Tests nuevos:
  - `crearCliente_deberiaRegistrarPendienteYGenerarTokenDe24Horas` — con
    `ArgumentCaptor` verifica: estado `PENDIENTE_ACTIVACION` en el cliente
    persistido, token no nulo, `usado=false`, token asociado al cliente y
    `fechaExpiracion` dentro de la ventana `[now+24h, now+24h+1s]`.
  - `crearCliente_noDeberiaExponerElTokenEnLaRespuesta` — verifica que la clase
    `ClienteResponseDto` no tenga campos `token`/`tokenActivacion` y que el
    `estado` expuesto sea `PENDIENTE_ACTIVACION`.
- Tests ajustados:
  - `crearCliente_deberiaGuardarCliente_cuandoNoExisteDuplicado` → verifica
    también `tokenActivacionRepository.save(...)`.
  - `eliminarPorId_deberiaBorrarCliente_cuandoExiste` → verifica
    `deleteByClienteId(id)`.
  - `crearAdherente_deberiaVincularAlTitular` → mock del repositorio de tokens,
    aserción de `estado = "PENDIENTE_ACTIVACION"` y verificación del token.

### `service/TransaccionServiceImplTest.java`

- Fixtures de `setUp()` (titular y adherente) y del test de adherente ajeno
  ahora construyen los clientes con `.estado(EstadoCliente.ACTIVO)` (los datos
  operativos deben estar activos para no chocar con la nueva validación).
- Tests nuevos:
  - `registrarDeposito_deberiaRechazar_cuandoElClienteNoEstaActivo` — titular en
    `PENDIENTE_ACTIVACION` → `OperacionNoPermitidaException` ("no está activo"),
    saldo intacto y ningún `save`.
  - `registrarExtraccion_deberiaRechazar_cuandoElAdherenteNoEstaActivo` —
    adherente pendiente → 403, sin guardar transacción.

---

## 5. Criterios de aceptación y verificación

### 5.1 Tests unitarios — `./mvnw test`

```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

- `ClienteServiceImplTest`: 11 tests ✅
- `TransaccionServiceImplTest`: 17 tests ✅
- `CuentaFinancieraServiceImplTest`: 9 tests ✅
- `LimitesExtraccionPropertiesTest`: 4 tests ✅
- `Tp2ApplicationTests` (contexto completo con MySQL): 1 test ✅

### 5.2 Prueba E2E sobre la app levantada (script `/tmp/opencode/test-issue7.sh`)

**24/24 checks PASS**, cubriendo:

| # | Verificación | Resultado |
| :---: | :--- | :--- |
| 1 | `data.sql`: 6 clientes de ejemplo en `ACTIVO`, ninguno pendiente | ✅ 200 |
| 2 | `POST /api/v1/clientes` → 201, `estado=PENDIENTE_ACTIVACION`, sin token en la respuesta | ✅ |
| 3 | Token en DB: UUID válido, `usado=0`, expiración = 1440 minutos (24 h), 1:1 con el cliente | ✅ |
| 4 | `GET /api/v1/clientes/{id}` refleja `PENDIENTE_ACTIVACION` | ✅ |
| 5 | Depósito de cliente no ACTIVO → **403** "El cliente no está activo…", saldo intacto | ✅ |
| 6 | Extracción de cliente no ACTIVO → **403** | ✅ |
| 7 | Depósito/extracción de clientes `ACTIVO` (data.sql) → 201; adherente en depósito → 403 (regla #5) | ✅ |
| 8 | `POST …/adherentes` → 201, `estado=PENDIENTE_ACTIVACION` + token en DB | ✅ |
| 9 | CUIL/email duplicado → 400 | ✅ |
| 10 | `DELETE /api/v1/clientes/{id}` → 204 y borra sus tokens sin tocar los de otros | ✅ |

---

## 6. Fuera de alcance de esta issue (trabajo pendiente)

- **Issue #8 — Envío del token por email:** aún no hay `EmailService` ni
  `spring-boot-starter-mail`. Queda el TODO en
  `ClienteServiceImpl.crearAdherente`:
  `// TODO (TP5-B #8): publicar el evento de dominio asíncrono para enviar el email de activación.`
  Hasta entonces el token solo se persiste en `token_activacion`.
- **Canje/activación del token:** `TokenActivacionRepository.findByToken(UUID)`
  está implementado y listo, pero **no existe aún** el endpoint/servicio que
  valide expiración + `usado` y pase el cliente a `ACTIVO` (transición que
  completa el flujo).
