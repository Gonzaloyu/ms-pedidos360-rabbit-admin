package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.BindingRequest;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API REST de bindings (unión cola-exchange). Delega todo en RabbitAdminService. */
@RestController
@RequestMapping("/api/bindings")
public class BindingController {

    private final RabbitAdminService service;

    public BindingController(RabbitAdminService service) {
        this.service = service;
    }

    /** POST /api/bindings -> 201 Created */
    @PostMapping
    public ResponseEntity<BindingRequest> crear(@Valid @RequestBody BindingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBinding(request));
    }

    /** DELETE /api/bindings?queue=..&exchange=..&routingKey=.. -> 204 */
    @DeleteMapping
    public ResponseEntity<Void> eliminar(@RequestParam String queue,
                                         @RequestParam String exchange,
                                         @RequestParam(defaultValue = "") String routingKey) {
        service.eliminarBinding(queue, exchange, routingKey);
        return ResponseEntity.noContent().build();
    }
}
