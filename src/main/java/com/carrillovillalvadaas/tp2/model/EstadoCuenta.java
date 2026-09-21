package com.carrillovillalvadaas.tp2.model;

/**
 * Representa los diferentes estados operativos que puede adoptar una cuenta financiera
 * dentro del sistema bancario.
 */
public enum EstadoCuenta {

    /**
     * La cuenta se encuentra operativa y habilitada para realizar todo tipo de transacciones.
     */
    ACTIVA,

    /**
     * La cuenta está temporalmente suspendida por cuestiones administrativas o de seguridad.
     */
    SUSPENDIDA,

    /**
     * La cuenta se encuentra bloqueada, impidiendo la ejecución de operaciones financieras.
     */
    BLOQUEADA
}