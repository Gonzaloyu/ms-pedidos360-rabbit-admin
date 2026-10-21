package com.pedidos360.rabbitadmin.dto;

/** Representación de un exchange hacia el cliente de la API. */
public record ExchangeResponse(String name, String type, boolean durable) {
}
