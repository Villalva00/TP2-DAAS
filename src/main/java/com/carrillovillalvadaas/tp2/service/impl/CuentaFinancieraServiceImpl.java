package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.service.CuentaFinancieraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CuentaFinancieraServiceImpl implements CuentaFinancieraService {

    private final CuentaFinancieraRepository cuentaFinancieraRepository;

    @Override
    @Transactional
    public CuentaFinanciera crearCuenta(CuentaFinanciera cuenta) {
        log.info("Creando cuenta financiera con CBU: {}", cuenta.getCbu());

        if (cuenta.getEstado() == null) {
            cuenta.setEstado(EstadoCuenta.ACTIVA);
        }
        if (cuenta.getSaldoOperativo() == null) {
            cuenta.setSaldoOperativo(0.0);
        }

        CuentaFinanciera cuentaGuardada = cuentaFinancieraRepository.save(cuenta);
        log.info("Cuenta financiera creada con ID: {}", cuentaGuardada.getId());
        return cuentaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinanciera obtenerPorId(UUID id) {
        log.debug("Buscando cuenta financiera por ID: {}", id);
        return cuentaFinancieraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta financiera no encontrada con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinanciera obtenerPorCbu(long cbu) {
        log.debug("Buscando cuenta financiera por CBU: {}", cbu);
        return cuentaFinancieraRepository.findByCbu(cbu)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta financiera no encontrada con el CBU: " + cbu));
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinanciera obtenerPorAlias(String alias) {
        log.debug("Buscando cuenta financiera por alias: {}", alias);
        return cuentaFinancieraRepository.findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta financiera no encontrada con el alias: " + alias));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinanciera> listarTodas() {
        log.debug("Listando todas las cuentas financieras");
        return cuentaFinancieraRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinanciera> listarPorCliente(UUID clienteId) {
        log.debug("Listando cuentas financieras del cliente: {}", clienteId);
        return cuentaFinancieraRepository.findByClienteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinanciera> listarPorEstado(EstadoCuenta estado) {
        log.debug("Listando cuentas financieras con estado: {}", estado);
        return cuentaFinancieraRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public CuentaFinanciera depositar(UUID cuentaId, Double monto) {
        log.info("Depositando {} en la cuenta {}", monto, cuentaId);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a cero.");
        }

        CuentaFinanciera cuenta = obtenerPorId(cuentaId);
        cuenta.depositar(monto);
        return cuentaFinancieraRepository.save(cuenta);
    }

    @Override
    @Transactional
    public CuentaFinanciera extraer(UUID cuentaId, Double monto) {
        log.info("Extrayendo {} de la cuenta {}", monto, cuentaId);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a extraer debe ser mayor a cero.");
        }

        CuentaFinanciera cuenta = obtenerPorId(cuentaId);
        Double saldoAntes = cuenta.getSaldoOperativo();

        cuenta.extraer(monto);

        // extraer() no lanza excepción si no hay fondos suficientes: solo no modifica el saldo.
        // Lo detectamos comparando el saldo antes y después para poder informar el error.
        if (saldoAntes.equals(cuenta.getSaldoOperativo())) {
            throw new IllegalStateException("Fondos insuficientes para extraer " + monto + " de la cuenta " + cuentaId);
        }

        return cuentaFinancieraRepository.save(cuenta);
    }

    @Override
    @Transactional
    public CuentaFinanciera cambiarEstado(UUID cuentaId, EstadoCuenta nuevoEstado) {
        log.info("Cambiando estado de la cuenta {} a {}", cuentaId, nuevoEstado);
        CuentaFinanciera cuenta = obtenerPorId(cuentaId);
        cuenta.setEstado(nuevoEstado);
        return cuentaFinancieraRepository.save(cuenta);
    }

    @Override
    @Transactional
    public void eliminarPorId(UUID id) {
        log.info("Eliminando cuenta financiera con ID: {}", id);
        CuentaFinanciera cuenta = obtenerPorId(id);
        cuentaFinancieraRepository.delete(cuenta);
    }
}
