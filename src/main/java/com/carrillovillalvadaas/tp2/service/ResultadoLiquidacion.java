package com.carrillovillalvadaas.tp2.service;

import java.math.BigDecimal;

/**
 * Resultado consolidado de una liquidación mensual de comisiones.
 *
 * @param procesadas   Cantidad de cuentas a las que se les debitó la comisión.
 * @param fallidas     Cantidad de cuentas cuyo débito falló (no frena al resto).
 * @param totalDebitado Suma total de comisiones debitadas en la liquidación.
 */
public record ResultadoLiquidacion(int procesadas, int fallidas, BigDecimal totalDebitado) {

    /**
     * @return {@code true} si se procesó al menos una cuenta o hubo fallos.
     */
    public boolean huboActividad() {
        return procesadas > 0 || fallidas > 0;
    }
}