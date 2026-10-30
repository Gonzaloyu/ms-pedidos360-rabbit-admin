package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.ColaResponse;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final RabbitAdminService service;

    public QueueController(RabbitAdminService service) {
        this.service = service;
    }

    /** POST /api/queues -> 201 Created */
    @PostMapping
    public ResponseEntity<ColaResponse> crear(@Valid @RequestBody CrearColaRequest request) {
        ColaResponse creada = service.crearCola(request);
        return ResponseEntity.created(URI.create("/api/queues/" + creada.name())).body(creada);
    }

    /** GET /api/queues -> 200 */
    @GetMapping
    public List<ColaResponse> listar() {
        return service.listarColas();
    }

    /** GET /api/queues/{name} -> 200 o 404 */
    @GetMapping("/{name}")
    public ColaResponse obtener(@PathVariable String name) {
        return service.obtenerCola(name);
    }

    /** DELETE /api/queues/{name} -> 204, 404 o 409 (recurso del sistema) */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> eliminar(@PathVariable String name) {
        service.eliminarCola(name);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/queues/{name}/messages -> vacía la cola (útil para las DLQ) */
    @DeleteMapping("/{name}/messages")
    public Map<String, Object> vaciar(@PathVariable String name) {
        int purgados = service.vaciarCola(name);
        return Map.of("queue", name, "purgedMessages", purgados);
    }
}
