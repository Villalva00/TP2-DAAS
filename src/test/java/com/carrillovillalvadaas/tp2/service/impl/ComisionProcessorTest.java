package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.config.ComisionesProperties;
import com.carrillovillalvadaas.tp2.model.CajaAhorro;
import com.carrillovillalvadaas.tp2.model.CuentaCorriente;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del débito de comisiones por cuenta
 * ({@link ComisionProcessor}).
 */
@ExtendWith(MockitoExtension.class)
class ComisionProcessorTest {

    @Mock
    private CuentaFinancieraRepository cuentaFinancieraRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    private ComisionProcessor comisionProcessor;
    private ComisionesProperties comisionesProperties;

    @BeforeEach
    void setUp() {
        comisionesProperties = new ComisionesProperties(5000.00, 2000.00);
        comisionProcessor = new ComisionProcessor(
                cuentaFinancieraRepository, transaccionRepository, comisionesProperties);
    }

    @Test
    void procesarCuenta_deberiaDebitar5000EnUnaCuentaCorriente() {
        CuentaCorriente cuenta = cuentaCorrienteActiva(0.0);
        when(cuentaFinancieraRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsDebitoComisionEnPeriodo(any(), any(), any(), any())).thenReturn(false);
        when(cuentaFinancieraRepository.save(any(CuentaCorriente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Double monto = comisionProcessor.procesarCuenta(cuenta.getId());

        assertThat(monto).isEqualTo(5000.00);
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(-5000.00);

        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        Transaccion tx = captor.getValue();
        assertThat(tx.getTipo()).isEqualTo(TipoTransaccion.DEBITO_COMISION);
        assertThat(tx.getEstadoTransaccion()).isEqualTo(EstadoTransaccion.COMPLETADA);
        assertThat(tx.getEjecutor()).isNull();
        assertThat(tx.getMonto()).isEqualTo(5000.00);
        assertThat(tx.getCuentaFinanciera()).isSameAs(cuenta);
    }

    @Test
    void procesarCuenta_deberiaDebitar2000EnUnaCajaDeAhorro() {
        CajaAhorro cuenta = cajaAhorroActiva(0.0);
        when(cuentaFinancieraRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsDebitoComisionEnPeriodo(any(), any(), any(), any())).thenReturn(false);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cuentaFinancieraRepository.save(any(CajaAhorro.class))).thenAnswer(inv -> inv.getArgument(0));

        Double monto = comisionProcessor.procesarCuenta(cuenta.getId());

        assertThat(monto).isEqualTo(2000.00);
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(-2000.00);
    }

    @Test
    void procesarCuenta_deberiaOmitir_cuandoYaFueDebitadaEnElMes() {
        CuentaCorriente cuenta = cuentaCorrienteActiva(100.0);
        when(cuentaFinancieraRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsDebitoComisionEnPeriodo(any(), any(), any(), any())).thenReturn(true);

        Double monto = comisionProcessor.procesarCuenta(cuenta.getId());

        assertThat(monto).isZero();
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(100.0);
        verify(cuentaFinancieraRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    private CuentaCorriente cuentaCorrienteActiva(double saldo) {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(UUID.randomUUID());
        cuenta.setCbu(9000000000000000L);
        cuenta.setAlias("cc.comision");
        cuenta.setSaldoOperativo(saldo);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        return cuenta;
    }

    private CajaAhorro cajaAhorroActiva(double saldo) {
        CajaAhorro cuenta = new CajaAhorro();
        cuenta.setId(UUID.randomUUID());
        cuenta.setCbu(9000000000000001L);
        cuenta.setAlias("ca.comision");
        cuenta.setSaldoOperativo(saldo);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        return cuenta;
    }
}