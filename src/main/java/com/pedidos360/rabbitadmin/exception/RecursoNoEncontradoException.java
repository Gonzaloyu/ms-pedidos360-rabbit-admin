package com.pedidos360.rabbitadmin.exception;

/** La cola o el exchange no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
