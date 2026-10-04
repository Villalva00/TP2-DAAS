package com.carrillovillalvadaas.tp2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la API REST ({@link RestControllerAdvice}).
 * <p>
 * Intercepta de manera centralizada las excepciones lanzadas tanto en la capa de servicios
 * (reglas de negocio y recursos ausentes) como en la frontera de los controladores (errores de validación de DTOs).
 * Su responsabilidad es transformar dichas excepciones en respuestas HTTP estructuradas y limpias,
 * devolviendo un formato JSON homogéneo que incluye marca temporal, código de estado y descripción del error.
 * </p>
 *
 * @author Carrillo Gonzalo Alejo, Villalva Elias Maciel
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepta la excepción {@link RecursoNoEncontradoException} cuando se busca un dato inexistente.
     *
     * @Excepción capturada que contiene el detalle del recurso faltante.
     * @return Una {@link ResponseEntity} conteniendo un mapa JSON con el error y estado HTTP 404 (NOT_FOUND).
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Recurso no encontrado");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Intercepta la excepción {@link SaldoInsuficienteException} ante fallos por falta de fondos o reglas operativas.
     *
     * @Excepción capturada con el motivo del rechazo financiero.
     * @return Una {@link ResponseEntity} conteniendo un mapa JSON con el error y estado HTTP 400 (BAD_REQUEST).
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de Regla de Negocio");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Intercepta los errores de validación generados por Spring cuando un {@code Request DTO}
     * falla ante las anotaciones de Jakarta Bean Validation (ej. @NotBlank, @Email, @Positive).
     *
     * @Excepción de validación lanzada automáticamente por el framework Spring MVC.
     * @return Una {@link ResponseEntity} con el detalle de cada campo inválido y estado HTTP 400 (BAD_REQUEST).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> erroresValidacion = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            erroresValidacion.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de Validación en los datos de entrada");
        errorResponse.put("messages", erroresValidacion);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    /**
     * Intercepta {@link IllegalArgumentException} (monto inválido, tipo de cuenta inválido,
     * cliente duplicado, formato numérico inválido) y la traduce a HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Solicitud inválida");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}