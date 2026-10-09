package com.carrillovillalvadaas.tp2.controller;

import com.carrillovillalvadaas.tp2.dto.ClienteRequestDto;
import com.carrillovillalvadaas.tp2.dto.ClienteResponseDto;
import com.carrillovillalvadaas.tp2.service.ClienteService;
import com.carrillovillalvadaas.tp2.dto.AdherenteRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST especializado en la gestión de recursos de tipo {@link com.carrillovillalvadaas.tp2.model.Cliente}.
 * <p>
 * Esta clase forma parte de la capa de presentación (API RESTful), actuando como el punto de entrada principal
 * para las solicitudes HTTP del cliente externo. Se adhiere estrictamente a los principios de arquitectura limpia,
 * desacoplando la capa de persistencia mediante el uso exclusivo de objetos de transferencia de datos
 * ({@link ClienteRequestDto} y {@link ClienteResponseDto}).
 * </p>
 * <p>
 * Gestiona operaciones CRUD completas aplicando validaciones automáticas de datos a través de la anotación
 * {@link Valid} y delegando la lógica transaccional de negocio de forma segura a {@link ClienteService}.
 * </p>
 *
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@RestController
@RequestMapping("/api/v1/clientes")
@Slf4j
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Registra y persiste un nuevo cliente en el sistema.
     * <p>
     * Recibe un payload JSON validado que se mapea automáticamente a un {@link ClienteRequestDto},
     * invoca al servicio correspondiente para procesar las reglas de negocio y retorna el recurso creado
     * envuelto en un {@link ResponseEntity} acompañado del código de estado HTTP 201 (Created).
     * </p>
     *
     * @param requestDto El objeto {@link ClienteRequestDto} que contiene la información del nuevo cliente.
     *                   Debe cumplir estrictamente con las restricciones de validación configuradas.
     * @return Un {@link ResponseEntity} que contiene el {@link ClienteResponseDto} con los datos del cliente
     *         recién creado (incluyendo su ID de tipo {@link UUID} y marcas temporales de auditoría)
     *         junto con el estado HTTP {@link HttpStatus#CREATED}.
     */
    @PostMapping
    public ResponseEntity<ClienteResponseDto> crearCliente(@Valid @RequestBody ClienteRequestDto requestDto) {
        log.info("Recibida solicitud HTTP POST en /api/v1/clientes para registrar nuevo cliente con CUIL: {}", requestDto.getCuil());
        ClienteResponseDto clienteCreado = clienteService.crearCliente(requestDto);
        log.info("Cliente creado exitosamente con ID asignado: {}", clienteCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
    }

    /**
     * Confirma la cuenta de un cliente canjeando su token de activación recibido
     * por email. El path literal {@code /activar} es más específico que
     * {@code /{id}} y no entra en conflicto con el mapeo de detalle por UUID.
     *
     * @param token Valor del token de activación (query param obligatorio).
     * @return El {@link ClienteResponseDto} con el cliente en estado {@code ACTIVO}
     *         y estado HTTP 200 (OK). Errores de token inválido → 400.
     */
    @GetMapping("/activar")
    public ResponseEntity<ClienteResponseDto> activarCliente(@RequestParam("token") String token) {
        log.info("Recibida solicitud HTTP GET en /api/v1/clientes/activar para confirmar la cuenta");
        ClienteResponseDto clienteActivado = clienteService.activarCliente(token);
        log.info("Cliente {} activado correctamente", clienteActivado.getId());
        return ResponseEntity.ok(clienteActivado);
    }

    /**
     * Busca y retorna la información pública de un cliente específico según su identificador único universal (UUID).
     * <p>
     * Realiza una consulta por clave primaria en la capa de servicios. Si el recurso no existe,
     * el servicio lanzará una excepción controlada de negocio (como {@link com.carrillovillalvadaas.tp2.exception.RecursoNoEncontradoException}),
     * la cual será interceptada para retornar un código de error HTTP 404 (Not Found).
     * </p>
     *
     * @param id El {@link UUID} que identifica unívocamente al cliente dentro del sistema.
     * @return Un {@link ResponseEntity} que contiene el {@link ClienteResponseDto} correspondiente al cliente encontrado
     *         junto con el código de estado HTTP {@link HttpStatus#OK} (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> obtenerClientePorId(@PathVariable UUID id) {
        log.info("Recibida solicitud HTTP GET en /api/v1/clientes/{} para consultar detalles del cliente", id);
        ClienteResponseDto cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Obtiene el listado completo de todos los clientes registrados y activos en el sistema.
     * <p>
     * Recupera todas las entidades de persistencia disponibles, transformándolas de manera masiva
     * a una lista de objetos {@link ClienteResponseDto} para evitar la exposición directa del modelo interno de base de datos.
     * </p>
     *
     * @return Un {@link ResponseEntity} con una {@link List} de {@link ClienteResponseDto} y el código de estado
     *         HTTP {@link HttpStatus#OK} (200). Si no existen registros, retornará una colección vacía.
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> listarTodosLosClientes() {
        log.info("Recibida solicitud HTTP GET en /api/v1/clientes para listar la totalidad de los clientes registrados");
        List<ClienteResponseDto> clientes = clienteService.listarTodos();
        log.debug("Se recuperaron exitosamente {} clientes del sistema", clientes.size());
        return ResponseEntity.ok(clientes);
    }

    /**
     * Actualiza de forma integral los datos de un cliente existente identificado por su UUID.
     * <p>
     * Valida el cuerpo de la petición HTTP entrante y actualiza los campos permitidos del cliente en la base de datos,
     * retornando la representación actualizada del recurso.
     * </p>
     *
     * @param id         El {@link UUID} del cliente cuyos datos se desean modificar.
     * @param requestDto El {@link ClienteRequestDto} con los nuevos valores validados para actualizar al cliente.
     * @return Un {@link ResponseEntity} con el {@link ClienteResponseDto} actualizado y el código de estado
     *         HTTP {@link HttpStatus#OK} (200).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> actualizarCliente(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteRequestDto requestDto) {
        log.info("Recibida solicitud HTTP PUT en /api/v1/clientes/{} para actualizar la información del cliente", id);
        ClienteResponseDto clienteActualizado = clienteService.actualizarCliente(id, requestDto);
        log.info("Cliente con ID {} actualizado exitosamente", id);
        return ResponseEntity.ok(clienteActualizado);
    }

    /**
     * Da de baja o elimina un cliente del sistema basándose en su identificador único (UUID).
     * <p>
     * Ejecuta la operación de eliminación en la capa de servicios. Una vez completada con éxito,
     * responde sin contenido en el cuerpo, estructurado con un código de estado HTTP 204 (No Content).
     * </p>
     *
     * @param id El {@link UUID} del cliente que será eliminado del sistema.
     * @return Un {@link ResponseEntity} sin cuerpo de tipo {@link Void} configurado con el código de estado
     *         HTTP {@link HttpStatus#NO_CONTENT} (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        log.info("Recibida solicitud HTTP DELETE en /api/v1/clientes/{} para eliminar el cliente del sistema", id);
        clienteService.eliminarPorId(id);
        log.info("Cliente con ID {} eliminado correctamente", id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Registra un adherente (cónyuge o hijo) asociado a un titular.
     *
     * @param titularId  UUID del titular.
     * @param requestDto Datos validados del adherente.
     * @return El adherente creado con HTTP 201 (Created).
     */
    @PostMapping("{titularId}/adherentes")
    public ResponseEntity<ClienteResponseDto> crearAdherente(
        @PathVariable UUID titularId,
        @Valid @RequestBody AdherenteRequestDto requestDto){
        log.info("Recibida solicitud HTTP POST en /api/v1/clientes/{}/adherentes", titularId);
        ClienteResponseDto adherente = clienteService.crearAdherente(titularId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(adherente);
    }

    /**
     * Lista los adherentes de un titular.
     *
     * @param titularId UUID del titular.
     * @return Lista de adherentes con HTTP 200 (OK).
     */
    @GetMapping("/{titularId}/adherentes")
    public ResponseEntity<List<ClienteResponseDto>> listarAdherentes(@PathVariable UUID titularId) {
        log.info("Recibida solicitud HTTP GET en /api/v1/clientes/{}/adherentes", titularId);
        return ResponseEntity.ok(clienteService.listarAdherentes(titularId));
    }

}