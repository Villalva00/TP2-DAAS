package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.TransaccionRequestDto;
import com.carrillovillalvadaas.tp2.dto.TransaccionResponseDto;
import com.carrillovillalvadaas.tp2.exception.OperacionNoPermitidaException;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.exception.SaldoInsuficienteException;
import com.carrillovillalvadaas.tp2.model.*;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la implementación de servicios de transacciones ({@link TransaccionServiceImpl}).
 * <p>
 * Valida de forma aislada el procesamiento correcto de depósitos y extracciones mediante DTOs,
 * el manejo de excepciones de negocio (como {@link RecursoNoEncontradoException} y {@link SaldoInsuficienteException})
 * y la correcta delegación a los repositorios simulados con Mockito.
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 * Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
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
    private TransaccionRequestDto requestDto;

    @Mock
    private ClienteRepository clienteRepository;

    private Cliente titular;
    private Cliente adherente;
    /**
     * Configuración inicial previa a cada prueba unitaria.
     * Inicializa una cuenta de prueba y un DTO de solicitud estándar.
     */
    @BeforeEach
    void setUp() {
        cuentaId = UUID.randomUUID();
        cuenta = new CajaAhorro();
        cuenta.setId(cuentaId);
        cuenta.setCbu(1234567890123456789L);
        cuenta.setSaldoOperativo(1000.0);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        titular = Cliente.builder().id(UUID.randomUUID()).nombre("Titular").build();
        adherente = Cliente.builder().id(UUID.randomUUID()).nombre("Adherente")
                .tipoCliente(TipoCliente.ADHERENTE)
                .clientePrincipal(titular)
                .parentesco(Parentesco.HIJO)
                .build();
        cuenta.setCliente(titular);
        requestDto = new TransaccionRequestDto();
        requestDto.setCbuCuenta("1234567890123456789");
        requestDto.setMonto(BigDecimal.valueOf(200.0));
        requestDto.setTipoTransaccion("DEPOSITO");
    }

    /**
     * Verifica que registrar un depósito actualice el saldo operativo de la cuenta,
     * persista la transacción y retorne un {@link TransaccionResponseDto} completado.
     */
    @Test
    void registrarDeposito_deberiaActualizarSaldoYGuardarTransaccionCompletada() {
        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaccion transaccionMock = Transaccion.builder()
                .id(1L)
                .monto(200.0)
                .tipo(TipoTransaccion.DEPOSITO)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuentaFinanciera(cuenta)
                .fechaHora(LocalDateTime.now())
                .build();

        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionMock);

        TransaccionResponseDto resultado = transaccionService.registrarDeposito(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstadoTransaccion()).isEqualTo("COMPLETADA");
        assertThat(resultado.getTipoTransaccion()).isEqualTo("DEPOSITO");
        assertThat(resultado.getMonto()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1200.0);
    }

    /**
     * Verifica que una extracción válida complete la transacción de manera exitosa
     * y descuente el monto del saldo operativo de la cuenta.
     */
    @Test
    void registrarExtraccion_deberiaCompletarTransaccion_cuandoHayFondosSuficientes() {
        requestDto.setMonto(BigDecimal.valueOf(300.0));
        requestDto.setTipoTransaccion("EXTRACCION");

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaccion transaccionMock = Transaccion.builder()
                .id(2L)
                .monto(300.0)
                .tipo(TipoTransaccion.EXTRACCION)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuentaFinanciera(cuenta)
                .fechaHora(LocalDateTime.now())
                .build();

        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionMock);

        TransaccionResponseDto resultado = transaccionService.registrarExtraccion(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstadoTransaccion()).isEqualTo("COMPLETADA");
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(700.0);
    }

    /**
     * Verifica que se lance la excepción personalizada {@link SaldoInsuficienteException}
     * cuando se intenta extraer un monto superior al saldo disponible en la cuenta.
     */
    @Test
    void registrarExtraccion_deberiaLanzarSaldoInsuficienteException_cuandoNoHayFondos() {
        requestDto.setMonto(BigDecimal.valueOf(5000.0));
        requestDto.setTipoTransaccion("EXTRACCION");

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));

        Transaccion transaccionRechazadaMock = Transaccion.builder()
                .id(3L)
                .monto(5000.0)
                .tipo(TipoTransaccion.EXTRACCION)
                .estadoTransaccion(EstadoTransaccion.RECHAZADA)
                .cuentaFinanciera(cuenta)
                .fechaHora(LocalDateTime.now())
                .build();

        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionRechazadaMock);

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(requestDto))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessageContaining("Fondos insuficientes");

        // El saldo de la cuenta no debe modificarse y no debe persistirse la cuenta
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1000.0);
        verify(cuentaFinancieraRepository, never()).save(any());
    }

    /**
     * Verifica que se lance la excepción {@link RecursoNoEncontradoException}
     * al intentar buscar una transacción mediante un ID numérico que no existe.
     */
    @Test
    void obtenerPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.obtenerPorId(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Transacción no encontrada");
    }

    /**
     * Verifica que la consulta del historial de transacciones por cuenta delegue correctamente en el repositorio.
     */
    @Test
    void listarPorCuenta_deberiaDelegarEnElRepositorio() {
        when(transaccionRepository.findByCuentaFinancieraId(cuentaId)).thenReturn(List.of());

        List<TransaccionResponseDto> resultado = transaccionService.listarPorCuenta(cuentaId);

        assertThat(resultado).isNotNull();
        verify(transaccionRepository).findByCuentaFinancieraId(cuentaId);
    }
    @Test
    void registrarDeposito_deberiaRechazar_cuandoElEjecutorEsAdherente() {
        requestDto.setClienteEjecutorId(adherente.getId());
        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(adherente.getId())).thenReturn(Optional.of(adherente));

        assertThatThrownBy(() -> transaccionService.registrarDeposito(requestDto))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("solo pueden realizar extracciones");

        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1000.0);
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void registrarExtraccion_deberiaGuardarElEjecutor_cuandoEsAdherenteDelTitular() {
        requestDto.setMonto(BigDecimal.valueOf(300.0));
        requestDto.setTipoTransaccion("EXTRACCION");
        requestDto.setClienteEjecutorId(adherente.getId());

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(adherente.getId())).thenReturn(Optional.of(adherente));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        when(transaccionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        transaccionService.registrarExtraccion(requestDto);

        assertThat(captor.getValue().getEjecutor()).isSameAs(adherente);
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(700.0);
    }

    @Test
    void registrarExtraccion_deberiaRechazar_cuandoElAdherenteEsDeOtroTitular() {
        Cliente otroTitular = Cliente.builder().id(UUID.randomUUID()).nombre("Otro").build();
        Cliente adherenteAjeno = Cliente.builder().id(UUID.randomUUID()).nombre("Ajeno")
                .tipoCliente(TipoCliente.ADHERENTE)
                .clientePrincipal(otroTitular)
                .parentesco(Parentesco.CONYUGE)
                .build();
        requestDto.setTipoTransaccion("EXTRACCION");
        requestDto.setClienteEjecutorId(adherenteAjeno.getId());

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(adherenteAjeno.getId())).thenReturn(Optional.of(adherenteAjeno));

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(requestDto))
                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(cuentaFinancieraRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void registrarExtraccion_deberiaLanzar404_cuandoElEjecutorNoExiste() {
        UUID inexistente = UUID.randomUUID();
        requestDto.setTipoTransaccion("EXTRACCION");
        requestDto.setClienteEjecutorId(inexistente);

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(requestDto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarDeposito_deberiaAsumirAlTitular_cuandoNoSeIndicaEjecutor() {
        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        when(transaccionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        transaccionService.registrarDeposito(requestDto);

        assertThat(captor.getValue().getEjecutor()).isSameAs(titular);
        verifyNoInteractions(clienteRepository);
    }
}