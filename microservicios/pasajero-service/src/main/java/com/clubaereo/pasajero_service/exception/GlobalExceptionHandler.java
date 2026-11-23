package com.clubaereo.pasajero_service.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.clubaereo.pasajero_service.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String error, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, mensaje));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(NotFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictException e) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto", e.getMessage());
    }

    // Red de seguridad: la restricción única (rut, jornada_id) de la BD detectó un duplicado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto",
                "La operación viola una restricción de datos (posible pasajero duplicado en la jornada)");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> peticionInvalida(BadRequestException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Petición inválida", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.conCampos(400, "Validación", "Datos inválidos", campos));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
                       MissingServletRequestParameterException.class,
                       MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> peticionMalFormada(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Petición inválida",
                "La petición está mal formada o le faltan parámetros");
    }
}
