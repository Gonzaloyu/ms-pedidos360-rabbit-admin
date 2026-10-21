package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Une una cola a un exchange mediante una routing key. Se usa como entrada y como respuesta. */
public record BindingRequest(

        @NotBlank(message = "La cola es obligatoria")
        @Pattern(regexp = ReglasNombre.NOMBRE, message = ReglasNombre.MENSAJE_NOMBRE)
        String queue,

        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = ReglasNombre.NOMBRE, message = ReglasNombre.MENSAJE_NOMBRE)
        String exchange,

        @Pattern(regexp = ReglasNombre.ROUTING_KEY, message = "La routing key no es válida")
        String routingKey) {
}
