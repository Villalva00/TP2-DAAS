package com.carrillovillalvadaas.tp2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Objeto de transferencia de datos (DTO) para la solicitud de apertura o creación
 * de una cuenta bancaria (Caja de Ahorro o Cuenta Corriente) asociada a un titular.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaFinancieraRequestDto {

    /**
     * Clave Bancaria Uniforme (CBU) única de la cuenta a crear.
     */
    @NotBlank(message = "El CBU es obligatorio.")
    private String cbu;

    /**
     * Alias alfanumérico asociado a la cuenta (ej. MATE.TERMO.CASA).
     */
    @NotBlank(message = "El Alias es obligatorio.")
    private String alias;

    /**
     * Saldo operativo inicial con el que se apertura la cuenta.
     * No puede ser un valor negativo.
     */
    @NotNull(message = "El saldo operativo inicial es obligatorio.")
    @PositiveOrZero(message = "El saldo inicial no puede ser un valor negativo.")
    private BigDecimal saldoOperativo;

    /**
     * Tipo de producto de cuenta bancaria a instanciar (ej. CAJA_AHORRO o CUENTA_CORRIENTE).
     */
    @NotBlank(message = "El tipo de cuenta es obligatorio.")
    private String tipoCuenta;

    /**
     * Identificador único del cliente titular que poseerá la cuenta.
     */
    @NotNull(message = "El ID del cliente titular es obligatorio.")
    private UUID clienteId;

    // --- Atributos específicos según el tipo de cuenta (TP2) ---

    // Para Caja de Ahorro:
    private Double tasaInteresAnual;
    private Integer cupoExtraccionesSinCosto; //

    // Para Cuenta Corriente:
    private BigDecimal margenDescubierto;
    private BigDecimal costoComisionMantenimiento; // ¡Agregado correctamente!
}