package com.pedidos360.rabbitadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Lee del application.yml (prefijo pedidos360.admin) la conexión a la API de
 * administración de RabbitMQ y la lista de recursos protegidos.
 */
@ConfigurationProperties(prefix = "pedidos360.admin")
public record AdminProperties(Management management, List<String> protectedNames) {

    public AdminProperties {
        if (protectedNames == null) {
            protectedNames = List.of();
        }
    }

    public record Management(String url, String user, String password) {}
}
