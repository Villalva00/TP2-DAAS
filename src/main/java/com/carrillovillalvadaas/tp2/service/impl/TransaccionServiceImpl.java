package com.carrillovillalvadaas.tp2.service.impl;

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
import com.carrillovillalvadaas.tp2.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación oficial de la interfaz {@link TransaccionService}.
 * <p>
 * Encapsula la lógica de negocio para el procesamiento de transacciones financieras
 * (depósitos y extracciones), garantizando la atomicidad mediante {@link Transactional}
 * y el manejo robusto de excepciones personalizadas para recursos no encontrados o saldo insuficiente.
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 * Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaFinancieraRepository cuentaFinancieraRepository;
    private final ClienteRepository clienteRepository;
    private final LimitesExtraccionProperties limitesExtraccion;
    /**
     * {@inheritDoc}
     * <p>
     * Busca la cuenta por su CBU, incrementa el saldo operativo de forma atómica
     * y registra la transacción con estado COMPLETADA.
     * </p>
     */
    @Override
    @Transactional
    public TransaccionResponseDto registrarDeposito(TransaccionRequestDto requestDto) {
        log.info("Registrando depósito de {} en cuenta CBU: {}", requestDto.getMonto(), requestDto.getCbuCuenta());

        CuentaFinanciera cuenta = cuentaFinancieraRepository.findByCbu(Long.parseLong(requestDto.getCbuCuenta()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con CBU: " + requestDto.getCbuCuenta()));
        //agrego la logica del ejecutor
        Cliente ejecutor =  resolverEjecutor(cuenta, requestDto.getClienteEjecutorId());
        if (ejecutor.getTipoCliente() == TipoCliente.ADHERENTE) {
            throw new OperacionNoPermitidaException("Los adherentes solo pueden realizar extracciones");
        }

        cuenta.depositar(requestDto.getMonto().doubleValue());
        cuentaFinancieraRepository.save(cuenta);

        Transaccion transaccion = construirTransaccion(cuenta, requestDto.getMonto().doubleValue(), TipoTransaccion.DEPOSITO, EstadoTransaccion.COMPLETADA, ejecutor);
        Transaccion guardada = transaccionRepository.save(transaccion);

        return mapearAResponseDto(guardada);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca la cuenta por su CBU, valida el tope diario acumulado de extracciones del ejecutor
     * y luego intenta extraer el monto solicitado. Si supera el tope diario, registra la
     * transacción como RECHAZADA y lanza {@link LimiteDiarioExcedidoException}.
     * Si no hay fondos suficientes, registra la transacción como RECHAZADA y lanza {@link SaldoInsuficienteException}.
     * </p>
     */
    @Override
    @Transactional(noRollbackFor = LimiteDiarioExcedidoException.class)
    public TransaccionResponseDto registrarExtraccion(TransaccionRequestDto requestDto) {
        log.info("Registrando extracción de {} en cuenta CBU: {}", requestDto.getMonto(), requestDto.getCbuCuenta());

        CuentaFinanciera cuenta = cuentaFinancieraRepository.findByCbu(Long.parseLong(requestDto.getCbuCuenta()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con CBU: " + requestDto.getCbuCuenta()));

        Cliente ejecutor = resolverEjecutor(cuenta, requestDto.getClienteEjecutorId());

        validarTopeDiario(cuenta, ejecutor, requestDto.getMonto());

        Double saldoAntes = cuenta.getSaldoOperativo();
        cuenta.extraer(requestDto.getMonto().doubleValue());
        boolean extraccionExitosa = !saldoAntes.equals(cuenta.getSaldoOperativo());

        EstadoTransaccion estado;
        if (extraccionExitosa) {
            cuentaFinancieraRepository.save(cuenta);
            estado = EstadoTransaccion.COMPLETADA;
        } else {
            log.warn("Fondos insuficientes en cuenta CBU {} para extraer {}", requestDto.getCbuCuenta(), requestDto.getMonto());
            estado = EstadoTransaccion.RECHAZADA;
        }

        Transaccion transaccion = construirTransaccion(cuenta, requestDto.getMonto().doubleValue(), TipoTransaccion.EXTRACCION, estado,ejecutor);
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        if (!extraccionExitosa) {
            throw new SaldoInsuficienteException("Fondos insuficientes para realizar la extracción por un monto de " + requestDto.getMonto());
        }

        return mapearAResponseDto(transaccionGuardada);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utiliza transacciones de solo lectura para optimizar el rendimiento al buscar por ID.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public TransaccionResponseDto obtenerPorId(Long id) {
        log.debug("Buscando transacción por ID: {}", id);
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transacción no encontrada con el ID: " + id));
        return mapearAResponseDto(transaccion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponseDto> listarPorCuenta(UUID cuentaId) {
        log.debug("Listando transacciones de la cuenta ID: {}", cuentaId);
        return transaccionRepository.findByCuentaFinancieraId(cuentaId).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponseDto> listarPorTipo(TipoTransaccion tipo) {
        log.debug("Listando transacciones de tipo: {}", tipo);
        return transaccionRepository.findByTipo(tipo).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponseDto> listarPorEstado(EstadoTransaccion estado) {
        log.debug("Listando transacciones con estado: {}", estado);
        return transaccionRepository.findByEstadoTransaccion(estado).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponseDto> listarTodas() {
        log.debug("Listando la totalidad de las transacciones");
        return transaccionRepository.findAll().stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar privado para construir y poblar una entidad de tipo {@link Transaccion}.
     *
     * @param cuenta Cuenta financiera asociada a la operación.
     * @param monto Monto monetario de la transacción.
     * @param tipo Tipo de transacción (DEPÓSITO o EXTRACCIÓN).
     * @param estado Estado de resolución de la transacción.
     * @return Una nueva instancia de {@link Transaccion} construida mediante el patrón Builder.
     */
    private Transaccion construirTransaccion(CuentaFinanciera cuenta, Double monto, TipoTransaccion tipo, EstadoTransaccion estado, Cliente ejecutor) {
        return Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(tipo)
                .estadoTransaccion(estado)
                .cuentaFinanciera(cuenta)
                .ejecutor(ejecutor)
                .build();
    }

    /**
     * Método auxiliar privado para mapear la entidad {@link Transaccion} hacia un {@link TransaccionResponseDto}.
     *
     * @param transaccion Entidad persistida en la base de datos.
     * @return El DTO de respuesta estructurado para la capa web.
     */
    private TransaccionResponseDto mapearAResponseDto(Transaccion transaccion) {
        return TransaccionResponseDto.builder()
                .idTransaccion(transaccion.getId())
                .cbuCuenta(transaccion.getCuentaFinanciera() != null ? String.valueOf(transaccion.getCuentaFinanciera().getCbu()) : null)
                .monto(BigDecimal.valueOf(transaccion.getMonto()))
                .tipoTransaccion(transaccion.getTipo() != null ? transaccion.getTipo().name() : null)
                .estadoTransaccion(transaccion.getEstadoTransaccion() != null ? transaccion.getEstadoTransaccion().name() : null)
                .fechaHora(transaccion.getFechaHora())
                .mensaje("Operación procesada correctamente.")
                .build();
    }
    /**
     * Valida que la extracción solicitada no haga superar al ejecutor su tope diario de extracción.
     * <p>
     * Cada cliente tiene su propio acumulado: el titular y cada adherente no comparten bolsa.
     * Solo se contabilizan las extracciones COMPLETADAS del día en curso, por lo que las
     * transacciones rechazadas no consumen el tope y al día siguiente el acumulado vuelve a cero.
     * </p>
     *
     * @param cuenta   Cuenta sobre la que se opera.
     * @param ejecutor Cliente que realiza la extracción (titular o adherente).
     * @param monto    Monto solicitado a extraer.
     * @throws LimiteDiarioExcedidoException si acumulado + monto supera el tope del ejecutor.
     */
    private void validarTopeDiario(CuentaFinanciera cuenta, Cliente ejecutor, BigDecimal monto) {
        Double tope = limitesExtraccion.limiteParaTipo(ejecutor.getTipoCliente());

        LocalDate hoy = LocalDate.now();
        Double acumuladoConsulta = transaccionRepository.sumarMontoPorEjecutorEntre(
                ejecutor.getId(),
                TipoTransaccion.EXTRACCION,
                EstadoTransaccion.COMPLETADA,
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay());
        double acumulado = acumuladoConsulta != null ? acumuladoConsulta : 0.0;

        if (acumulado + monto.doubleValue() > tope) {
            log.warn("Tope diario de extracción excedido para cliente {}: acumulado {}, monto {}, tope {}",
                    ejecutor.getId(), acumulado, monto, tope);
            Transaccion rechazada = construirTransaccion(
                    cuenta, monto.doubleValue(), TipoTransaccion.EXTRACCION, EstadoTransaccion.RECHAZADA, ejecutor);
            transaccionRepository.save(rechazada);
            throw new LimiteDiarioExcedidoException(
                    "Límite diario de extracción excedido. Tope: " + tope + ", acumulado del día: " + acumulado,
                    tope, acumulado);
        }
    }

    /**
     * Determina quién ejecuta la operación y valida que pueda operar sobre la cuenta.
     *
     * @param cuenta     Cuenta sobre la que se opera.
     * @param ejecutorId Id del ejecutor (null = titular de la cuenta).
     * @return El cliente ejecutor.
     * @throws RecursoNoEncontradoException si el ejecutor no existe.
     * @throws OperacionNoPermitidaException si no es el titular de la cuenta ni un adherente suyo.
     */
    private Cliente resolverEjecutor(CuentaFinanciera cuenta, UUID ejecutorId) {
        Cliente titularCuenta = cuenta.getCliente();

        if (ejecutorId == null) {
            return titularCuenta;
        }

        Cliente ejecutor = clienteRepository.findById(ejecutorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente ejecutor no encontrado con el ID: " + ejecutorId));

        boolean esTitularDeLaCuenta = ejecutor.getId().equals(titularCuenta.getId());
        boolean esAdherenteDelTitular = ejecutor.getClientePrincipal() != null
                && ejecutor.getClientePrincipal().getId().equals(titularCuenta.getId());

        if (!esTitularDeLaCuenta && !esAdherenteDelTitular) {
            throw new OperacionNoPermitidaException("El cliente no está autorizado a operar sobre esta cuenta");
        }

        return ejecutor;
    }
}