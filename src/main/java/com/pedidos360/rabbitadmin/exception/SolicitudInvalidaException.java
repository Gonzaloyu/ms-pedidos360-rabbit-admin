package com.pedidos360.rabbitadmin.exception;

/** La solicitud es incorrecta (HTTP 400). */
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
