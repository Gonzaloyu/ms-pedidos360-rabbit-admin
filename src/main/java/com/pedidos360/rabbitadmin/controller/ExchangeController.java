package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.dto.ExchangeResponse;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** API REST de exchanges. No contiene lógica: delega todo en RabbitAdminService. */
@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final RabbitAdminService service;

    public ExchangeController(RabbitAdminService service) {
        this.service = service;
    }

    /** POST /api/exchanges -> 201 Created */
    @PostMapping
    public ResponseEntity<ExchangeResponse> crear(@Valid @RequestBody CrearExchangeRequest request) {
        ExchangeResponse creado = service.crearExchange(request);
        return ResponseEntity.created(URI.create("/api/exchanges/" + creado.name())).body(creado);
    }

    /** DELETE /api/exchanges/{name} -> 204, 404 o 409 (recurso del sistema) */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> eliminar(@PathVariable String name) {
        service.eliminarExchange(name);
        return ResponseEntity.noContent().build();
    }
}
