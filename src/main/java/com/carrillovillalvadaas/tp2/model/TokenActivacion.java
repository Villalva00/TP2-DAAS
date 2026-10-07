package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa el token de activación enviado por email a un cliente recién
 * registrado. El token es un UUID único con fecha de expiración (24 horas desde su
 * emisión) y marca de uso, y mantiene una relación uno a uno con el {@link Cliente}
 * al que pertenece.
 *
 * @see EntidadAuditable
 * @see EstadoCliente
 */
@Entity
@Table(name = "token_activacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenActivacion extends EntidadAuditable {

    /**
     * Identificador unico autogenerado del token.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Valor único del token (UUID) que debe ingresar el cliente para activarse.
     */
    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    /**
     * Fecha y hora en la que el token deja de ser válido (emisión + 24 horas).
     */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Indica si el token ya fue canjeado. Un token usado no puede volver a activar.
     */
    @Column(name = "usado", nullable = false)
    @Builder.Default
    private boolean usado = false;

    /**
     * Cliente al que pertenece este token. Es la columna foránea de la tabla
     * token_activacion hacia la tabla cliente.
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
}
