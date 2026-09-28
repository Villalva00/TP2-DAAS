package com.carrillovillalvadaas.tp2.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos (DTO) para retornar los detalles completos
 * de una cuenta financiera y su saldo actual ante una consulta de saldo o detalle.
 * Contiene tanto los atributos generales como los específicos de cada producto (TP2).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaFinancieraResponseDto {

    /**
     * Clave Bancaria Uniforme (CBU) única de la cuenta bancaria.
     */
    private String cbu;

    /**
     * Alias alfanumérico asociado a la cuenta.
     */
    private String alias;

    /**
     * Saldo operativo disponible actual en la cuenta.
     */
    private BigDecimal saldoOperativo;

    /**
     * Estado operativo actual de la cuenta (ACTIVA, SUSPENDIDA, BLOQUEADA)
     */
    private String estado;

    /**
     * Tipo de cuenta financiera de la que se trata (CAJA_AHORRO o CUENTA_CORRIENTE)
     */
    private String tipoCuenta;


    /**
     * Tasa de interés anual (aplicable y visible si corresponde a una Caja de Ahorro)
     */
    private Double tasaInteresAnual;

    /**
     * Cupo límite de extracciones mensuales sin costo (aplicable a Caja de Ahorro)
     */
    private Integer cupoExtraccionesSinCosto;

    /**
     * Margen de descubierto autorizado (aplicable y visible si corresponde a una Cuenta Corriente)
     */
    private BigDecimal margenDescubierto;

    /**
     * Costo de comisión de mantenimiento mensual (aplicable a Cuenta Corriente)
     */
    private BigDecimal costoComisionMantenimiento;

    /**
     * DTO resumido del cliente titular asociado a la cuenta (soporta co-titularidad o titular principal)
     */
    private ClienteResponseDto titular;
}