package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.model.Cliente;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
import com.carrillovillalvadaas.tp2.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        log.info("Iniciando proceso de creación de cliente con CUIL: {}", cliente.getCuil());

        if (clienteRepository.existsByCuilOrEmail(cliente.getCuil(), cliente.getEmail())) {
            log.error("Fallo al crear cliente. Ya existe un registro con CUIL {} o Email {}",
                    cliente.getCuil(), cliente.getEmail());
            throw new IllegalArgumentException("Ya existe un cliente registrado con el mismo CUIL o Email.");
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);

        log.info("Cliente registrado exitosamente con ID: {}", clienteGuardado.getId());
        return clienteGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(UUID id) {
        log.debug("Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorCuil(long cuil) {
        log.debug("Buscando cliente por CUIL: {}", cuil);
        return clienteRepository.findByCuil(cuil)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con el CUIL: " + cuil));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        log.debug("Listando la totalidad de los clientes registrados");
        return clienteRepository.findAll();
    }

    @Override
    @Transactional
    public Cliente actualizarCliente(UUID id, Cliente clienteDetalles) {
        log.info("Iniciando actualización de datos para el cliente con ID: {}", id);

        Cliente clienteExistente = obtenerPorId(id);

        // Actualización selectiva de campos modificables del dominio
        clienteExistente.setNombre(clienteDetalles.getNombre());
        clienteExistente.setEmail(clienteDetalles.getEmail());
        clienteExistente.setDireccion(clienteDetalles.getDireccion());
        clienteExistente.setTelefono(clienteDetalles.getTelefono());

        return clienteRepository.save(clienteExistente);
    }

    @Override
    @Transactional
    public void eliminarPorId(UUID id) {
        log.info("Solicitada la eliminación del cliente con ID: {}", id);
        Cliente cliente = obtenerPorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente con ID {} eliminado correctamente", id);
    }
}
