# TP5 · Issue #14 — Documentación, datos de ejemplo y colección Postman

> **Rama:** `feature/issue-14-documentacion`
> **Asignado:** @elias · **Estado:** implementado y verificado
> **Fecha de documentación:** 09/10/2026

---

## 1. Contexto

Cierre de la entrega TP5: un tercero debe poder **levantar el proyecto desde cero** y
probar todos los flujos siguiendo la documentación, contando con una colección
Postman/Insomnia lista para importar y datos de ejemplo coherentes con el modelo final.

---

## 2. Cambios en el repositorio

### 2.1 `README` (actualizado)

Se reescribió y expandió con una sección **TP5** completa:

*   **Reglas de adherentes y topes diarios** (`TITULAR`/`ADHERENTE`, `parentesco`
    `CONYUGE`/`HIJO`, adherente solo extrae, deposit–por-adherente → 403, topes
    `100 000`/`70 000` por ejecutor y acumulado diario).
*   **Flujo de alta + activación** con diagrama de secuencia ASCII y comportamiento
    de los tokens (UUID, vencimiento 24 h, un solo uso, 400 en inválido/vencido/usado).
*   **Liquidación mensual de comisiones** (`@Scheduled`, montos CC 5000 / CA 2000,
    solo cuentas `ACTIVA`, idempotencia mensual, `REQUIRES_NEW`) y **cómo cambiar el
    cron para pruebas** (`0 * * * * *` vs. `0 0 0 1 * *`, formato 6 campos Spring)
    más el disparo manual `POST /api/v1/admin/comisiones/liquidar`.
*   **Tabla de propiedades `app.*` y variables de entorno**
    (`DB_*`, `MAIL_*`, `APP_*`) con sus valores por defecto.
*   Resumen de endpoints, guía de arranque desde cero (`.env` + `docker compose up -d`
    + `spring-boot:run`), sección de Postman y verificación (`clean test` → 60 tests).

### 2.2 `postman/TP2-DAAS.postman_collection.json` (nuevo)

Colección **Postman v2.1** (importable en Insomnia) con 3 carpetas y 8 requests:
`baseUrl` y tokens como variables de colección.

| # | Carpeta / request | Esperado |
| :--- | :--- | :--- |
| 1 | **TP5 · Alta y activación**: alta titular · activar con token (Mailpit) · alta adherente | 201 / 200 `ACTIVO` / 201 |
| 2 | **Errores de regla**: token expirado (`GET /activar`) | 400 |
| 3 | **Errores de regla**: adherente que deposita (`POST /deposito` con `clienteEjecutorId` adherente) | 403 |
| 4 | **Errores de regla**: exceder tope diario (`POST /extraccion` de 100 000 por adherente) | 400 `tope=70000` |
| 5 | **Liquidación**: disparo manual · verificación `GET /transacciones/tipo/DEBITO_COMISION` (idempotencia) | 200 `procesadas=3`/`9000` · 200 |

### 2.3 `src/main/resources/data.sql` (revisado — sin cambios)

Se auditó contra el modelo final y es **coherente**:

*   Columnas de `cliente`: `tipo_cliente` (`TITULAR`/`ADHERENTE`), `parentesco`
    (`CONYUGE`/`HIJO`), `estado` (`ACTIVO`), `cliente_padre_id` (FK reflexiva).
*   Columnas de `cuenta_financiera` y tablas hijas `caja_ahorro`
    (`tasa_interes_anual`, `limite_extraccion`) y `cuenta_corriente`
    (`margen_descubierto_autorizado`, `costo_comision_mantenimiento`): coinciden
    con las entidades `@Column`.
*   `transaccion` con `tipo` (`DEPOSITO`, `EXTRACCION`) y `estado_transaccion`
    (`COMPLETADA`); la columna `ejecutor_id` es nullable y queda en `NULL` (seed).
*   Se truncan las 5 tablas (FK checks off) y los UUID se insertan con `UUID_TO_BIN()`.

---

## 3. Verificación

### 3.1 Suite de tests

```
bash ./mvnw clean test
→ Tests run: 60, Failures: 0, Errors: 0, Skipped: 0   → BUILD SUCCESS
```

### 3.2 Arranque "desde cero" y E2E manual (los 5 escenarios de la colección)

Con `docker compose up -d` (MySQL + Mailpit) y `spring-boot:run`:

| Escenario | Resultado |
| :--- | :--- |
| Seed restaurado (6 clientes, 4 cuentas, sin `DEBITO_COMISION`) | ✅ |
| 1 · Alta titular → `PENDIENTE_ACTIVACION`; token en Mailpit; `GET /activar?token=` | ✅ 201 → 200 `ACTIVO` |
| 2 · Token vencido (nuevo cliente + `UPDATE fecha_expiracion` a pasado) | ✅ 400 "El token de activación expiró." |
| 3 · Adherente deposita (`clienteEjecutorId` Laura) | ✅ 403 "Los adherentes solo pueden realizar extracciones" |
| 4 · Adherente extrae 100 000 (> tope 70 000) | ✅ 400 `tope=70000.0, acumulado=0.0` |
| 5 · `POST /admin/comisiones/liquidar` + `GET /transacciones/tipo/DEBITO_COMISION` | ✅ 200 `{procesadas:3,fallidas:0,totalDebitado:9000.0}` · 3 débitos COMPLETADA |

### 3.3 Release

Tag local **`tp5-entrega`** apuntando al commit de esta issue (el push de tags queda
sujeto al flujo de merges/PR del repositorio, igual que las ramas).

---

## 4. Criterios de aceptación

*   ✅ Un tercero puede levantar el proyecto y probar los flujos siguiendo el README.
*   ✅ La colección Postman cubre todos los escenarios listados en la issue.