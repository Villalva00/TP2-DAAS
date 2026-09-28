package com.carrillovillalvadaas.tp2.exception;

/**
 * Excepción personalizada de negocio que extiende de {@link RuntimeException}.
 * <p>
 * Se lanza explícitamente desde la capa de servicios cuando se intenta procesar
 * una operación de débito, extracción o transferencia de fondos y la cuenta de origen
 * no dispone de los fondos suficientes en su saldo operativo ni cuenta con el margen
 * de descubierto autorizado para cubrir la transacción.
 * Su captura en la capa web debe traducirse en un código de estado HTTP 400 (Bad Request).
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public class SaldoInsuficienteException extends RuntimeException {

    /**
     * Construye una nueva excepción de saldo insuficiente con el mensaje detallado especificado.
     *
     * @param mensaje Descripción explicativa sobre el déficit financiero que impidió la operación.
     */
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}