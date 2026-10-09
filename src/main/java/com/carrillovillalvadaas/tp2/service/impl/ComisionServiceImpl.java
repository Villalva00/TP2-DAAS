package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.service.ComisionService;
import com.carrillovillalvadaas.tp2.service.ResultadoLiquidacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementación de {@link ComisionService} que orquesta la liquidación mensual
 * de comisiones.
 * <p>
 * Recupera las cuentas en estado {@code ACTIVA} (las {@code SUSPENDIDA} y
 * {@code BLOQUEADA} quedan fuera del filtro del repositorio) y delega en
 * {@link ComisionProcessor} el débito de cada una en una transacción aislada.
 * Un fallo en una cuenta se loguea y no interrumpe el resto.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ComisionServiceImpl implements ComisionService {

    private final CuentaFinancieraRepository cuentaFinancieraRepository;
    private final ComisionProcessor comisionProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultadoLiquidacion liquidarComisionesMensuales() {
        List<CuentaFinanciera> cuentasActivas = cuentaFinancieraRepository.findByEstado(EstadoCuenta.ACTIVA);
        log.info("Liquidación mensual de comisiones: {} cuentas activas encontradas", cuentasActivas.size());

        int procesadas = 0;
        int fallidas = 0;
        BigDecimal totalDebitado = BigDecimal.ZERO;

        for (CuentaFinanciera cuenta : cuentasActivas) {
            try {
                Double monto = comisionProcessor.procesarCuenta(cuenta.getId());
                if (monto > 0) {
                    procesadas++;
                    totalDebitado = totalDebitado.add(BigDecimal.valueOf(monto));
                }
            } catch (Exception ex) {
                fallidas++;
                log.error("Falló la liquidación de la comisión de la cuenta {}. Continúa con la siguiente.",
                        cuenta.getId(), ex);
            }
        }

        ResultadoLiquidacion resultado = new ResultadoLiquidacion(procesadas, fallidas, totalDebitado);
        log.info("Liquidación completada: procesadas={}, fallidas={}, total={}",
                resultado.procesadas(), resultado.fallidas(), resultado.totalDebitado());
        return resultado;
    }
}