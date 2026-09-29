package com.carrillovillalvadaas.tp2.controller;

import com.carrillovillalvadaas.tp2.dto.TransaccionRequestDto;
import com.carrillovillalvadaas.tp2.dto.TransaccionResponseDto;
import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST especializado en la gestión y procesamiento de transacciones financieras
 * ({@link com.carrillovillalvadaas.tp2.model.Transaccion}, tales como depósitos y extracciones).
 * <p>
 * Actúa como punto de entrada para operaciones monetarias sensibles, garantizando el desacoplamiento
 * de la persistencia mediante DTOs ({@link TransaccionRequestDto} y {@link TransaccionResponseDto}),
 * validación de datos de entrada y control de excepciones de negocio.
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 * Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@RestController
@RequestMapping("/api/v1/transacciones")
@Slf4j
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    /**
     * Registra y procesa un depósito financiero en una cuenta específica a través de su CBU.
     * <p>
     * Recibe un DTO de solicitud validado, incrementa el saldo operativo de la cuenta de destino
     * de forma atómica y genera el comprobante correspondiente con estado COMPLETADA.
     * </p>
     *
     * @param requestDto DTO que contiene los datos necesarios para el depósito (CBU de la cuenta y monto).
     * @return Un {@link ResponseEntity} con el {@link TransaccionResponseDto} de la transacción registrada
     *         y el código de estado HTTP 201 (Created).
     */
    @PostMapping("/deposito")
    public ResponseEntity<TransaccionResponseDto> registrarDeposito(@Valid @RequestBody TransaccionRequestDto requestDto) {
        log.info("Recibida solicitud HTTP POST en /api/v1/transacciones/deposito para CBU: {}", requestDto.getCbuCuenta());
        TransaccionResponseDto respuesta = transaccionService.registrarDeposito(requestDto);
        log.info("Depósito procesado y registrado con éxito. ID de transacción: {}", respuesta.getIdTransaccion());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Registra y procesa una extracción de fondos desde una cuenta específica a través de su CBU.
     * <p>
     * Valida la disponibilidad de fondos. Si la cuenta cuenta con saldo suficiente, efectúa la extracción
     * y marca la transacción como COMPLETADA. En caso contrario, registra la transacción como RECHAZADA
     * y lanza una excepción controlada de negocio.
     * </p>
     *
     * @param requestDto DTO que contiene los datos de la extracción (CBU de la cuenta y monto a extraer).
     * @return Un {@link ResponseEntity} con el {@link TransaccionResponseDto} correspondiente
     *         y el código de estado HTTP 201 (Created).
     */
    @PostMapping("/extraccion")
    public ResponseEntity<TransaccionResponseDto> registrarExtraccion(@Valid @RequestBody TransaccionRequestDto requestDto) {
        log.info("Recibida solicitud HTTP POST en /api/v1/transacciones/extraccion para CBU: {}", requestDto.getCbuCuenta());
        TransaccionResponseDto respuesta = transaccionService.registrarExtraccion(requestDto);
        log.info("Extracción procesada. Estado resultante: {}. ID de transacción: {}",
                respuesta.getEstadoTransaccion(), respuesta.getIdTransaccion());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Busca y retorna el comprobante o registro de una transacción a partir de su ID numérico único.
     *
     * @param id Identificador numérico de la transacción.
     * @return Un {@link ResponseEntity} con el {@link TransaccionResponseDto} encontrado
     *         y el código de estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponseDto> obtenerPorId(@PathVariable Long id) {
        log.info("Recibida solicitud HTTP GET en /api/v1/transacciones/{} para consultar detalles de transacción", id);
        TransaccionResponseDto transaccion = transaccionService.obtenerPorId(id);
        return ResponseEntity.ok(transaccion);
    }

    /**
     * Lista el historial completo de transacciones asociadas a una cuenta financiera específica (por su ID de cuenta).
     *
     * @param cuentaId Identificador único universal (UUID) de la cuenta financiera.
     * @return Un {@link ResponseEntity} con una lista de {@link TransaccionResponseDto} de dicha cuenta
     *         y código HTTP 200 (OK).
     */
    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionResponseDto>> listarPorCuenta(@PathVariable UUID cuentaId) {
        log.info("Recibida solicitud HTTP GET en /api/v1/transacciones/cuenta/{} para listar historial", cuentaId);
        List<TransaccionResponseDto> transacciones = transaccionService.listarPorCuenta(cuentaId);
        return ResponseEntity.ok(transacciones);
    }

    /**
     * Filtra y lista el historial de transacciones según su tipo operativo (ej. DEPOSITO, EXTRACCION).
     *
     * @param tipo El tipo de transacción por el cual filtrar.
     * @return Un {@link ResponseEntity} con la lista filtrada de {@link TransaccionResponseDto}
     *         y código HTTP 200 (OK).
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransaccionResponseDto>> listarPorTipo(@PathVariable TipoTransaccion tipo) {
        log.info("Recibida solicitud HTTP GET en /api/v1/transacciones/tipo/{} para filtrar operaciones", tipo);
        List<TransaccionResponseDto> transacciones = transaccionService.listarPorTipo(tipo);
        return ResponseEntity.ok(transacciones);
    }

    /**
     * Filtra y lista las transacciones según su estado de procesamiento (ej. COMPLETADA, RECHAZADA).
     *
     * @param estado El estado de la transacción por el cual filtrar.
     * @return Un {@link ResponseEntity} con la lista filtrada de {@link TransaccionResponseDto}
     *         y código HTTP 200 (OK).
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TransaccionResponseDto>> listarPorEstado(@PathVariable EstadoTransaccion estado) {
        log.info("Recibida solicitud HTTP GET en /api/v1/transacciones/estado/{} para filtrar operaciones", estado);
        List<TransaccionResponseDto> transacciones = transaccionService.listarPorEstado(estado);
        return ResponseEntity.ok(transacciones);
    }

    /**
     * Obtiene la totalidad de las transacciones registradas históricamente en el sistema.
     *
     * @return Un {@link ResponseEntity} con la lista completa de {@link TransaccionResponseDto}
     *         y código HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<TransaccionResponseDto>> listarTodas() {
        log.info("Recibida solicitud HTTP GET en /api/v1/transacciones para listar la totalidad de las operaciones");
        List<TransaccionResponseDto> transacciones = transaccionService.listarTodas();
        return ResponseEntity.ok(transacciones);
    }
}