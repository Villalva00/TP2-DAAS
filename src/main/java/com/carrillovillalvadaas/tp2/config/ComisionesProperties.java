package com.carrillovillalvadaas.tp2.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Montos globales de las comisiones de mantenimiento mensuales, parametrizables
 * desde {@code application.properties}.
 * <p>
 * La liquidación mensual usa estos montos fijos (no el campo propio de cada
 * cuenta) y registra cada débito como una {@code Transaccion} de tipo
 * {@code DEBITO_COMISION}.
 * </p>
 *
 * @param cuentaCorriente Comisión mensual de una cuenta corriente.
 * @param cajaAhorro      Comisión mensual de una caja de ahorro.
 */
@Validated
@ConfigurationProperties(prefix = "app.comisiones")
public record ComisionesProperties(
        @NotNull @Positive Double cuentaCorriente,
        @NotNull @Positive Double cajaAhorro) {
}