package com.carrillovillalvadaas.tp2.exception;


/**
 * Se lanza cuando un cliente intenta realizar una operación para la que no tiene permiso
 * (por ejemplo, un adherente intentando depositar, u operar sobre la cuenta de otro titular).
 * Se traduce a HTTP 403 (Forbidden).
 */

public class OperacionNoPermitidaException extends RuntimeException {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
