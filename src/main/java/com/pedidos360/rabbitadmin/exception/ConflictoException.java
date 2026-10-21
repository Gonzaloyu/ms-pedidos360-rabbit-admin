package com.pedidos360.rabbitadmin.exception;

/** El recurso ya existe o está protegido (HTTP 409). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
