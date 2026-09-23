package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.*;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.repository.TransaccionRepository;
import com.carrillovillalvadaas.tp2.service.impl.TransaccionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaFinancieraRepository cuentaFinancieraRepository;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private CajaAhorro cuenta;
    private UUID cuentaId;

    @BeforeEach
    void setUp() {
        cuentaId = UUID.randomUUID();
        cuenta = new CajaAhorro();
        cuenta.setId(cuentaId);
        cuenta.setSaldoOperativo(1000.0);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
    }

    @Test
    void registrarDeposito_deberiaActualizarSaldoYGuardarTransaccionCompletada() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaccion resultado = transaccionService.registrarDeposito(cuentaId, 200.0);

        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1200.0);
        assertThat(resultado.getEstadoTransaccion()).isEqualTo(EstadoTransaccion.COMPLETADA);
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
        assertThat(resultado.getMonto()).isEqualTo(200.0);
    }

    @Test
    void registrarExtraccion_deberiaCompletarTransaccion_cuandoHayFondosSuficientes() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaccion resultado = transaccionService.registrarExtraccion(cuentaId, 300.0);

        assertThat(cuenta.getSaldoOperativo()).isEqualTo(700.0);
        assertThat(resultado.getEstadoTransaccion()).isEqualTo(EstadoTransaccion.COMPLETADA);
    }

    @Test
    void registrarExtraccion_deberiaGuardarTransaccionRechazadaYLanzarExcepcion_cuandoNoHayFondos() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        when(transaccionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(cuentaId, 5000.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fondos insuficientes");

        // El saldo de la cuenta no debe haber cambiado...
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1000.0);
        // ...y la cuenta no debe haberse persistido
        verify(cuentaFinancieraRepository, never()).save(any());
        // ...pero igual queda registro de la transacción, con estado RECHAZADA
        assertThat(captor.getValue().getEstadoTransaccion()).isEqualTo(EstadoTransaccion.RECHAZADA);
    }

    @Test
    void obtenerPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.obtenerPorId(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listarPorCuenta_deberiaDelegarEnElRepositorio() {
        transaccionService.listarPorCuenta(cuentaId);

        verify(transaccionRepository).findByCuentaFinancieraId(cuentaId);
    }
}
