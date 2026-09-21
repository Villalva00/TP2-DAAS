package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.model.*;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.repository.TransaccionRepository;
import com.carrillovillalvadaas.tp2.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaFinancieraRepository cuentaFinancieraRepository;

    @Override
    @Transactional
    public Transaccion registrarDeposito(UUID cuentaId, Double monto) {
        log.info("Registrando depósito de {} en la cuenta {}", monto, cuentaId);

        CuentaFinanciera cuenta = obtenerCuenta(cuentaId);
        validarMonto(monto);

        cuenta.depositar(monto);
        cuentaFinancieraRepository.save(cuenta);

        Transaccion transaccion = construirTransaccion(cuenta, monto, TipoTransaccion.DEPOSITO, EstadoTransaccion.COMPLETADA);
        return transaccionRepository.save(transaccion);
    }

    @Override
    @Transactional
    public Transaccion registrarExtraccion(UUID cuentaId, Double monto) {
        log.info("Registrando extracción de {} en la cuenta {}", monto, cuentaId);

        CuentaFinanciera cuenta = obtenerCuenta(cuentaId);
        validarMonto(monto);

        Double saldoAntes = cuenta.getSaldoOperativo();
        cuenta.extraer(monto);
        boolean extraccionExitosa = !saldoAntes.equals(cuenta.getSaldoOperativo());

        EstadoTransaccion estado;
        if (extraccionExitosa) {
            cuentaFinancieraRepository.save(cuenta);
            estado = EstadoTransaccion.COMPLETADA;
        } else {
            log.warn("Fondos insuficientes en la cuenta {} para extraer {}", cuentaId, monto);
            estado = EstadoTransaccion.RECHAZADA;
        }

        Transaccion transaccion = construirTransaccion(cuenta, monto, TipoTransaccion.EXTRACCION, estado);
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        if (!extraccionExitosa) {
            throw new IllegalStateException("Fondos insuficientes para extraer " + monto + " de la cuenta " + cuentaId);
        }
        return transaccionGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Transaccion obtenerPorId(Long id) {
        log.debug("Buscando transacción por ID: {}", id);
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con el ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarPorCuenta(UUID cuentaId) {
        log.debug("Listando transacciones de la cuenta: {}", cuentaId);
        return transaccionRepository.findByCuentaFinancieraId(cuentaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarPorTipo(TipoTransaccion tipo) {
        log.debug("Listando transacciones de tipo: {}", tipo);
        return transaccionRepository.findByTipo(tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarPorEstado(EstadoTransaccion estado) {
        log.debug("Listando transacciones con estado: {}", estado);
        return transaccionRepository.findByEstadoTransaccion(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarTodas() {
        log.debug("Listando la totalidad de las transacciones");
        return transaccionRepository.findAll();
    }

    // ---- métodos privados de apoyo ----

    private CuentaFinanciera obtenerCuenta(UUID cuentaId) {
        return cuentaFinancieraRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta financiera no encontrada con el ID: " + cuentaId));
    }

    private void validarMonto(Double monto) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero.");
        }
    }

    private Transaccion construirTransaccion(CuentaFinanciera cuenta, Double monto, TipoTransaccion tipo, EstadoTransaccion estado) {
        return Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(tipo)
                .estadoTransaccion(estado)
                .cuentaFinanciera(cuenta)
                .build();
    }
}
