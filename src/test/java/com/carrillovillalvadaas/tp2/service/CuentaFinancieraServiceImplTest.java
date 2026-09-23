package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.CajaAhorro;
import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.service.impl.CuentaFinancieraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaFinancieraServiceImplTest {

    @Mock
    private CuentaFinancieraRepository cuentaFinancieraRepository;

    @InjectMocks
    private CuentaFinancieraServiceImpl cuentaFinancieraService;

    private CajaAhorro cuenta;
    private UUID cuentaId;

    @BeforeEach
    void setUp() {
        cuentaId = UUID.randomUUID();
        cuenta = new CajaAhorro();
        cuenta.setId(cuentaId);
        cuenta.setCbu(1234567890123456789L);
        cuenta.setAlias("mi.cuenta.test");
        cuenta.setSaldoOperativo(1000.0);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTasaInteresAnual(50.0);
        cuenta.setLimiteExtraccion(5);
    }

    @Test
    void crearCuenta_deberiaAsignarEstadoActivoYSaldoCero_siNoVienenSeteados() {
        CajaAhorro nueva = new CajaAhorro();
        nueva.setCbu(999L);
        nueva.setAlias("nueva.cuenta");
        // no se setea estado ni saldoOperativo a propósito

        when(cuentaFinancieraRepository.save(nueva)).thenReturn(nueva);

        CuentaFinanciera resultado = cuentaFinancieraService.crearCuenta(nueva);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
        assertThat(resultado.getSaldoOperativo()).isEqualTo(0.0);
    }

    @Test
    void obtenerPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(cuentaFinancieraRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaFinancieraService.obtenerPorId(idInexistente))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void depositar_deberiaIncrementarSaldo_cuandoMontoEsValido() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        CuentaFinanciera resultado = cuentaFinancieraService.depositar(cuentaId, 500.0);

        assertThat(resultado.getSaldoOperativo()).isEqualTo(1500.0);
    }

    @Test
    void depositar_deberiaLanzarExcepcion_cuandoMontoEsInvalido() {
        assertThatThrownBy(() -> cuentaFinancieraService.depositar(cuentaId, -100.0))
                .isInstanceOf(IllegalArgumentException.class);

        // Como el monto es inválido, ni siquiera debería buscar la cuenta
        verifyNoInteractions(cuentaFinancieraRepository);
    }

    @Test
    void extraer_deberiaDecrementarSaldo_cuandoHayFondosSuficientes() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        CuentaFinanciera resultado = cuentaFinancieraService.extraer(cuentaId, 300.0);

        assertThat(resultado.getSaldoOperativo()).isEqualTo(700.0);
    }

    @Test
    void extraer_deberiaLanzarExcepcion_cuandoNoHayFondosSuficientes() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> cuentaFinancieraService.extraer(cuentaId, 5000.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fondos insuficientes");

        // El saldo no debe haberse persistido si la operación se rechazó
        verify(cuentaFinancieraRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_deberiaActualizarElEstadoDeLaCuenta() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));
        ArgumentCaptor<CuentaFinanciera> captor = ArgumentCaptor.forClass(CuentaFinanciera.class);
        when(cuentaFinancieraRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        cuentaFinancieraService.cambiarEstado(cuentaId, EstadoCuenta.SUSPENDIDA);

        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoCuenta.SUSPENDIDA);
    }

    @Test
    void listarPorCliente_deberiaDelegarEnElRepositorio() {
        UUID clienteId = UUID.randomUUID();
        when(cuentaFinancieraRepository.findByClienteId(clienteId)).thenReturn(List.of(cuenta));

        List<CuentaFinanciera> resultado = cuentaFinancieraService.listarPorCliente(clienteId);

        assertThat(resultado).containsExactly(cuenta);
    }
}
