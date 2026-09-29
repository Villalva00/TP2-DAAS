package com.carrillovillalvadaas.tp2.controller;

import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraRequestDto;
import com.carrillovillalvadaas.tp2.dto.CuentaFinancieraResponseDto;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import com.carrillovillalvadaas.tp2.service.CuentaFinancieraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST especializado en la gestión de cuentas financieras
 * ({@link com.carrillovillalvadaas.tp2.model.CuentaFinanciera}, incluyendo cajas de ahorro y cuentas corrientes).
 * <p>
 * Expone los endpoints necesarios para el alta de cuentas mediante DTOs, consultas avanzadas por
 * ID, CBU o alias, filtrados por cliente o estado, y la ejecución de operaciones transaccionales
 * de depósitos y extracciones.
 * </p>
 *
 * @author  Villalva Elias Maciel, Carrillo Gonzalo Alejo
 *            Desarrollo y Arquitecturas Avanzadas de Software (UNJu)

 */
@RestController
@RequestMapping("/api/v1/cuentas")
@Slf4j
@RequiredArgsConstructor
public class CuentaFinancieraController {

    private final CuentaFinancieraService cuentaFinancieraService;

    /**
     * Crea y registra una nueva cuenta financiera en el sistema.
     * <p>
     * Evalúa dinámicamente el tipo de cuenta recibido en el {@link CuentaFinancieraRequestDto}
     * para instanciar la entidad concreta correspondiente (Caja de Ahorro o Cuenta Corriente).
     * </p>
     *
     * @param requestDto DTO con la información validada para la apertura de la cuenta.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} creado y el código HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<CuentaFinancieraResponseDto> crearCuenta(@Valid @RequestBody CuentaFinancieraRequestDto requestDto) {
        log.info("Recibida solicitud HTTP POST en /api/v1/cuentas para crear cuenta con CBU: {}", requestDto.getCbu());
        CuentaFinancieraResponseDto cuentaCreada = cuentaFinancieraService.crearCuenta(requestDto);
        log.info("Cuenta financiera creada exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaCreada);
    }

    /**
     * Busca y retorna una cuenta financiera a partir de su identificador único (UUID).
     *
     * @param id Identificador único de la cuenta.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} y código HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CuentaFinancieraResponseDto> obtenerPorId(@PathVariable UUID id) {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas/{} para buscar cuenta por ID", id);
        CuentaFinancieraResponseDto cuenta = cuentaFinancieraService.obtenerPorId(id);
        return ResponseEntity.ok(cuenta);
    }

    /**
     * Busca y retorna una cuenta financiera según su CBU numérico.
     *
     * @param cbu CBU de la cuenta a buscar.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} y código HTTP 200 (OK).
     */
    @GetMapping("/cbu/{cbu}")
    public ResponseEntity<CuentaFinancieraResponseDto> obtenerPorCbu(@PathVariable long cbu) {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas/cbu/{} para buscar cuenta", cbu);
        CuentaFinancieraResponseDto cuenta = cuentaFinancieraService.obtenerPorCbu(cbu);
        return ResponseEntity.ok(cuenta);
    }

    /**
     * Busca y retorna una cuenta financiera según su alias alfanumérico.
     *
     * @param alias Alias de la cuenta.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} y código HTTP 200 (OK).
     */
    @GetMapping("/alias/{alias}")
    public ResponseEntity<CuentaFinancieraResponseDto> obtenerPorAlias(@PathVariable String alias) {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas/alias/{} para buscar cuenta", alias);
        CuentaFinancieraResponseDto cuenta = cuentaFinancieraService.obtenerPorAlias(alias);
        return ResponseEntity.ok(cuenta);
    }

    /**
     * Lista la totalidad de las cuentas financieras registradas en el sistema.
     *
     * @return Un {@link ResponseEntity} con la lista de {@link CuentaFinancieraResponseDto} y código HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<CuentaFinancieraResponseDto>> listarTodas() {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas para listar todas las cuentas financieras");
        List<CuentaFinancieraResponseDto> cuentas = cuentaFinancieraService.listarTodas();
        return ResponseEntity.ok(cuentas);
    }

    /**
     * Lista todas las cuentas financieras asociadas a un cliente específico.
     *
     * @param clienteId Identificador único del cliente.
     * @return Un {@link ResponseEntity} con la lista de {@link CuentaFinancieraResponseDto} correspondientes.
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CuentaFinancieraResponseDto>> listarPorCliente(@PathVariable UUID clienteId) {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas/cliente/{} para listar cuentas del cliente", clienteId);
        List<CuentaFinancieraResponseDto> cuentas = cuentaFinancieraService.listarPorCliente(clienteId);
        return ResponseEntity.ok(cuentas);
    }

    /**
     * Filtra y lista las cuentas financieras según su estado actual (ej. ACTIVA, SUSPENDIDA).
     *
     * @param estado Estado por el cual filtrar las cuentas.
     * @return Un {@link ResponseEntity} con la lista filtrada de {@link CuentaFinancieraResponseDto}.
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CuentaFinancieraResponseDto>> listarPorEstado(@PathVariable EstadoCuenta estado) {
        log.info("Recibida solicitud HTTP GET en /api/v1/cuentas/estado/{} para listar cuentas", estado);
        List<CuentaFinancieraResponseDto> cuentas = cuentaFinancieraService.listarPorEstado(estado);
        return ResponseEntity.ok(cuentas);
    }

    /**
     * Realiza un depósito de fondos en una cuenta específica.
     *
     * @param cuentaId Identificador único de la cuenta receptora.
     * @param monto    Monto monetario a depositar (debe ser mayor a cero).
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} actualizado y estado HTTP 200 (OK).
     */
    @PostMapping("/{cuentaId}/depositar")
    public ResponseEntity<CuentaFinancieraResponseDto> depositar(
            @PathVariable UUID cuentaId,
            @RequestParam Double monto) {
        log.info("Recibida solicitud HTTP POST para depositar {} en la cuenta ID: {}", monto, cuentaId);
        CuentaFinancieraResponseDto cuentaActualizada = cuentaFinancieraService.depositar(cuentaId, monto);
        return ResponseEntity.ok(cuentaActualizada);
    }

    /**
     * Realiza una extracción de fondos desde una cuenta específica.
     *
     * @param cuentaId Identificador único de la cuenta de origen.
     * @param monto    Monto monetario a extraer.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} actualizado y estado HTTP 200 (OK).
     */
    @PostMapping("/{cuentaId}/extraer")
    public ResponseEntity<CuentaFinancieraResponseDto> extraer(
            @PathVariable UUID cuentaId,
            @RequestParam Double monto) {
        log.info("Recibida solicitud HTTP POST para extraer {} de la cuenta ID: {}", monto, cuentaId);
        CuentaFinancieraResponseDto cuentaActualizada = cuentaFinancieraService.extraer(cuentaId, monto);
        return ResponseEntity.ok(cuentaActualizada);
    }

    /**
     * Actualiza el estado operativo de una cuenta financiera (ej. suspender o activar).
     *
     * @param cuentaId    Identificador único de la cuenta.
     * @param nuevoEstado El nuevo estado a aplicar.
     * @return Un {@link ResponseEntity} con el {@link CuentaFinancieraResponseDto} modificado y estado HTTP 200 (OK).
     */
    @PatchMapping("/{cuentaId}/estado")
    public ResponseEntity<CuentaFinancieraResponseDto> cambiarEstado(
            @PathVariable UUID cuentaId,
            @RequestParam EstadoCuenta nuevoEstado) {
        log.info("Recibida solicitud HTTP PATCH para cambiar estado de cuenta ID: {} a {}", cuentaId, nuevoEstado);
        CuentaFinancieraResponseDto cuentaActualizada = cuentaFinancieraService.cambiarEstado(cuentaId, nuevoEstado);
        return ResponseEntity.ok(cuentaActualizada);
    }

    /**
     * Elimina una cuenta financiera del sistema por su ID.
     *
     * @param id Identificador único de la cuenta a eliminar.
     * @return Un {@link ResponseEntity} sin contenido y código HTTP 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable UUID id) {
        log.info("Recibida solicitud HTTP DELETE en /api/v1/cuentas/{} para eliminar cuenta", id);
        cuentaFinancieraService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }
}