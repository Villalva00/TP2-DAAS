# TP5-C · Issue #11 — Servicio de liquidación mensual de comisiones y tarea programada

> **Rama:** `feature/issue-11-liquidacion-comisiones`
> **Asignado:** @gonza · **Estado:** implementado y verificado
> **Fecha de documentación:** 09/10/2026

---

## 1. Contexto

Con el enum `DEBITO_COMISION` y las propiedades globales (`ComisionesProperties`)
listos (issue #10), esta issue implementa la **liquidación mensual**: debitar
masivamente la comisión de mantenimiento a todas las cuentas **activas** el primer
día de cada mes, registrando por cada cuenta una `Transaccion` de tipo
`DEBITO_COMISION` con estado `COMPLETADA`.

**Criterios de aceptación** de la issue:
- Se debita únicamente a cuentas `ACTIVA` (`SUSPENDIDA`/`BLOQUEADA` quedan fuera).
- Montos: `CuentaCorriente → 5000` y `CajaAhorro → 2000` (parametrizables).
- Un fallo en una cuenta **no detiene** la liquidación de las demás.
- Cada débito queda verificado vía el endpoint existente
  `GET /api/v1/transacciones/tipo/DEBITO_COMISION`.

---

## 2. Diseño

```
        ┌─────────────────────────┐
        │ LiquidacionComisionesScheduler  ── @Scheduled(cron)
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  ComisionService        │  orquesta: findByEstado(ACTIVA)
        │  (ComisionServiceImpl)  │  try/catch por cuenta
        └────────────┬────────────┘
                     │  por cada cuenta (REQUIRES_NEW)
        ┌────────────▼────────────┐
        │  ComisionProcessor      │  debita + registra Transaccion
        └────────────────────────┘
```

- **`ComisionService.liquidarComisionesMensuales()`**: recorre las cuentas activas
  y delega cada débito en el `ComisionProcessor`; el fallo de una cuenta se loguea
  y se incrementa `fallidas` sin abortar el resto. Devuelve `ResultadoLiquidacion`.
- **`ResultadoLiquidacion`** (record): `procesadas`, `fallidas`, `totalDebitado`.
- **`ComisionProcessor.procesarCuenta(UUID)`**: corre en **su propia transacción**
  (`Propagation.REQUIRES_NEW`) para aislar cada cuenta.
- **`LiquidacionComisionesScheduler`**: `@Scheduled` con el cron configurable
  `app.comisiones.cron` (por defecto `0 0 0 1 * *`, cada primer día del mes a las 00:00).
- **`ComisionController`** (opcional, incluido): `POST /api/v1/admin/comisiones/liquidar`
  dispara la liquidación manualmente (útil para pruebas/demo).
- **Idempotencia mensual** (opcional, incluido): antes de debitar se verifica si la
  cuenta ya tiene un `DEBITO_COMISION` en el mes en curso; de ser así se omite.

---

## 3. Cambios en el código

### 3.1 `Tp2Application.java`

Se agregó `@EnableScheduling` para registrar las tareas `@Scheduled`.

### 3.2 `service/ResultadoLiquidacion.java` (nuevo)

```java
public record ResultadoLiquidacion(int procesadas, int fallidas, BigDecimal totalDebitado) {
    public boolean huboActividad() { return procesadas > 0 || fallidas > 0; }
}
```

### 3.3 `service/ComisionService.java` e `impl/ComisionServiceImpl.java` (nuevos)

```java
@Override
public ResultadoLiquidacion liquidarComisionesMensuales() {
    List<CuentaFinanciera> cuentasActivas = cuentaFinancieraRepository.findByEstado(EstadoCuenta.ACTIVA);
    ...
    for (CuentaFinanciera cuenta : cuentasActivas) {
        try {
            Double monto = comisionProcessor.procesarCuenta(cuenta.getId());
            if (monto > 0) { procesadas++; totalDebitado = totalDebitado.add(BigDecimal.valueOf(monto)); }
        } catch (Exception ex) {
            fallidas++;
            log.error("Falló la liquidación de la cuenta {}... Continúa.", cuenta.getId(), ex);
        }
    }
    ...
}
```

### 3.4 `service/impl/ComisionProcessor.java` (nuevo)

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public Double procesarCuenta(UUID cuentaId) {
    CuentaFinanciera cuenta = ...findById(cuentaId).orElseThrow(...);
    if (existeDebitoComisionEnElMes(cuentaId)) return 0.0;   // idempotencia

    Double monto = comisionPara(cuenta);                     // CC→5000, CA→2000
    cuenta.actualizarSaldo(-monto);                          // NO extraer(): puede quedar negativo
    cuentaFinancieraRepository.save(cuenta);

    Transaccion tx = Transaccion.builder()
        .fechaHora(LocalDateTime.now())
        .monto(monto)
        .tipo(TipoTransaccion.DEBITO_COMISION)
        .estadoTransaccion(EstadoTransaccion.COMPLETADA)
        .cuentaFinanciera(cuenta)
        .ejecutor(null)                                      // movimientos de sistema
        .build();
    transaccionRepository.save(tx);
    return monto;
}
```

Puntos clave de diseño:
- **Tipo concreto** de la cuenta: como la herencia es `JOINED`, se usa
  `Hibernate.unproxy(cuenta)` + `instanceof CuentaCorriente/CajaAhorro`.
- **Descuento**: `actualizarSaldo(-monto)` (no `extraer()`, que valida fondos).
  Por diseño el saldo puede quedar **negativo** tras la liquidación.
- **`ejecutor = null`**: son movimientos de sistema, no hay cliente ejecutor.
- Mombre del monto guardado: **positivo** (magnitud del débito).

### 3.5 `scheduler/LiquidacionComisionesScheduler.java` (nuevo)

```java
@Component
public class LiquidacionComisionesScheduler {
    @Scheduled(cron = "${app.comisiones.cron:0 0 0 1 * *}")
    public void ejecutarLiquidacionMensual() {
        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();
        ...
    }
}
```

### 3.6 `controller/ComisionController.java` (nuevo)

```java
@RestController
@RequestMapping("/api/v1/admin/comisiones")
public class ComisionController {
    @PostMapping("/liquidar")
    public ResponseEntity<ResultadoLiquidacion> liquidarComisionesManual() { ... }
}
```

### 3.7 `repository/TransaccionRepository.java`

Nueva consulta de idempotencia mensual:

```java
@Query("SELECT COUNT(t) > 0 FROM Transaccion t " +
       "WHERE t.cuentaFinanciera.id = :cuentaId AND t.tipo = :tipo " +
       "AND t.fechaHora >= :desde AND t.fechaHora < :hasta")
boolean existsDebitoComisionEnPeriodo(UUID cuentaId, TipoTransaccion tipo, LocalDateTime desde, LocalDateTime hasta);
```

---

## 4. Tests (+7)

| Test | Verifica |
| :--- | :--- |
| `ComisionProcessorTest.procesarCuenta_deberiaDebitar5000EnUnaCuentaCorriente` | CC → débito 5000, saldo `-5000`, tx `DEBITO_COMISION`/`COMPLETADA`, `ejecutor = null`. |
| `ComisionProcessorTest.procesarCuenta_deberiaDebitar2000EnUnaCajaDeAhorro` | CA → débito 2000, saldo `-2000`. |
| `ComisionProcessorTest.procesarCuenta_deberiaOmitir_cuandoYaFueDebitadaEnElMes` | idempotencia: no vuelve a debitar ni guarda tx. |
| `ComisionServiceImplTest.liquidarComisionesMensuales_deberiaDebitarSoloCuentasActivas` | solo se procesan `ACTIVA`; acumula total correctamente. |
| `ComisionServiceImplTest.liquidarComisionesMensuales_noDebeFrenarseCuandoUnaCuentaFalla` | una cuenta que falla → `fallidas=1`, el resto se procesa. |
| `ComisionServiceImplTest.liquidarComisionesMensuales_sinCuentasActivas_deberiaDevolverCero` | sin activas → `0/0/0`. |
| `LiquidacionComisionesSchedulerTest.ejecutarLiquidacionMensual_deberiaInvocarElServicioDeComisiones` | la tarea delegua en el servicio. |

---

## 5. Verificación

### 5.1 Unitario

```
./mvnw test
→ Tests run: 60, Failures: 0, Errors: 0, Skipped: 0   (53 previos + 7 nuevos)
```

> Nota: `Tp2ApplicationTests` (contexto completo) carga `application.properties` cuyo
> cron de producción (`0 0 0 1 * *`) **no dispara** durante los tests; por eso la
> suite no genera débitos reales.

### 5.2 E2E (manual)

Para validar el disparo programado sin esperar al día 1, se corre la app con el cron
por minuto (variable de entorno; Spring relaxed binding):

```bash
APP_COMISIONES_CRON="0 * * * * *" bash ./mvnw spring-boot:run
```

Pasos de validación (MySQL local con 4 cuentas seed: 3 `ACTIVA` + 1 `SUSPENDIDA`):

1. `POST /api/v1/admin/comisiones/liquidar` →
   `{"procesadas":3,"fallidas":0,"totalDebitado":…}` (solo las 3 activas).
2. `GET /api/v1/transacciones/tipo/DEBITO_COMISION` → 3 transacciones
   (`COMPLETADA`, monto 5000/2000 según tipo, `ejecutor: null`).
3. Saldos en `cuenta_financiera` reducidos en el monto de su comisión
   (pueden quedar **negativos**).
4. Se espera el disparo del cron (≤ 60 s) y se repite el `GET` → sigue en 3
   transacciones: **idempotencia** confirma que no se dobla el débito.
5. `POST .../liquidar` de nuevo → `procesadas:0` (todas ya liquidadas en el mes).

---

## 6. Fuera de alcance

- Comisiones proporcionales/fraccionadas: los montos son **fijos globales**.
- Notificaciones por email del débito de comisión.
- Regla de "protección de saldo" / límite de descubierto al dejar saldo negativo.