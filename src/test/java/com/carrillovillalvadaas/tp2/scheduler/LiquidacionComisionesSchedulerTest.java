package com.carrillovillalvadaas.tp2.scheduler;

import com.carrillovillalvadaas.tp2.service.ComisionService;
import com.carrillovillalvadaas.tp2.service.ResultadoLiquidacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la tarea programada de liquidación mensual
 * ({@link LiquidacionComisionesScheduler}).
 */
@ExtendWith(MockitoExtension.class)
class LiquidacionComisionesSchedulerTest {

    @Mock
    private ComisionService comisionService;

    @Test
    void ejecutarLiquidacionMensual_deberiaInvocarElServicioDeComisiones() {
        LiquidacionComisionesScheduler scheduler = new LiquidacionComisionesScheduler(comisionService);
        when(comisionService.liquidarComisionesMensuales())
                .thenReturn(new ResultadoLiquidacion(1, 0, BigDecimal.valueOf(2000.00)));

        scheduler.ejecutarLiquidacionMensual();

        verify(comisionService).liquidarComisionesMensuales();
    }
}