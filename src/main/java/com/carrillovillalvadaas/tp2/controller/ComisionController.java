package com.carrillovillalvadaas.tp2.controller;

import com.carrillovillalvadaas.tp2.service.ComisionService;
import com.carrillovillalvadaas.tp2.service.ResultadoLiquidacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints administrativos para la gestión de comisiones.
 * <p>
 * Permite disparar manualmente la liquidación mensual (útil en la demo y para
 * pruebas con cron por minuto sin esperar al día 1 del mes).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/comisiones")
@Slf4j
@RequiredArgsConstructor
public class ComisionController {

    private final ComisionService comisionService;

    /**
     * Dispara manualmente la liquidación mensual de comisiones sobre todas las
     * cuentas activas.
     *
     * @return El {@link ResultadoLiquidacion} con procesadas, fallidas y total
     *         debitado, con HTTP 200 (OK).
     */
    @PostMapping("/liquidar")
    public ResponseEntity<ResultadoLiquidacion> liquidarComisionesManual() {
        log.info("Recibida solicitud HTTP POST en /api/v1/admin/comisiones/liquidar (disparo manual)");
        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();
        return ResponseEntity.ok(resultado);
    }
}