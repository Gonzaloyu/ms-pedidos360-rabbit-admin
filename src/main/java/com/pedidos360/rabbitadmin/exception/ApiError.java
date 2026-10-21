package com.pedidos360.rabbitadmin.exception;

import java.time.Instant;
import java.util.Map;

/** Cuerpo estándar de todas las respuestas de error de la API. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details) {
}
