package com.carrillovillalvadaas.tp2.service.impl;

import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraRequestDto;
import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraResponseDto;
import com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException;
import com.carrillovillalvadaas.tp2.exception.SaldoInsuficienteException;
import com.carrillovillalvadaas.tp2.model.*;
import com.carrillovillalvadaas.tp2.repository.ClienteRepository;
import com.carrillovillalvadaas.tp2.repository.CuentaFinancieraRepository;
import com.carrillovillalvadaas.tp2.service.CuentaFinancieraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación oficial de la interfaz {@link CuentaFinancieraService}.
 * <p>
 * Encapsula la lógica transaccional de las operaciones bancarias sobre cuentas,
 * implementando validaciones defensivas, control de excepciones de negocio y mapeo a DTOs.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CuentaFinancieraServiceImpl implements CuentaFinancieraService {
    private final CuentaFinancieraRepository cuentaFinancieraRepository;
    private final ClienteRepository clienteRepository;
    @Override
    @Transactional
    public CuentaFinancieraResponseDto crearCuenta(CuentaFinancieraRequestDto requestDto) {
        log.info("Creando cuenta financiera tipo {} con CBU: {}", requestDto.getTipoCuenta(), requestDto.getCbu());

        CuentaFinanciera cuentaEntity;
        if ("CAJA_AHORRO".equalsIgnoreCase(requestDto.getTipoCuenta())) {
            CajaAhorro cajaAhorro = new CajaAhorro();
            // Setear atributos específicos de Caja de Ahorro si vienen en el DTO
            if (requestDto.getTasaInteresAnual() != null) {
                cajaAhorro.setTasaInteresAnual(requestDto.getTasaInteresAnual().doubleValue());
            }
            if (requestDto.getCupoExtraccionesSinCosto() != null) {
                cajaAhorro.setLimiteExtraccion(requestDto.getCupoExtraccionesSinCosto());
            }
            cuentaEntity = cajaAhorro;

        } else if ("CUENTA_CORRIENTE".equalsIgnoreCase(requestDto.getTipoCuenta())) {
            CuentaCorriente cuentaCorriente = new CuentaCorriente();
            // Setear atributos específicos de Cuenta Corriente si vienen en el DTO
            if (requestDto.getMargenDescubierto() != null) {
                cuentaCorriente.setMargenDescubiertoAutorizado(requestDto.getMargenDescubierto().doubleValue());
            }
            if (requestDto.getCostoComisionMantenimiento() != null) {
                cuentaCorriente.setCostoComisionMantenimiento(requestDto.getCostoComisionMantenimiento().doubleValue());
            }
            cuentaEntity = cuentaCorriente;

        } else {
            throw new IllegalArgumentException("Tipo de cuenta inválido: " + requestDto.getTipoCuenta()
                    + ". Debe ser CAJA_AHORRO o CUENTA_CORRIENTE.");
        }
        cuentaEntity.setCbu(Long.parseLong(requestDto.getCbu()));
        cuentaEntity.setAlias(requestDto.getAlias());
        cuentaEntity.setSaldoOperativo(requestDto.getSaldoOperativo().doubleValue());
        cuentaEntity.setEstado(EstadoCuenta.ACTIVA);
        Cliente titular = clienteRepository.findById(requestDto.getClienteId())
                .orElseThrow(()-> new RecursoNoEncontradoException(
                        "Cliente no encontrado con el ID: " + requestDto.getClienteId()));
        cuentaEntity.setCliente(titular);

        CuentaFinanciera cuentaGuardada = cuentaFinancieraRepository.save(cuentaEntity);
        log.info("Cuenta financiera creada con ID: {}", cuentaGuardada.getId());

        return mapearAResponseDto(cuentaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinancieraResponseDto obtenerPorId(UUID id) {
        log.debug("Buscando cuenta financiera por ID: {}", id);
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta financiera no encontrada con el ID: " + id));
        return mapearAResponseDto(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinancieraResponseDto obtenerPorCbu(long cbu) {
        log.debug("Buscando cuenta financiera por CBU: {}", cbu);
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findByCbu(cbu)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta financiera no encontrada con el CBU: " + cbu));
        return mapearAResponseDto(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaFinancieraResponseDto obtenerPorAlias(String alias) {
        log.debug("Buscando cuenta financiera por alias: {}", alias);
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findByAlias(alias)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta financiera no encontrada con el alias: " + alias));
        return mapearAResponseDto(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinancieraResponseDto> listarTodas() {
        log.debug("Listando todas las cuentas financieras");
        return cuentaFinancieraRepository.findAll().stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinancieraResponseDto> listarPorCliente(UUID clienteId) {
        log.debug("Listando cuentas financieras del cliente: {}", clienteId);
        return cuentaFinancieraRepository.findByClienteId(clienteId).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaFinancieraResponseDto> listarPorEstado(EstadoCuenta estado) {
        log.debug("Listando cuentas financieras con estado: {}", estado);
        return cuentaFinancieraRepository.findByEstado(estado).stream()
                .map(this::mapearAResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CuentaFinancieraResponseDto depositar(UUID cuentaId, Double monto) {
        log.info("Depositando {} en la cuenta {}", monto, cuentaId);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a cero.");
        }

        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con ID: " + cuentaId));

        cuenta.depositar(monto);
        CuentaFinanciera actualizada = cuentaFinancieraRepository.save(cuenta);
        return mapearAResponseDto(actualizada);
    }

    @Override
    @Transactional
    public CuentaFinancieraResponseDto extraer(UUID cuentaId, Double monto) {
        log.info("Extrayendo {} de la cuenta {}", monto, cuentaId);
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a extraer debe ser mayor a cero.");
        }

        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con ID: " + cuentaId));

        Double saldoAntes = cuenta.getSaldoOperativo();
        cuenta.extraer(monto);

        if (saldoAntes.equals(cuenta.getSaldoOperativo())) {
            log.warn("Fondos insuficientes en la cuenta {} para extraer {}", cuentaId, monto);
            // Reemplazado por nuestra excepción personalizada de negocio
            throw new SaldoInsuficienteException("Fondos insuficientes para extraer " + monto + " de la cuenta.");
        }

        CuentaFinanciera actualizada = cuentaFinancieraRepository.save(cuenta);
        return mapearAResponseDto(actualizada);
    }

    @Override
    @Transactional
    public CuentaFinancieraResponseDto cambiarEstado(UUID cuentaId, EstadoCuenta nuevoEstado) {
        log.info("Cambiando estado de la cuenta {} a {}", cuentaId, nuevoEstado);
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con ID: " + cuentaId));

        cuenta.setEstado(nuevoEstado);
        CuentaFinanciera actualizada = cuentaFinancieraRepository.save(cuenta);
        return mapearAResponseDto(actualizada);
    }

    @Override
    @Transactional
    public void eliminarPorId(UUID id) {
        log.info("Eliminando cuenta financiera con ID: {}", id);
        CuentaFinanciera cuenta = cuentaFinancieraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con ID: " + id));
        cuentaFinancieraRepository.delete(cuenta);
    }

    /**
     * Método auxiliar privado para mapear la entidad {@link CuentaFinanciera} a un {@link CuentaFinancieraResponseDto}.
     */
    private CuentaFinancieraResponseDto mapearAResponseDto(CuentaFinanciera cuenta) {
        return CuentaFinancieraResponseDto.builder()
                .cbu(String.valueOf(cuenta.getCbu()))
                .alias(cuenta.getAlias())
                .saldoOperativo(BigDecimal.valueOf(cuenta.getSaldoOperativo()))
                .estado(cuenta.getEstado() != null ? cuenta.getEstado().name() : null)
                .build();
    }
}