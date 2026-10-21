package com.pedidos360.rabbitadmin.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte cada excepción en una respuesta HTTP clara y consistente. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (FieldError campo : e.getBindingResult().getFieldErrors()) {
            String mensaje = campo.getDefaultMessage() == null ? "Valor inválido" : campo.getDefaultMessage();
            detalles.merge(campo.getField(), mensaje, (a, b) -> a + "; " + b);
        }
        return responder(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> parametroFaltante(MissingServletRequestParameterException e) {
        return responder(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + e.getParameterName() + "'", Map.of());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiError> solicitudInvalida(SolicitudInvalidaException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), Map.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), Map.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflicto(ConflictoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), Map.of());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ApiError> servicioNoDisponible(ServicioNoDisponibleException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), Map.of());
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ApiError> rabbitCaido(AmqpException e) {
        log.error("Error de RabbitMQ: {}", e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE,
                "RabbitMQ no está disponible o rechazó la operación", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception e) {
        // Errores propios de Spring MVC (ruta inexistente, método no permitido...): conservan su código HTTP.
        if (e instanceof ErrorResponse errorSpring) {
            HttpStatus estado = HttpStatus.resolve(errorSpring.getStatusCode().value());
            return responder(estado != null ? estado : HttpStatus.BAD_REQUEST,
                    errorSpring.getBody().getDetail(), Map.of());
        }
        log.error("Error inesperado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", Map.of());
    }

    private ResponseEntity<ApiError> responder(HttpStatus estado, String mensaje, Map<String, String> detalles) {
        ApiError cuerpo = new ApiError(Instant.now(), estado.value(), estado.getReasonPhrase(), mensaje, detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
