# TP5-B · Issue #9 — Endpoint de confirmación `GET /api/v1/clientes/activar?token=`

> **Rama:** `feature/issue-9-endpoint-activacion`
> **Asignado:** @gonzalo · **Depende de:** #7 · **Estado:** implementado y verificado
> **Fecha de documentación:** 09/10/2026

---

## 1. Contexto

El cliente recibe por email (issue #8) un token de activación con el enlace
`{baseUrl}/api/v1/clientes/activar?token={token}`. Este endpoint completa el
flujo: valida que el token exista y esté vigente y, si es válido, pone al cliente
en estado `ACTIVO` y marca el token como `usado`.

---

## 2. Cambios de funcionamiento (comportamiento observable)

### 2.1 `GET /api/v1/clientes/activar?token={token}`

| Caso | Respuesta |
| :--- | :--- |
| Token **válido** (existe, no usado, no vencido) | **200** con `ClienteResponseDto` en `estado = "ACTIVO"` |
| Token **inexistente** | **400** — `"El token de activación no existe o es inválido."` |
| Token **reutilizado** (`usado=true`) | **400** — `"El token de activación ya fue utilizado."` |
| Token **expirado** (`fecha_expiracion` pasada) | **400** — `"El token de activación expiró."` |
| **Formato inválido** (no UUID) o token vacío | **400** — `"El token de activación tiene un formato inválido."` |

Todos los errores devuelven el mismo JSON homogéneo del
`GlobalExceptionHandler`:
`{ timestamp, status, error, message }`.

### 2.2 Resolución de rutas (sin conflicto con `/{id}`)

`GET /activar` es un **path literal** y Spring MVC le da prioridad sobre la
plantilla `GET /{id}` (que además convertiría `activar` a `UUID` y fallaría).
No hay choque entre ambos mapeos.

### 2.3 Efecto en los datos

En la misma transacción:
- `cliente.estado` → `ACTIVO` (queda habilitado para operar, saldo intacto).
- `token_activacion.usado` → `true` (un cliente no puede reactivarse dos veces).

---

## 3. Cambios en el código

### 3.1 Archivos nuevos

| Archivo | Descripción |
| :--- | :--- |
| `src/main/java/.../exception/TokenInvalidoException.java` | `RuntimeException` con constructor `(String mensaje)`. Indica token rechazado (formato, inexistente, usado o vencido). |

### 3.2 Archivos modificados

#### `exception/GlobalExceptionHandler.java`

- Nuevo handler:

```java
@ExceptionHandler(TokenInvalidoException.class)
public ResponseEntity<Map<String, Object>> handleTokenInvalido(TokenInvalidoException ex) {
    // status=400, error="Token de activación inválido", message=ex.getMessage()
}
```

#### `service/ClienteService.java`

- Nuevo contrato `ClienteResponseDto activarCliente(String token)` con Javadoc.

#### `service/impl/ClienteServiceImpl.java`

- `activarCliente(String token)` (`@Transactional`) con el flujo pedido:

```java
UUID tokenUuid = parsearToken(token);                       // UUID.fromString → TokenInvalidoException si falla
TokenActivacion ta = tokenActivacionRepository.findByToken(tokenUuid)
        .orElseThrow(() -> new TokenInvalidoException("...no existe..."));
validarToken(ta);                                           // usado=true → excepción | vencido → excepción
cliente.setEstado(EstadoCliente.ACTIVO);
ta.setUsado(true);
clienteRepository.save(cliente);
tokenActivacionRepository.save(ta);
```

- Privados: `parsearToken(String)` (captura `IllegalArgumentException` y
  `NullPointerException`) y `validarToken(TokenActivacion)` (chequea `usado` y
  `fechaExpiracion`).

#### `controller/ClienteController.java`

- Nuevo endpoint (ubicado antes de `/{id}`, aunque el literal siempre gana por
  especificidad):

```java
@GetMapping("/activar")
public ResponseEntity<ClienteResponseDto> activarCliente(@RequestParam("token") String token) {
    return ResponseEntity.ok(clienteService.activarCliente(token));
}
```

---

## 4. Tests

### `service/ClienteServiceImplTest.java` (+5 tests)

| Test | Verifica |
| :--- | :--- |
| `activarCliente_...TokenEsValido` | cliente pasa a `ACTIVO`, token `usado=true`, `save` de ambos y DTO con `estado=ACTIVO`. |
| `activarCliente_...TokenNoExiste` | `TokenInvalidoException` "no existe" (por `Optional.empty()`). |
| `activarCliente_...TokenYaFueUsado` | `TokenInvalidoException` "ya fue utilizado". |
| `activarCliente_...TokenExpiro` | `TokenInvalidoException` "expiró" (fecha en el pasado). |
| `activarCliente_...FormatoInvalido` | `TokenInvalidoException` "formato inválido" **sin consultar** el repositorio (`verifyNoInteractions`). |

---

## 5. Criterios de aceptación y verificación

### 5.1 Tests unitarios — `./mvnw test`

```
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0  →  BUILD SUCCESS
```

- `ClienteServiceImplTest`: 17 ✅ · `TransaccionServiceImplTest`: 17 ✅
- `CuentaFinancieraServiceImplTest`: 9 ✅ · `LimitesExtraccionPropertiesTest`: 4 ✅
- `ClienteRegistradoListenerTest`: 2 ✅ · `Tp2ApplicationTests`: 1 ✅

### 5.2 Prueba E2E (app levantada + MySQL)

| # | Verificación | Resultado |
| :---: | :--- | :--- |
| 1 | Alta → `PENDIENTE_ACTIVACION`, token real extraído de `token_activacion` | ✅ |
| 2 | `GET /activar?token=<válido>` → **200** `estado=ACTIVO` | ✅ |
| 3 | Mismo token de nuevo → **400** "ya fue utilizado" | ✅ |
| 4 | Token aleatorio inexistente → **400** "no existe o es inválido" | ✅ |
| 5 | `token=no-soy-un-uuid` → **400** "formato inválido"; `token=` vacío → **400** | ✅ |
| 6 | `UPDATE fecha_expiracion` a 1 h en el pasado → **400** "expiró" | ✅ |
| 7 | Estado en BD: cliente activado con `estado=ACTIVO` y `usado=1`; el expirado sigue `PENDIENTE_ACTIVACION` con `usado=0` | ✅ |

Todos los criterios de aceptación cumplidos:
- ✅ Token válido → cliente `ACTIVO` (200).
- ✅ Token inexistente / expirado / reutilizado / formato inválido → **400** con JSON de error.

---

## 6. Fuera de alcance

- **Regeneración de tokens / reenvío del email:** no existe en esta issue.
- **Respuesta** web para humanos (la petición viene de un navegador pero se
  devuelve JSON, no una página HTML): se puede agregar en una issue futura de UX.