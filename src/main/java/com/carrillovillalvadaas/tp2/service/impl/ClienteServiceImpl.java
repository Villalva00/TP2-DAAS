package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.dto.AdherenteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteResponseDto;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.model.Cliente;
import com.carrillovillalvadaas.tp2.model.TipoCliente;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
import com.carrillovillalvadaas.tp2.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación oficial de la interfaz {@link ClienteService}.
 * <p>
 * Gestiona la lógica de negocio y las reglas de validación para los clientes del banco,
 * asegurando el desacoplamiento mediante el mapeo de Entidades a DTOs y garantizando
 * la atomicidad transaccional con la anotación {@link Transactional}.
 * </p>
 *
 * @author Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Valida que no existan duplicados por CUIL o email antes de realizar la persistencia,
     * transformando el DTO de entrada en Entidad y devolviendo un DTO de salida estructurado.
     * </p>
     */
    @Override
    @Transactional
    public ClienteResponseDto crearCliente(ClienteRequestDto requestDto) {
        log.info("Iniciando proceso de creación de cliente con CUIL: {}", requestDto.getCuil());

        long cuilLong = Long.parseLong(requestDto.getCuil());

        if (clienteRepository.existsByCuilOrEmail(cuilLong, requestDto.getEmail())) {
            log.error("Fallo al crear cliente. Ya existe un registro con CUIL {} o Email {}",
                    cuilLong, requestDto.getEmail());
            throw new IllegalArgumentException("Ya existe un cliente registrado con el mismo CUIL o Email.");
        }

        // Mapeo explícito de RequestDto a la Entidad JPA
        Cliente clienteEntity = Cliente.builder()
                .nombre(requestDto.getNombre())
                .cuil(cuilLong)
                .email(requestDto.getEmail())
                .telefono(requestDto.getTelefono())
                .direccion(requestDto.getDireccion())
                .build();

        Cliente clienteGuardado = clienteRepository.save(clienteEntity);
        log.info("Cliente registrado exitosamente con ID: {}", clienteGuardado.getId());

        return mapearAResponseDto(clienteGuardado);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utiliza {@link Transactional}(readOnly = true) optimizando el rendimiento de lectura en base de datos
     * y lanza {@link RecursoNoEncontradoException} (mapeada a HTTP 404) si el registro no es hallado[cite: 9, 12].
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorId(UUID id) {
        log.debug("Buscando cliente por ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con el ID: " + id)); //[cite: 9, 12]

        return mapearAResponseDto(cliente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorCuil(long cuil) {
        log.debug("Buscando cliente por CUIL: {}", cuil);
        Cliente cliente = clienteRepository.findByCuil(cuil)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con el CUIL: " + cuil)); //[cite: 9, 12]

        return mapearAResponseDto(cliente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDto> listarTodos() {
        log.debug("Listando la totalidad de los clientes registrados");
        return clienteRepository.findAll().stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ClienteResponseDto actualizarCliente(UUID id, ClienteRequestDto requestDto) {
        log.info("Iniciando actualización de datos para el cliente con ID: {}", id);

        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con el ID: " + id)); //[cite: 9, 12]

        // Actualización selectiva de atributos modificables
        clienteExistente.setNombre(requestDto.getNombre());
        clienteExistente.setEmail(requestDto.getEmail());
        clienteExistente.setDireccion(requestDto.getDireccion());
        clienteExistente.setTelefono(requestDto.getTelefono());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        log.info("Cliente con ID {} actualizado correctamente", id);

        return mapearAResponseDto(clienteActualizado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarPorId(UUID id) {
        log.info("Solicitada la eliminación del cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con el ID: " + id)); //[cite: 9, 12]

        clienteRepository.delete(cliente);
        log.info("Cliente con ID {} eliminado correctamente", id);
    }

    @Override
    @Transactional
    public ClienteResponseDto crearAdherente(UUID titularId, AdherenteRequestDto dto){
        log.info("Creando adherente {} para el titular {}", dto.getParentesco(), titularId);

        Cliente titular = clienteRepository.findById(titularId)
                .orElseThrow(()-> new RecursoNoEncontradoException("Titular no Encontrado con el ID: " +titularId));
        //valido que sea titular
        if(titular.getTipoCliente() != TipoCliente.TITULAR){
            throw new IllegalArgumentException("Un adherente no puede tener adherentes.");
        }
        long cuilLong= Long.parseLong(dto.getCuil());
        if(clienteRepository.existsByCuilOrEmail(cuilLong,dto.getEmail())){
            throw new IllegalArgumentException("Ya existe un cliente registrado con el mismo cuil o email.");
        }
        Cliente adherente = Cliente.builder()
                .nombre(dto.getNombre())
                .cuil(cuilLong)
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .tipoCliente(TipoCliente.ADHERENTE)
                .clientePrincipal(titular)
                .parentesco(dto.getParentesco())
                .build();
        // TODO (TP5-B #7): asignar estado inicial PENDIENTE_ACTIVACION y generar TokenActivacion.
        // TODO (TP5-B #8): publicar el evento de dominio asíncrono para enviar el email de activación.
        Cliente guardado = clienteRepository.save(adherente);
        log.info("Adherente registrado con ID: {}", guardado.getId());

        return mapearAResponseDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDto> listarAdherentes(UUID titularId) {
        log.debug("Listando adherentes del titular {}", titularId);

        if (!clienteRepository.existsById(titularId)) {
            throw new RecursoNoEncontradoException("Titular no encontrado con el ID: " + titularId);
        }

        return clienteRepository.findByClientePrincipalId(titularId).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }
    /**
     * Método auxiliar privado para centralizar y encapsular el mapeo
     * de la Entidad de persistencia {@link Cliente} hacia el {@link ClienteResponseDto}.
     *
     * @param cliente Entidad recuperada de la base de datos.
     * @return El DTO de salida listo para ser expuesto por la API.
     */
    private ClienteResponseDto mapearAResponseDto(Cliente cliente) {
        return ClienteResponseDto.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .cuil(String.valueOf(cliente.getCuil()))
                .email(cliente.getEmail())
                .tipoCliente(cliente.getTipoCliente() != null ? cliente.getTipoCliente().name() : null)
                .parentesco(cliente.getParentesco()!=null?cliente.getParentesco().name() :null)
                .build();
    }
}