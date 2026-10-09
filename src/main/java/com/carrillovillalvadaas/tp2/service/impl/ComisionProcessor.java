package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.config.ComisionesProperties;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.model.CajaAhorro;
import com.carrillovillalvadaas.tp2.model.CuentaCorriente;
import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Procesa el débito de la comisión mensual de una única cuenta financiera.
 * <p>
 * Cada invocación corre en <b>su propia transacción</b>
 * ({@code REQUIRES_NEW}), de modo que el fallo de una cuenta no revierte ni
 * detiene la liquidación de las demás.
 * </p>
 * <p>
 * La comisión es un cargo fijo global: se descuenta con {@code actualizarSaldo(-monto)}
 * (nunca con {@code extraer()}, que valida fondos). Por diseño, el saldo de la
 * cuenta puede quedar negativo tras la liquidación.
 * </p>
 * <p>
 * Idempotencia: si la cuenta ya tiene un {@code DEBITO_COMISION} en el mes en
 * curso, se omite el débito.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ComisionProcessor {

    private final CuentaFinancieraRepository cuentaFinancieraRepository;
    private final TransaccionRepository transaccionRepository;
    private final ComisionesProperties comisionesProperties;

    /**
     * Debita la comisión de mantenimiento de la cuenta indicada y registra la
     * transacción de tipo {@code DEBITO_COMISION}.
     *
     * @param cuentaId Identificador de la cuenta financiera.
     * @return El monto debitado, o {@code 0.0} si ya estaba liquidada en el mes.
     * @throws RecursoNoEncontradoException si la cuenta no existe.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Double procesarCuenta(UUID cuentaId) {
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con ID: " + cuentaId));

        YearMonth mesActual = YearMonth.now();
        LocalDateTime desde = mesActual.atDay(1).atStartOfDay();
        LocalDateTime hasta = mesActual.plusMonths(1).atDay(1).atStartOfDay();

        if (transaccionRepository.existsDebitoComisionEnPeriodo(
                cuentaId, TipoTransaccion.DEBITO_COMISION, desde, hasta)) {
            log.info("La cuenta {} ya fue debitada por comisión este mes; se omite.", cuentaId);
            return 0.0;
        }

        Double monto = comisionPara(cuenta);

        cuenta.actualizarSaldo(-monto);
        cuentaFinancieraRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(TipoTransaccion.DEBITO_COMISION)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuentaFinanciera(cuenta)
                .ejecutor(null)
                .build();
        transaccionRepository.save(transaccion);

        log.info("Comisión de {} debitada a la cuenta {} (saldo {})", monto, cuentaId, cuenta.getSaldoOperativo());
        return monto;
    }

    /**
     * Devuelve el monto de la comisión según el tipo concreto de la cuenta,
     * usando {@link Hibernate#unproxy} para resolver los proxies de la herencia
     * {@code JOINED} de {@link CuentaFinanciera}.
     *
     * @param cuenta Cuenta activa a liquidar.
     * @return Monto global configurado para ese tipo de cuenta.
     */
    private Double comisionPara(CuentaFinanciera cuenta) {
        Object cuentaReal = Hibernate.unproxy(cuenta);
        if (cuentaReal instanceof CuentaCorriente) {
            return comisionesProperties.cuentaCorriente();
        }
        if (cuentaReal instanceof CajaAhorro) {
            return comisionesProperties.cajaAhorro();
        }
        throw new IllegalStateException(
                "Tipo de cuenta no soportado para la liquidación de comisiones: "
                        + cuentaReal.getClass().getSimpleName());
    }
}