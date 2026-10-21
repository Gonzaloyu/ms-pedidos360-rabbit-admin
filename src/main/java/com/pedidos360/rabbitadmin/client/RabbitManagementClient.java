package com.pedidos360.rabbitadmin.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pedidos360.rabbitadmin.dto.ColaResponse;
import com.pedidos360.rabbitadmin.exception.ServicioNoDisponibleException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Cliente de la API HTTP de administración de RabbitMQ (puerto 15672).
 * El protocolo AMQP no permite LISTAR colas, por eso el listado se consulta aquí.
 */
@Component
public class RabbitManagementClient {

    private static final String VHOST_POR_DEFECTO = "/";

    private final RestClient restClient;

    public RabbitManagementClient(RestClient managementRestClient) {
        this.restClient = managementRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ColaGestion(String name, String vhost, String type,
                              boolean durable, long messages, long consumers) {
    }

    public List<ColaResponse> listarColas() {
        try {
            List<ColaGestion> colas = restClient.get()
                    .uri("/api/queues")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ColaGestion>>() {
                    });

            if (colas == null) {
                return List.of();
            }
            return colas.stream()
                    .filter(c -> VHOST_POR_DEFECTO.equals(c.vhost()))
                    .map(c -> new ColaResponse(
                            c.name(),
                            c.type() == null ? "classic" : c.type(),
                            c.durable(),
                            c.messages(),
                            c.consumers()))
                    .toList();

        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo consultar la API de administración de RabbitMQ: " + e.getMessage());
        }
    }
}
