package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.dto.AdherenteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define los contratos de la lógica de negocio para la gestión de clientes.
 * <p>
 * Sigue estrictamente el principio de bajo acoplamiento al operar exclusivamente
 * con Objetos de Transferencia de Datos (DTOs), aislando la capa de persistencia
 * y las entidades del modelo de dominio de la capa de control web (Controllers).
 * </p>
 *
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 * Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
public interface ClienteService {

    /**
     * Registra y persiste un nuevo cliente en el sistema a partir de los datos validados.
     *
     * @param requestDto Objeto de entrada con los datos del nuevo cliente (nombre, CUIL, email, etc.).
     * @return Un {@link ClienteResponseDto} conteniendo los datos completos del cliente creado y su ID de sistema.
     * @throws IllegalArgumentException si ya se encuentra registrado un cliente con el mismo CUIL o correo electrónico
     */
    ClienteResponseDto crearCliente(ClienteRequestDto requestDto);

    /**
     * Busca y retorna la información detallada de un cliente específico basándose en su identificador único universal (UUID).
     *
     * @param id Identificador único (UUID) del cliente a buscar.
     * @return Un {@link ClienteResponseDto} con la información del cliente encontrado.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el cliente no existe en la base de datos.
     */
    ClienteResponseDto obtenerPorId(UUID id);

    /**
     * Busca y retorna los datos de un cliente utilizando su número de CUIL como clave de búsqueda única.
     *
     * @param cuil Clave Única de Identificación Laboral/Tributaria del cliente[cite: 9].
     * @return Un {@link ClienteResponseDto} con los detalles del cliente localizado.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si no existe un cliente con dicho CUIL
     */
    ClienteResponseDto obtenerPorCuil(long cuil);

    /**
     * Retorna un listado completo con la totalidad de los clientes registrados en el sistema financiero.
     *
     * @return Una {@link List} de {@link ClienteResponseDto} con todos los registros activos.
     */
    List<ClienteResponseDto> listarTodos();

    /**
     * Actualiza los datos modificables de un cliente existente identificado por su UUID.
     *
     * @param id Identificador único del cliente cuyos datos serán actualizados.
     * @param requestDto DTO con los nuevos valores a aplicar (nombre, teléfono, dirección, etc.).
     * @return Un {@link ClienteResponseDto} reflejando el estado actualizado del cliente.
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el ID provisto no pertenece a ningún cliente.
     */
    ClienteResponseDto actualizarCliente(UUID id, ClienteRequestDto requestDto);

    /**
     * Elimina de forma lógica o física un registro de cliente del sistema según su identificador único.
     *
     * @param id Identificador único (UUID) del cliente a eliminar
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el cliente a eliminar no es encontrado.
     */
    void eliminarPorId(UUID id);


    /**
     * Confirma la cuenta de un cliente canjeando su token de activación.
     * <p>
     * Valida que el token tenga formato UUID, exista, no haya sido usado y no esté
     * vencido. Si es válido, el cliente pasa a estado {@code ACTIVO} y el token a
     * {@code usado = true}.
     * </p>
     *
     * @param token Valor del token de activación (tal como llegó por el query param).
     * @return El {@link ClienteResponseDto} del cliente activado.
     * @throws com.carrillovillalvadaas.tp2.exception.TokenInvalidoException si el
     *         formato es inválido, el token no existe, ya fue usado o está vencido.
     */
    ClienteResponseDto activarCliente(String token);


    /**
            * Registra un adherente (cónyuge o hijo) vinculado a un titular.
            *
            * @param titularId Identificador del cliente titular.
            * @param dto Datos del adherente.
            * @return El adherente creado.
            * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el titular no existe.
            * @throws IllegalArgumentException si el cliente indicado es un adherente, o si ya existe un cliente con el mismo CUIL o email.
 */
    ClienteResponseDto crearAdherente(UUID titularId, AdherenteRequestDto dto);

    /**
     * Lista los adherentes vinculados a un titular.
     *
     * @param titularId Identificador del titular.
     * @return Lista de adherentes (vacía si no tiene).
     * @throws com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException si el titular no existe.
     */
    List<ClienteResponseDto> listarAdherentes(UUID titularId);
}