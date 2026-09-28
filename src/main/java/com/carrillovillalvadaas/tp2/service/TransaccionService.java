package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.TransaccionRequestDto;
import com.carrillovillalvadaas.tp2.dto.TransaccionResponseDto;
import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define los contratos de la lógica de negocio para el procesamiento de transacciones.
 * <p>
 * Orquesta operaciones críticas (depósitos y extracciones) asegurando
 * la abstracción del dominio mediante DTOs.
 * </p>
 *
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 * Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public interface TransaccionService {

    /**
     * Registra y procesa un depósito financiero en una cuenta dada.
     *
     * @param requestDto DTO con la información de la cuenta y el monto a depositar.
     * @return Un {@link TransaccionResponseDto} con el comprobante y detalles de la transacción.
     */
    TransaccionResponseDto registrarDeposito(TransaccionRequestDto requestDto);

    /**
     * Registra y procesa una extracción de fondos desde una cuenta.
     *
     * @param requestDto DTO con los datos de la cuenta y el monto a extraer.
     * @return Un {@link TransaccionResponseDto} con el resultado de la operación.
     * @throws com.carrillovillalvadaas.tp2.exception.SaldoInsuficienteException si no cuenta con saldo disponible.
     */
    TransaccionResponseDto registrarExtraccion(TransaccionRequestDto requestDto);

    /**
     * Busca el comprobante o registro de una transacción a partir de su ID único.
     *
     * @param id Identificador numérico de la transacción.
     * @return El {@link TransaccionResponseDto} correspondiente.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si la transacción no existe.
     */
    TransaccionResponseDto obtenerPorId(Long id);

    /**
     * Lista todas las transacciones históricas asociadas a una cuenta específica.
     *
     * @param cuentaId Identificador único de la cuenta financiera.
     * @return Lista de {@link TransaccionResponseDto} de dicha cuenta.
     */
    List<TransaccionResponseDto> listarPorCuenta(UUID cuentaId);

    /**
     * Filtra el historial de transacciones según su tipo operativo.
     *
     * @param tipo Tipo de transacción (ej. DEPOSITO, EXTRACCION).
     * @return Lista de {@link TransaccionResponseDto} filtradas.
     */
    List<TransaccionResponseDto> listarPorTipo(TipoTransaccion tipo);

    /**
     * Filtra las transacciones según su estado de procesamiento.
     *
     * @param estado Estado de la transacción (ej. COMPLETADA, RECHAZADA).
     * @return Lista de {@link TransaccionResponseDto} correspondientes.
     */
    List<TransaccionResponseDto> listarPorEstado(EstadoTransaccion estado);

    /**
     * Retorna la totalidad de las transacciones registradas en el sistema.
     *
     * @return Lista completa de {@link TransaccionResponseDto}.
     */
    List<TransaccionResponseDto> listarTodas();
}