package com.carrillovillalvadaas.tp2.model;

/**
 * Clasifica los distintos tipos de movimientos u operaciones monetarias
 * que pueden efectuarse sobre las cuentas financieras.
 */
public enum TipoTransaccion {

    /**
     * Operación de ingreso de fondos a una cuenta.
     */
    DEPOSITO,

    /**
     * Operación de retiro de fondos desde una cuenta.
     */
    EXTRACCION,

    /**
     * Movimiento correspondiente al envío de dinero hacia otra cuenta externa o interna.
     */
    TRANSFERENCIA_ENVIADA,

    /**
     * Movimiento correspondiente a la recepción de fondos provenientes de otra cuenta.
     */
    TRANSFERENCIA_RECIBIDA,

    /**
     * Débito originado por la comisión de mantenimiento cobrada en la liquidación
     * mensual. Se registra con los montos globales configurados en
     * {@link com.carrillovillalvadaas.tp2.config.ComisionesProperties}.
     */
    DEBITO_COMISION
}