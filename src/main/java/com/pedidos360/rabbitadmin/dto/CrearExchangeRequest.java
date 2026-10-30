package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record CrearExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = ReglasNombre.NOMBRE, message = ReglasNombre.MENSAJE_NOMBRE)
        String name,

        @NotBlank(message = "El tipo de exchange es obligatorio")
        @Pattern(regexp = "^(?i)(direct|topic|fanout|headers)$",
                message = "El tipo debe ser direct, topic, fanout o headers")
        String type,

        Boolean durable) {
}
