package com.carrillovillalvadaas.tp2.scheduler;

import com.carrillovillalvadaas.tp2.service.ComisionService;
import com.carrillovillalvadaas.tp2.service.ResultadoLiquidacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada que dispara la liquidación mensual de comisiones.
 * <p>
 * Cron configurable mediante {@code app.comisiones.cron} (por defecto, el primer
 * día de cada mes a las 00:00; para pruebas: {@code 0 * * * * *}, cada minuto).
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LiquidacionComisionesScheduler {

    private final ComisionService comisionService;

    /**
     * Ejecuta la liquidación mensual de comisiones según el cron configurado.
     */
    @Scheduled(cron = "${app.comisiones.cron:0 0 0 1 * *}")
    public void ejecutarLiquidacionMensual() {
        log.info("Disparo programado de la liquidación mensual de comisiones");
        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();
        log.info("Liquidación programada finalizada: {}", resultado);
    }
}