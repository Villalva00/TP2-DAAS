package com.carrillovillalvadaas.tp2.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Objeto de transferencia de datos (DTO) para exponer el resultado detallado
 * y el estado resultante de una operación transaccional procesada por el sistema.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionResponseDto {

    /**
     * Identificador único de la transacción registrada en la base de datos (Auditoría).
     */
    private Long idTransaccion;

    /**
     * CBU de la cuenta principal involucrada en la operación.
     */
    private String cbuCuenta;

    /**
     * CBU de la cuenta de destino (si la operación involucró a un tercero).
     */
    private String cbuDestino;

    /**
     * Monto monetario operado.
     */
    private BigDecimal monto;

    /**
     * Tipo de transacción ejecutada.
     */
    private String tipoTransaccion;

    /**
     * Estado operativo final de la transacción
     * (por ejemplo: PENDIENTE, COMPLETADA, RECHAZADA, REVERTIDA).
     */
    private String estadoTransaccion;

    /**
     * Marca temporal exacta de la fecha y hora en que se procesó la operación.
     */
    private LocalDateTime fechaHora;

    /**
     * Mensaje descriptivo con el resultado o respuesta del procesamiento.
     */
    private String mensaje;
}