package com.carrillovillalvadaas.tp2.exception;

/**
 * Excepción personalizada de negocio que extiende de {@link RuntimeException}.
 * <p>
 * Se lanza explícitamente desde la capa de servicios cuando se intenta buscar
 * un recurso persistente en la base de datos (por ejemplo, un {@code Cliente} por su ID
 * o una {@code CuentaFinanciera} por su CBU) y dicho registro no existe o no se encuentra activo.
 * Su captura en la capa web debe traducirse en un código de estado HTTP 404 (Not Found).
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Construye una nueva excepción de recurso no encontrado con el mensaje detallado especificado.
     *
     * @param mensaje Descripción clara y específica del recurso que no pudo ser localizado.
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}