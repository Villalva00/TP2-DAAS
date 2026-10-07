package com.carrillovillalvadaas.tp2.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Objeto de transferencia de datos (DTO) para exponer la información pública
 * de un Cliente hacia el exterior, evitando exponer directamente las entidades JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDto {

    /**
     * Identificador único del cliente en el sistema.
     */
    private UUID id;

    /**
     * Nombre o razón social registrada del cliente.
     */
    private String nombre;

    /**
     * CUIL del cliente.
     */
    private String cuil;

    /**
     * Correo electrónico registrado.
     */
    private String email;

    /**
     * Marca temporal de auditoría que indica cuándo fue creado el registro del cliente.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Marca temporal de auditoria qeu indica la ultima actualizacion.
     */
    private LocalDateTime ultimaModificacion;

    /*
    *Rol del cliente en el grupo familiar (TITULAR O ADHERENTE)
     */
    private String tipoCliente;
    /*
    * Parentesco con el titular (CONYUGE O HIJO). Null si es titular.
     */
    private String parentesco;

    /*
    * Estado de activación del cliente (PENDIENTE_ACTIVACION o ACTIVO).
     * El token de activación nunca se expone en este DTO.
     */
    private String estado;
}