package com.main.kpiengine.controller;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de errores para la API de KPI Engine.
 *
 * <p>Traduce excepciones comunes a respuestas HTTP estructuradas y estables
 * para clientes consumidores.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Maneja errores de validación de bean validation en payloads.
     *
     * @param ex excepción con detalle de campos inválidos.
     * @return respuesta 400 con lista de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", "Request validation failed");
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> detail = new LinkedHashMap<>();
                    detail.put("field", fieldError.getField());
                    detail.put("message", fieldError.getDefaultMessage());
                    return detail;
                })
                .toList();
        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Maneja body ausente o JSON mal formado.
     *
     * @param ex excepción de deserialización/parsing.
     * @return respuesta 400 con mensaje estándar de formato inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Malformed JSON");
        body.put("message", "Request body is missing or has invalid JSON format");
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Fallback genérico para errores no controlados explícitamente.
     *
     * @param ex excepción inesperada.
     * @return respuesta 500 con mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Unexpected error in KPI Engine");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
