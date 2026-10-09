package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.model.CuentaCorriente;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.service.ResultadoLiquidacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la orquestación de la liquidación mensual
 * ({@link ComisionServiceImpl}).
 */
@ExtendWith(MockitoExtension.class)
class ComisionServiceImplTest {

    @Mock
    private CuentaFinancieraRepository cuentaFinancieraRepository;

    @Mock
    private ComisionProcessor comisionProcessor;

    private ComisionServiceImpl comisionService;

    @BeforeEach
    void setUp() {
        comisionService = new ComisionServiceImpl(cuentaFinancieraRepository, comisionProcessor);
    }

    @Test
    void liquidarComisionesMensuales_deberiaDebitarSoloCuentasActivas() {
        CuentaCorriente cc = cuentaActiva();
        CuentaCorriente ca = cuentaActiva();
        when(cuentaFinancieraRepository.findByEstado(EstadoCuenta.ACTIVA)).thenReturn(List.of(cc, ca));
        when(comisionProcessor.procesarCuenta(cc.getId())).thenReturn(5000.00);
        when(comisionProcessor.procesarCuenta(ca.getId())).thenReturn(2000.00);

        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();

        assertThat(resultado.procesadas()).isEqualTo(2);
        assertThat(resultado.fallidas()).isZero();
        assertThat(resultado.totalDebitado()).isEqualByComparingTo("7000.00");
        verify(cuentaFinancieraRepository).findByEstado(EstadoCuenta.ACTIVA);
    }

    @Test
    void liquidarComisionesMensuales_noDebeFrenarseCuandoUnaCuentaFalla() {
        CuentaCorriente falla = cuentaActiva();
        CuentaCorriente ok = cuentaActiva();
        when(cuentaFinancieraRepository.findByEstado(EstadoCuenta.ACTIVA)).thenReturn(List.of(falla, ok));
        when(comisionProcessor.procesarCuenta(falla.getId())).thenThrow(new IllegalStateException("saldo inconsistente"));
        when(comisionProcessor.procesarCuenta(ok.getId())).thenReturn(2000.00);

        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();

        assertThat(resultado.procesadas()).isEqualTo(1);
        assertThat(resultado.fallidas()).isEqualTo(1);
        assertThat(resultado.totalDebitado()).isEqualByComparingTo("2000.00");
    }

    @Test
    void liquidarComisionesMensuales_sinCuentasActivas_deberiaDevolverCero() {
        when(cuentaFinancieraRepository.findByEstado(EstadoCuenta.ACTIVA)).thenReturn(List.of());

        ResultadoLiquidacion resultado = comisionService.liquidarComisionesMensuales();

        assertThat(resultado.procesadas()).isZero();
        assertThat(resultado.fallidas()).isZero();
        assertThat(resultado.totalDebitado()).isEqualByComparingTo("0");
    }

    private CuentaCorriente cuentaActiva() {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(UUID.randomUUID());
        cuenta.setCbu(9000000000000000L + System.nanoTime() % 1000);
        cuenta.setAlias("cc." + UUID.randomUUID());
        cuenta.setSaldoOperativo(100.0);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        return cuenta;
    }
}