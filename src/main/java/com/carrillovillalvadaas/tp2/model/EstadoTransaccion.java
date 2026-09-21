package com.carrillovillalvadaas.tp2.model;

/**
 * Define los posibles estados por los que puede transicionar una transacción monetaria
 * a lo largo de su ciclo de vida en el sistema.
 */
public enum EstadoTransaccion {

    /**
     * La transacción ha sido registrada pero se encuentra a la espera de validación o procesamiento.
     */
    PENDIENTE,

    /**
     * La transacción se procesó y finalizó de manera exitosa.
     */
    COMPLETADA,

    /**
     * La transacción fue denegada debido a validaciones de saldo, seguridad o reglas de negocio.
     */
    RECHAZADA,

    /**
     * La transacción fue anulada o deshecha después de haber sido completada.
     */
    REVERTIDA
}