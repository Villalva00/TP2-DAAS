package com.carrillovillalvadaas.tp2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos (DTO) para la recepción y validación
 * de una solicitud transaccional en la API (Depósitos, Extracciones o Transferencias).
 * Actúa como la frontera de entrada en la capa Controller.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionRequestDto {

    /**
     * Clave Bancaria Uniforme (CBU) de la cuenta financiera principal afectada
     * o cuenta de origen desde la cual se opera.
     */
    @NotBlank(message = "El CBU de la cuenta es obligatorio.")
    private String cbuCuenta;

    /**
     * Clave Bancaria Uniforme (CBU) de la cuenta de destino.
     * Este campo es opcional para depósitos o extracciones, pero obligatorio
     * en caso de tratarse de una transferencia entre cuentas.
     */
    private String cbuDestino;

    /**
     * Monto monetario exacto de la transacción a realizar.
     * Debe ser un valor numérico estrictamente mayor a cero.
     */
    @NotNull(message = "El monto de la transacción es obligatorio.")
    @Positive(message = "El monto a operar debe ser mayor a cero.")
    private BigDecimal monto;

    /**
     * Tipo o naturaleza de la transacción a ejecutar
     * (por ejemplo: DEPOSITO, EXTRACCION, TRANSFERENCIA_ENVIADA).
     */
    @NotBlank(message = "El tipo de transacción es obligatorio.")
    private String tipoTransaccion;
}