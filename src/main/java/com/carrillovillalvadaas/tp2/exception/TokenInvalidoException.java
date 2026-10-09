package com.carrillovillalvadaas.tp2.exception;

/**
 * Excepción de negocio que indica que el token de activación provisto no es aceptable
 * para confirmar una cuenta.
 * <p>
 * Se lanza desde la capa de servicios cuando se intenta activar un cliente con un
 * token que: no tiene formato UUID válido, no existe en la base de datos, ya fue
 * utilizado o está vencido. Su captura en la capa web se traduce en
 * HTTP 400 (Bad Request).
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public class TokenInvalidoException extends RuntimeException {

    /**
     * Construye una nueva excepción de token inválido con el mensaje detallado.
     *
     * @param mensaje Descripción del motivo por el cual el token fue rechazado.
     */
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}