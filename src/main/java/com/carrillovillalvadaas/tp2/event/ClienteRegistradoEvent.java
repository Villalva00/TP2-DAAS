package com.carrillovillalvadaas.tp2.event;

import java.util.UUID;

/**
 * Evento de dominio publicado cuando un cliente (titular o adherente) es
 * registrado exitosamente.
 * <p>
 * Transporta únicamente datos primitivos (sin entidades JPA) para desacoplar
 * a los oyentes de la capa de persistencia y evitar problemas de lazy loading
 * una vez que la transacción de alta ya se confirmó.
 * </p>
 *
 * @param clienteId Identificador del cliente recién guardado.
 * @param nombre    Nombre completo del cliente (para el saludo del email).
 * @param email     Correo electrónico de destino.
 * @param token     Valor del token de activación que viaja por email.
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public record ClienteRegistradoEvent(UUID clienteId, String nombre, String email, UUID token) {
}
