package com.pedidos360.rabbitadmin.dto;

/** Reglas de validación compartidas por todos los DTO (un solo lugar donde cambiarlas). */
public final class ReglasNombre {

    /** Nombres de colas y exchanges: letras, números, punto, guion y guion bajo. */
    public static final String NOMBRE = "^[A-Za-z0-9._-]{1,255}$";

    /** Routing keys: igual que los nombres, más los comodines de topic (* y #). Puede ir vacía. */
    public static final String ROUTING_KEY = "^[A-Za-z0-9._*#-]{0,255}$";

    public static final String MENSAJE_NOMBRE =
            "Solo se permiten letras, números, '.', '_' y '-' (máximo 255 caracteres)";

    private ReglasNombre() {
    }
}
