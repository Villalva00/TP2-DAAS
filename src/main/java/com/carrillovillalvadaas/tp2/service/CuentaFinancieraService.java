package com.carrillovillalvadaas.tp2.service;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;

import java.util.List;
import java.util.UUID;

public interface CuentaFinancieraService {

    CuentaFinanciera crearCuenta(CuentaFinanciera cuenta);

    CuentaFinanciera obtenerPorId(UUID id);

    CuentaFinanciera obtenerPorCbu(long cbu);

    CuentaFinanciera obtenerPorAlias(String alias);

    List<CuentaFinanciera> listarTodas();

    List<CuentaFinanciera> listarPorCliente(UUID clienteId);

    List<CuentaFinanciera> listarPorEstado(EstadoCuenta estado);

    CuentaFinanciera depositar(UUID cuentaId, Double monto);

    CuentaFinanciera extraer(UUID cuentaId, Double monto);

    CuentaFinanciera cambiarEstado(UUID cuentaId, EstadoCuenta nuevoEstado);

    void eliminarPorId(UUID id);
}
