package com.pedidos360.rabbitadmin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configura el cliente HTTP hacia la API de administración de RabbitMQ. */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class ManagementClientConfig {

    @Bean
    public RestClient managementRestClient(RestClient.Builder builder, AdminProperties props) {
        return builder
                .baseUrl(props.management().url())
                .defaultHeaders(headers ->
                        headers.setBasicAuth(props.management().user(), props.management().password()))
                .build();
    }
}
