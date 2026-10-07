package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.config.LimitesExtraccionProperties;
import com.carrillovillalvadaas.tp2.dto.TransaccionRequestDto;
import com.carrillovillalvadaas.tp2.dto.TransaccionResponseDto;
import com.carrillovillalvadaas.tp2.exception.LimiteDiarioExcedidoException;
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
import java.time.LocalDate;
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

    @Mock
    private LimitesExtraccionProperties limitesExtraccion;

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

        lenient().when(limitesExtraccion.limiteParaTipo(any(TipoCliente.class))).thenReturn(100000.0);
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

    /**
     * Tope diario del titular (100.000): 60.000 ya extraídos en el día + 50.000 solicitados
     * superan el tope, por lo que la segunda extracción debe rechazarse.
     */
    @Test
    void registrarExtraccion_deberiaRechazarYGuardarTransaccion_cuandoSuperaElTopeDiarioDelTitular() {
        requestDto.setMonto(BigDecimal.valueOf(50000.0));
        requestDto.setTipoTransaccion("EXTRACCION");

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.sumarMontoPorEjecutorEntre(
                eq(titular.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                any(), any())).thenReturn(60000.0);

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(requestDto))
                .isInstanceOf(LimiteDiarioExcedidoException.class)
                .hasMessageContaining("Tope: 100000.0")
                .hasMessageContaining("acumulado del día: 60000.0");

        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoTransaccion()).isEqualTo(EstadoTransaccion.RECHAZADA);
        assertThat(captor.getValue().getEjecutor()).isSameAs(titular);
        assertThat(captor.getValue().getCuentaFinanciera()).isSameAs(cuenta);

        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1000.0);
        verify(cuentaFinancieraRepository, never()).save(any());
    }

    /**
     * El adherente tiene tope propio de 70.000, independiente del titular:
     * 30.000 acumulados + 45.000 solicitados lo superan, aunque el tope del titular (100.000) lo permitiría.
     */
    @Test
    void registrarExtraccion_deberiaRechazarAlAdherente_cuandoSuperaSuTopePropio() {
        requestDto.setMonto(BigDecimal.valueOf(45000.0));
        requestDto.setTipoTransaccion("EXTRACCION");
        requestDto.setClienteEjecutorId(adherente.getId());

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(adherente.getId())).thenReturn(Optional.of(adherente));
        when(limitesExtraccion.limiteParaTipo(TipoCliente.ADHERENTE)).thenReturn(70000.0);
        when(transaccionRepository.sumarMontoPorEjecutorEntre(
                eq(adherente.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                any(), any())).thenReturn(30000.0);

        assertThatThrownBy(() -> transaccionService.registrarExtraccion(requestDto))
                .isInstanceOf(LimiteDiarioExcedidoException.class)
                .hasMessageContaining("Tope: 70000.0");

        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        assertThat(captor.getValue().getEjecutor()).isSameAs(adherente);
        assertThat(captor.getValue().getEstadoTransaccion()).isEqualTo(EstadoTransaccion.RECHAZADA);
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(1000.0);
        verify(cuentaFinancieraRepository, never()).save(any());
    }

    /**
     * Un adherente con espacio disponible en su propio tope sí puede extraer:
     * 10.000 acumulados + 60.000 solicitados quedan dentro de los 70.000.
     */
    @Test
    void registrarExtraccion_deberiaPermitirAlAdherente_dentroDeSuTopePropio() {
        cuenta.setSaldoOperativo(1000000.0);
        requestDto.setMonto(BigDecimal.valueOf(60000.0));
        requestDto.setTipoTransaccion("EXTRACCION");
        requestDto.setClienteEjecutorId(adherente.getId());

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(adherente.getId())).thenReturn(Optional.of(adherente));
        when(limitesExtraccion.limiteParaTipo(TipoCliente.ADHERENTE)).thenReturn(70000.0);
        when(transaccionRepository.sumarMontoPorEjecutorEntre(
                eq(adherente.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                any(), any())).thenReturn(10000.0);
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        TransaccionResponseDto resultado = transaccionService.registrarExtraccion(requestDto);

        assertThat(resultado.getEstadoTransaccion()).isEqualTo("COMPLETADA");
        assertThat(cuenta.getSaldoOperativo()).isEqualTo(940000.0);
    }
    /**
     * Cuando el acumulado del día más el monto solicitado igualan exactamente el tope,
     * la extracción está permitida (solo se rechaza al superarlo).
     */
    @Test
    void registrarExtraccion_deberiaPermitir_cuandoElAcumuladoMasElMontoIgualanElTope() {
        cuenta.setSaldoOperativo(1000000.0);
        requestDto.setMonto(BigDecimal.valueOf(40000.0));
        requestDto.setTipoTransaccion("EXTRACCION");

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.sumarMontoPorEjecutorEntre(
                eq(titular.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                any(), any())).thenReturn(60000.0);
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        TransaccionResponseDto resultado = transaccionService.registrarExtraccion(requestDto);

        assertThat(resultado.getEstadoTransaccion()).isEqualTo("COMPLETADA");
    }

    /**
     * El acumulado debe calcularse solo con extracciones COMPLETADAS del ejecutor dentro de la jornada actual:
     * las rechazadas no consumen tope y al día siguiente el acumulado vuelve a cero.
     */
    @Test
    void registrarExtraccion_deberiaConsultarSoloExtraccionesCompletadasDelDiaDelEjecutor() {
        requestDto.setMonto(BigDecimal.valueOf(100.0));
        requestDto.setTipoTransaccion("EXTRACCION");

        when(cuentaFinancieraRepository.findByCbu(1234567890123456789L)).thenReturn(Optional.of(cuenta));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<LocalDateTime> desdeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(transaccionRepository.sumarMontoPorEjecutorEntre(
                eq(titular.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                desdeCaptor.capture(), hastaCaptor.capture())).thenReturn(0.0);

        transaccionService.registrarExtraccion(requestDto);

        LocalDate hoy = LocalDate.now();
        assertThat(desdeCaptor.getValue()).isEqualTo(hoy.atStartOfDay());
        assertThat(hastaCaptor.getValue()).isEqualTo(hoy.plusDays(1).atStartOfDay());
        verify(transaccionRepository).sumarMontoPorEjecutorEntre(
                eq(titular.getId()), eq(TipoTransaccion.EXTRACCION), eq(EstadoTransaccion.COMPLETADA),
                any(), any());
    }
}