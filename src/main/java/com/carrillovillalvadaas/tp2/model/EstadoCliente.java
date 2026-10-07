package com.carrillovillalvadaas.tp2.model;

/**
 * Define los posibles estados por los que puede transicionar un cliente a lo largo
 * de su ciclo de vida en el sistema.
 */
public enum EstadoCliente {

    /**
     * El cliente fue registrado pero aún no confirmó su cuenta mediante el token de
     * activación enviado por email. No puede operar hasta que sea ACTIVO.
     */
    PENDIENTE_ACTIVACION,

    /**
     * El cliente confirmó su cuenta (o fue dado de alta como dato de prueba) y
     * ya puede realizar operaciones en el sistema.
     */
    ACTIVO
}
