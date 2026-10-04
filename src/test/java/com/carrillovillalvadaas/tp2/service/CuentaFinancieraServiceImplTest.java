package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraRequestDto;
import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraResponseDto;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.exception.SaldoInsuficienteException;
import com.carrillovillalvadaas.tp2.model.CajaAhorro;
import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.model.Cliente;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
import com.carrillovillalvadaas.tp2.service.impl.CuentaFinancieraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la implementación de servicios de cuentas financieras ({@link CuentaFinancieraServiceImpl}).
 * <p>
 * Valida de forma aislada la correcta manipulación de saldos, reglas de negocio para depósitos y extracciones,
 * el mapeo adecuado hacia DTOs de respuesta y el correcto lanzamiento de excepciones personalizadas,
 * simulando el comportamiento del repositorio {@link CuentaFinancieraRepository} mediante Mockito.
 * </p>
 *
 * @author Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@ExtendWith(MockitoExtension.class)
class CuentaFinancieraServiceImplTest {

    @Mock
    private CuentaFinancieraRepository cuentaFinancieraRepository;
    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CuentaFinancieraServiceImpl cuentaFinancieraService;

    private CajaAhorro cuentaEntity;
    private UUID cuentaId;
    private CuentaFinancieraRequestDto requestDto;

    private UUID clienteId;
    /**
     * Configuración inicial previa a la ejecución de cada prueba unitaria.
     * Inicializa una entidad de tipo {@link CajaAhorro} de prueba y un {@link CuentaFinancieraRequestDto}.
     */
    @BeforeEach
    void setUp() {
        cuentaId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        cuentaEntity = new CajaAhorro();
        cuentaEntity.setId(cuentaId);
        cuentaEntity.setCbu(1234567890123456789L);
        cuentaEntity.setAlias("mi.cuenta.test");
        cuentaEntity.setSaldoOperativo(1000.0);
        cuentaEntity.setEstado(EstadoCuenta.ACTIVA);
        cuentaEntity.setTasaInteresAnual(50.0);
        cuentaEntity.setLimiteExtraccion(5);

        requestDto = new CuentaFinancieraRequestDto();
        requestDto.setCbu("1234567890123456789");
        requestDto.setAlias("mi.cuenta.test");
        requestDto.setSaldoOperativo(BigDecimal.valueOf(1000.0));
        requestDto.setTipoCuenta("CAJA_AHORRO");
        requestDto.setTasaInteresAnual(50.0);
        requestDto.setClienteId(clienteId);
    }

    /**
     * Verifica que al crear una cuenta de manera exitosa se persista correctamente
     * y se retorne un {@link CuentaFinancieraResponseDto} con los datos esperados.
     */
    @Test
    void crearCuenta_deberiaGuardarYRetornarResponseDto() {
        Cliente cliente = Cliente.builder().id(clienteId).build();
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class)))
                .thenReturn(cuentaEntity);
        CuentaFinancieraResponseDto resultado = cuentaFinancieraService.crearCuenta(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCbu()).isEqualTo("1234567890123456789");
        assertThat(resultado.getEstado()).isEqualTo("ACTIVA");
        verify(cuentaFinancieraRepository).save(any(CuentaFinanciera.class));
    }

    /**
     * Verifica que se lance la excepción {@link RecursoNoEncontradoException}
     * al intentar buscar o manipular una cuenta utilizando un identificador (UUID) inexistente.
     */
    @Test
    void obtenerPorId_deberiaLanzarExcepcionRecursoNoEncontrado_cuandoNoExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(cuentaFinancieraRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaFinancieraService.obtenerPorId(idInexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Cuenta financiera no encontrada");
    }

    /**
     * Verifica que un depósito válido incremente correctamente el saldo operativo
     * de la cuenta y devuelva el DTO actualizado.
     */
    @Test
    void depositar_deberiaIncrementarSaldo_cuandoMontoEsValido() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        CuentaFinancieraResponseDto resultado = cuentaFinancieraService.depositar(cuentaId, 500.0);

        assertThat(resultado.getSaldoOperativo()).isEqualByComparingTo(BigDecimal.valueOf(1500.0));
    }

    /**
     * Verifica que se lance una {@link IllegalArgumentException} si se intenta depositar un monto nulo o menor/igual a cero.
     */
    @Test
    void depositar_deberiaLanzarExcepcion_cuandoMontoEsInvalido() {
        assertThatThrownBy(() -> cuentaFinancieraService.depositar(cuentaId, -100.0))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(cuentaFinancieraRepository);
    }

    /**
     * Verifica que una extracción válida decremente el saldo operativo de la cuenta de forma exitosa.
     */
    @Test
    void extraer_deberiaDecrementarSaldo_cuandoHayFondosSuficientes() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));
        when(cuentaFinancieraRepository.save(any(CuentaFinanciera.class))).thenAnswer(inv -> inv.getArgument(0));

        CuentaFinancieraResponseDto resultado = cuentaFinancieraService.extraer(cuentaId, 300.0);

        assertThat(resultado.getSaldoOperativo()).isEqualByComparingTo(BigDecimal.valueOf(700.0));
    }

    /**
     * Verifica que se lance la excepción personalizada {@link SaldoInsuficienteException}
     * cuando se intenta extraer un monto que excede la disponibilidad de fondos.
     */
    @Test
    void extraer_deberiaLanzarSaldoInsuficienteException_cuandoNoHayFondos() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));

        assertThatThrownBy(() -> cuentaFinancieraService.extraer(cuentaId, 5000.0))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessageContaining("Fondos insuficientes");

        verify(cuentaFinancieraRepository, never()).save(any());
    }

    /**
     * Verifica que la actualización del estado de una cuenta opere correctamente
     * y capture el cambio mediante un captor de argumentos.
     */
    @Test
    void cambiarEstado_deberiaActualizarElEstadoDeLaCuenta() {
        when(cuentaFinancieraRepository.findById(cuentaId)).thenReturn(Optional.of(cuentaEntity));
        ArgumentCaptor<CuentaFinanciera> captor = ArgumentCaptor.forClass(CuentaFinanciera.class);
        when(cuentaFinancieraRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        cuentaFinancieraService.cambiarEstado(cuentaId, EstadoCuenta.SUSPENDIDA);

        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoCuenta.SUSPENDIDA);
    }

    /**
     * Verifica que el listado de cuentas por ID de cliente delegue en el repositorio
     * y devuelva una lista mapeada de {@link CuentaFinancieraResponseDto}.
     */
    @Test
    void listarPorCliente_deberiaRetornarListaDeResponseDtos() {

        when(cuentaFinancieraRepository.findByClienteId(clienteId)).thenReturn(List.of(cuentaEntity));

        List<CuentaFinancieraResponseDto> resultado = cuentaFinancieraService.listarPorCliente(clienteId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCbu()).isEqualTo("1234567890123456789");
    }
    @Test
    void crearCuenta_deberiaLanzarRecursoNoEncontrado_cuandoClienteNoExiste() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaFinancieraService.crearCuenta(requestDto))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Cliente no encontrado");

        verify(cuentaFinancieraRepository, never()).save(any());
    }
}