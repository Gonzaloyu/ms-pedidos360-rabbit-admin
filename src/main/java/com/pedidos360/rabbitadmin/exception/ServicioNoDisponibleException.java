package com.pedidos360.rabbitadmin.exception;

/** RabbitMQ o su API de administración no responden (HTTP 503). */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
