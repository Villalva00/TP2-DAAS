package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraRequestDto;
import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraResponseDto;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define los contratos de la lógica de negocio para la gestión de cuentas financieras.
 * <p>
 * Aplica los principios de arquitectura limpia operando exclusivamente con objetos DTO,
 * aislando el modelo de persistencia del exterior y garantizando un bajo acoplamiento.
 * </p>
 *
 * @author Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public interface CuentaFinancieraService {

    /**
     * Realiza la apertura de una nueva cuenta financiera asociada a un cliente.
     *
     * @param requestDto DTO con los datos de entrada para la creación de la cuenta (CBU, alias, saldo, etc.)[cite: 1, 10].
     * @return Un {@link CuentaFinancieraResponseDto} con los detalles completos de la cuenta recién abierta.
     */
    CuentaFinancieraResponseDto crearCuenta(CuentaFinancieraRequestDto requestDto);

    /**
     * Busca una cuenta financiera a partir de su identificador único universal (UUID).
     *
     * @param id Identificador único de la cuenta[cite: 3, 10].
     * @return El {@link CuentaFinancieraResponseDto} correspondiente a la cuenta encontrada.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el ID no existe[cite: 3, 6, 13].
     */
    CuentaFinancieraResponseDto obtenerPorId(UUID id);

    /**
     * Busca y retorna los detalles de una cuenta financiera mediante su Clave Bancaria Uniforme (CBU).
     *
     * @param cbu Clave Bancaria Uniforme de la cuenta a buscar[cite: 3, 10].
     * @return El {@link CuentaFinancieraResponseDto} asociado al CBU consultado.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el CBU no se encuentra registrado[cite: 3, 6, 13].
     */
    CuentaFinancieraResponseDto obtenerPorCbu(long cbu);

    /**
     * Busca una cuenta financiera a partir de su alias alfanumérico único.
     *
     * @param alias Alias de la cuenta bancaria[cite: 3, 10].
     * @return El {@link CuentaFinancieraResponseDto} correspondiente.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el alias no existe[cite: 3, 6, 13].
     */
    CuentaFinancieraResponseDto obtenerPorAlias(String alias);

    /**
     * Retorna un listado con todas las cuentas financieras registradas en el sistema.
     *
     * @return Una {@link List} de {@link CuentaFinancieraResponseDto}.
     */
    List<CuentaFinancieraResponseDto> listarTodas();

    /**
     * Lista todas las cuentas financieras pertenecientes a un cliente específico.
     *
     * @param clienteId Identificador único del cliente titular[cite: 3, 10].
     * @return Lista de {@link CuentaFinancieraResponseDto} asociadas al cliente.
     */
    List<CuentaFinancieraResponseDto> listarPorCliente(UUID clienteId);

    /**
     * Filtra y lista las cuentas financieras según su estado operativo actual.
     *
     * @param estado Estado de la cuenta (ej. ACTIVA, SUSPENDIDA, BLOQUEADA)[cite: 1, 3, 10].
     * @return Lista de {@link CuentaFinancieraResponseDto} que coinciden con el estado.
     */
    List<CuentaFinancieraResponseDto> listarPorEstado(EstadoCuenta estado);

    /**
     * Procesa un depósito de fondos en una cuenta financiera específica.
     *
     * @param cuentaId Identificador único de la cuenta receptora[cite: 3, 10].
     * @param monto Cantidad monetaria a depositar (debe ser mayor a cero).
     * @return Un {@link CuentaFinancieraResponseDto} con el saldo actualizado.
     */
    CuentaFinancieraResponseDto depositar(UUID cuentaId, Double monto);

    /**
     * Procesa una extracción de fondos desde una cuenta financiera.
     *
     * @param cuentaId Identificador único de la cuenta de origen[cite: 3, 10].
     * @param monto Cantidad monetaria a extraer.
     * @return Un {@link CuentaFinancieraResponseDto} reflejando el nuevo saldo.
     * @throws com.carrillovillalvadaas.tp2.exception.SaldoInsuficienteException si no hay fondos suficientes[cite: 3, 6, 13].
     */
    CuentaFinancieraResponseDto extraer(UUID cuentaId, Double monto);

    /**
     * Modifica el estado operativo de una cuenta financiera.
     *
     * @param cuentaId Identificador único de la cuenta[cite: 3, 10].
     * @param nuevoEstado Nuevo estado a aplicar[cite: 3, 10].
     * @return El {@link CuentaFinancieraResponseDto} actualizado.
     */
    CuentaFinancieraResponseDto cambiarEstado(UUID cuentaId, EstadoCuenta nuevoEstado);

    /**
     * Elimina una cuenta financiera del sistema mediante su identificador único.
     *
     * @param id Identificador único de la cuenta a eliminar[cite: 3, 10].
     */
    void eliminarPorId(UUID id);
}