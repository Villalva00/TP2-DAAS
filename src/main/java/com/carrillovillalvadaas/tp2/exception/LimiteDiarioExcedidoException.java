package com.carrillovillalvadaas.tp2.exception;

/**
 * Excepción personalizada de negocio que extiende de {@link RuntimeException}.
 * <p>
 * Se lanza desde la capa de servicios cuando una extracción haría que la suma de las
 * extracciones COMPLETADAS del día del mismo ejecutor (titular o adherente) supere
 * su tope diario parametrizado. La transacción asociada se persiste como RECHAZADA
 * antes de lanzar esta excepción y su captura en la capa web debe traducirse en un
 * código de estado HTTP 400 (Bad Request).
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public class LimiteDiarioExcedidoException extends RuntimeException {

    private final Double tope;
    private final Double acumulado;

    /**
     * Construye una nueva excepción de límite diario excedido.
     *
     * @param mensaje    Descripción detallada del rechazo.
     * @param tope       Tope diario configurado para el tipo de cliente.
     * @param acumulado  Monto ya extraído por el ejecutor en el día (solo extracciones completadas).
     */
    public LimiteDiarioExcedidoException(String mensaje, Double tope, Double acumulado) {
        super(mensaje);
        this.tope = tope;
        this.acumulado = acumulado;
    }

    public Double getTope() {
        return tope;
    }

    public Double getAcumulado() {
        return acumulado;
    }
}
