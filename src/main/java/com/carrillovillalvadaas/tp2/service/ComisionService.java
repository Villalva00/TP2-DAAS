package com.carrillovillalvadaas.tp2.service;

/**
 * Servicio de liquidación mensual de comisiones de mantenimiento.
 * <p>
 * Debita masivamente la comisión de todas las cuentas en estado {@code ACTIVA},
 * registrando por cada una una {@code Transaccion} de tipo {@code DEBITO_COMISION}
 * con estado {@code COMPLETADA}.
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public interface ComisionService {

    /**
     * Ejecuta la liquidación mensual de comisiones sobre todas las cuentas activas.
     * <p>
     * Cada cuenta se procesa en su propia transacción: si una falla, se registra el
     * error y la liquidación continúa con el resto.
     * </p>
     *
     * @return {@link ResultadoLiquidacion} con las cuentas procesadas, las fallidas
     *         y el total debitado.
     */
    ResultadoLiquidacion liquidarComisionesMensuales();
}