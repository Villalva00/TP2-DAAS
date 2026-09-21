package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;

import java.util.List;
import java.util.UUID;

public interface TransaccionService {

    Transaccion registrarDeposito(UUID cuentaId, Double monto);

    Transaccion registrarExtraccion(UUID cuentaId, Double monto);

    Transaccion obtenerPorId(Long id);

    List<Transaccion> listarPorCuenta(UUID cuentaId);

    List<Transaccion> listarPorTipo(TipoTransaccion tipo);

    List<Transaccion> listarPorEstado(EstadoTransaccion estado);

    List<Transaccion> listarTodas();
}
