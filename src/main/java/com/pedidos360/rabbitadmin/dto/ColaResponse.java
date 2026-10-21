package com.pedidos360.rabbitadmin.dto;

/** Representación de una cola hacia el cliente de la API. */
public record ColaResponse(
        String name,
        String type,
        boolean durable,
        long messages,
        long consumers) {
}
