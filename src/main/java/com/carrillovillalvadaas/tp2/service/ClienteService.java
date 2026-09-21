package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.Cliente;

import java.util.List;
import java.util.UUID;

public interface ClienteService {
    Cliente crearCliente(Cliente cliente);

    Cliente obtenerPorId(UUID id);

    Cliente obtenerPorCuil(long cuil);

    List<Cliente> listarTodos();

    Cliente actualizarCliente(UUID id, Cliente clienteDetalles);

    void eliminarPorId(UUID id);
}
