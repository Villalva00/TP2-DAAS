# TP5-C · Issue #10 — Tipo `DEBITO_COMISION` y configuración global de comisiones

> **Rama:** `feature/issue-10-config-comisiones`
> **Asignado:** @elias · **Estado:** implementado y verificado
> **Fecha de documentación:** 09/10/2026

---

## 1. Contexto

La liquidación mensual de comisiones usará **montos fijos globales** parametrizados
en `application.properties` y registrará cada débito como una `Transaccion` de tipo
`DEBITO_COMISION`. Esta issue prepara el terreno: el enum, las propiedades y su
binding tipado. La tarea programada de liquidación en sí es una issue posterior.

---

## 2. Cambios en el código

### 2.1 `model/TipoTransaccion.java`

Nuevo valor `DEBITO_COMISION` al final del enum, con Javadoc que referencia a
`ComisionesProperties`:

```java
DEBITO_COMISION  // débito originado por la comisión de mantenimiento mensual
```

Se agrega **al final** para no alterar los valores ya persistidos de los demás tipos
(`DEPOSITO`, `EXTRACCION`, `TRANSFERENCIA_ENVIADA`, `TRANSFERENCIA_RECIBIDA`).

### 2.2 `resources/application.properties`

```properties
app.comisiones.cuenta-corriente=5000.00
app.comisiones.caja-ahorro=2000.00

# Cron (6 campos Spring) de la liquidación: primer día de cada mes a las 00:00.
# Para pruebas locales: "0 * * * * *" (cada minuto).
app.comisiones.cron=0 0 0 1 * *
```

> Nota: `app.comisiones.cron` usa la sintaxis **6 campos de Spring**
> (`seg min hora día mes día-semana`). El valor de producción indica "primer día del
> mes a medianoche" y el comentario deja el valor de prueba (cada minuto).

### 2.3 `config/ComisionesProperties.java` (nuevo)

```java
@Validated
@ConfigurationProperties(prefix = "app.comisiones")
public record ComisionesProperties(
        @NotNull @Positive Double cuentaCorriente,
        @NotNull @Positive Double cajaAhorro) {
}
```

Sigue el mismo patrón que `LimitesExtraccionProperties` (TP5-A): record inmutable,
validado y detectado por `@ConfigurationPropertiesScan` de `Tp2Application`.

### 2.4 `model/CuentaCorriente.java`

Se documentó en el Javadoc de `costoComisionMantenimiento` que la liquidación
mensual usa los **montos globales** de `ComisionesProperties` y no ese campo
(solo informativo por cuenta).

---

## 3. Tests (+3)

| Test | Verifica |
| :--- | :--- |
| `ComisionesPropertiesTest.deberiaCargarLosValoresDesdeLasPropiedades` | bindea `app.comisiones.cuenta-corriente`/`caja-ahorro` → `5000.00` y `2000.00`. |
| `ComisionesPropertiesTest.deberiaFallarElArranque_cuandoFaltaUnaComision` | context falla si falta una propiedad (`@NotNull`). |
| `TipoTransaccionTest.deberiaExistirElTipoDebitoComision` | existe el valor `DEBITO_COMISION` (y está en `values()`). |

---

## 4. Verificación

- `./mvnw test` → **Tests run: 53, Failures: 0, Errors: 0, Skipped: 0** → BUILD SUCCESS.
- `Tp2ApplicationTests` (contexto completo con MySQL) pasa: confirma que el record
  se instancia y valida contra las propiedades reales del `application.properties`.

Criterio de aceptación cumplido: ✅ el enum y las propiedades existen y se cargan
correctamente.

---

## 5. Fuera de alcance

- **Tarea programada de liquidación mensual** (`@Scheduled` con `app.comisiones.cron`)
  y el registro real de las `Transaccion` de tipo `DEBITO_COMISION`: issue posterior.
- Cálculo proporcional/diario de comisiones: los montos son fijos globales.