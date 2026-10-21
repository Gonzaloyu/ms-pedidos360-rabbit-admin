package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para crear una cola. Solo name es obligatorio.
 *  - type: classic o quorum (por defecto quorum).
 *  - deadLetterExchange y deadLetterRoutingKey: se envían juntos para asociar una DLQ.
 */
public record CrearColaRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = ReglasNombre.NOMBRE, message = ReglasNombre.MENSAJE_NOMBRE)
        String name,

        @Pattern(regexp = "^(?i)(classic|quorum)$", message = "El tipo debe ser 'classic' o 'quorum'")
        String type,

        @Pattern(regexp = ReglasNombre.NOMBRE, message = ReglasNombre.MENSAJE_NOMBRE)
        String deadLetterExchange,

        @Pattern(regexp = ReglasNombre.ROUTING_KEY, message = "La routing key de dead letter no es válida")
        String deadLetterRoutingKey) {
}
