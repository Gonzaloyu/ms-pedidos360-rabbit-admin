package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.client.RabbitManagementClient;
import com.pedidos360.rabbitadmin.config.AdminProperties;
import com.pedidos360.rabbitadmin.dto.BindingRequest;
import com.pedidos360.rabbitadmin.dto.ColaResponse;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.dto.ExchangeResponse;
import com.pedidos360.rabbitadmin.dto.ReglasNombre;
import com.pedidos360.rabbitadmin.exception.ConflictoException;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import com.pedidos360.rabbitadmin.exception.ServicioNoDisponibleException;
import com.pedidos360.rabbitadmin.exception.SolicitudInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * ÚNICO lugar que conoce la librería de RabbitMQ.
 * Los controladores solo llaman a estos métodos de alto nivel.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private static final Pattern NOMBRE_VALIDO = Pattern.compile(ReglasNombre.NOMBRE);
    private static final Pattern ROUTING_KEY_VALIDA = Pattern.compile(ReglasNombre.ROUTING_KEY);
    private static final String RESERVADO = "amq.";
    private static final String TIPO_QUORUM = "quorum";
    private static final String TIPO_CLASSIC = "classic";

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitManagementClient managementClient;
    private final AdminProperties props;

    public RabbitAdminService(AmqpAdmin amqpAdmin,
                              RabbitTemplate rabbitTemplate,
                              RabbitManagementClient managementClient,
                              AdminProperties props) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.managementClient = managementClient;
        this.props = props;
    }

    // ==================================================================
    // COLAS
    // ==================================================================

    public ColaResponse crearCola(CrearColaRequest request) {
        String nombre = validarNombre(request.name());
        exigirNoReservado(nombre);

        if (colaExiste(nombre)) {
            throw new ConflictoException("La cola '" + nombre + "' ya existe");
        }

        String tipo = request.type() == null ? TIPO_QUORUM : request.type().toLowerCase(Locale.ROOT);

        QueueBuilder cola = QueueBuilder.durable(nombre);
        if (TIPO_QUORUM.equals(tipo)) {
            cola.quorum();
        }
        aplicarDeadLetter(cola, request);

        amqpAdmin.declareQueue(cola.build());
        log.info("[ADMIN] Cola creada: {} ({})", nombre, tipo);
        return new ColaResponse(nombre, tipo, true, 0, 0);
    }

    public List<ColaResponse> listarColas() {
        return managementClient.listarColas();
    }

    public ColaResponse obtenerCola(String nombre) {
        validarNombre(nombre);
        QueueInformation info = amqpAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new RecursoNoEncontradoException("La cola '" + nombre + "' no existe");
        }
        return new ColaResponse(nombre, tipoDeCola(nombre), true,
                info.getMessageCount(), info.getConsumerCount());
    }

    public void eliminarCola(String nombre) {
        validarNombre(nombre);
        exigirNoProtegido(nombre);
        if (!colaExiste(nombre)) {
            throw new RecursoNoEncontradoException("La cola '" + nombre + "' no existe");
        }
        amqpAdmin.deleteQueue(nombre);
        log.info("[ADMIN] Cola eliminada: {}", nombre);
    }

    /** Elimina todos los mensajes de una cola. Devuelve cuántos se borraron. */
    public int vaciarCola(String nombre) {
        validarNombre(nombre);
        if (!colaExiste(nombre)) {
            throw new RecursoNoEncontradoException("La cola '" + nombre + "' no existe");
        }
        Integer purgados = rabbitTemplate.execute(channel -> channel.queuePurge(nombre).getMessageCount());
        log.info("[ADMIN] Cola vaciada: {} ({} mensajes)", nombre, purgados);
        return purgados == null ? 0 : purgados;
    }

    // ==================================================================
    // EXCHANGES
    // ==================================================================

    public ExchangeResponse crearExchange(CrearExchangeRequest request) {
        String nombre = validarNombre(request.name());
        exigirNoReservado(nombre);

        if (exchangeExiste(nombre)) {
            throw new ConflictoException("El exchange '" + nombre + "' ya existe");
        }

        String tipo = request.type().toLowerCase(Locale.ROOT);
        boolean durable = request.durable() == null || request.durable();

        Exchange exchange = switch (tipo) {
            case "direct" -> new DirectExchange(nombre, durable, false);
            case "topic" -> new TopicExchange(nombre, durable, false);
            case "fanout" -> new FanoutExchange(nombre, durable, false);
            case "headers" -> new HeadersExchange(nombre, durable, false);
            default -> throw new SolicitudInvalidaException("Tipo de exchange no soportado: " + tipo);
        };

        amqpAdmin.declareExchange(exchange);
        log.info("[ADMIN] Exchange creado: {} ({})", nombre, tipo);
        return new ExchangeResponse(nombre, tipo, durable);
    }

    public void eliminarExchange(String nombre) {
        validarNombre(nombre);
        exigirNoProtegido(nombre);
        if (!exchangeExiste(nombre)) {
            throw new RecursoNoEncontradoException("El exchange '" + nombre + "' no existe");
        }
        amqpAdmin.deleteExchange(nombre);
        log.info("[ADMIN] Exchange eliminado: {}", nombre);
    }

    // ==================================================================
    // BINDINGS
    // ==================================================================

    public BindingRequest crearBinding(BindingRequest request) {
        Binding binding = construirBinding(request);
        amqpAdmin.declareBinding(binding);
        log.info("[ADMIN] Binding creado: {} -> {} ({})",
                request.exchange(), request.queue(), binding.getRoutingKey());
        return new BindingRequest(request.queue(), request.exchange(), binding.getRoutingKey());
    }

    public void eliminarBinding(String cola, String exchange, String routingKey) {
        BindingRequest request = new BindingRequest(cola, exchange, routingKey);
        exigirNoProtegido(request.queue());
        exigirNoProtegido(request.exchange());

        Binding binding = construirBinding(request);
        amqpAdmin.removeBinding(binding);
        log.info("[ADMIN] Binding eliminado: {} -> {} ({})", exchange, cola, binding.getRoutingKey());
    }

    private Binding construirBinding(BindingRequest request) {
        String cola = validarNombre(request.queue());
        String exchange = validarNombre(request.exchange());
        String clave = request.routingKey() == null ? "" : request.routingKey();

        if (!ROUTING_KEY_VALIDA.matcher(clave).matches()) {
            throw new SolicitudInvalidaException("La routing key no es válida");
        }
        if (!colaExiste(cola)) {
            throw new RecursoNoEncontradoException("La cola '" + cola + "' no existe");
        }
        if (!exchangeExiste(exchange)) {
            throw new RecursoNoEncontradoException("El exchange '" + exchange + "' no existe");
        }
        return new Binding(cola, Binding.DestinationType.QUEUE, exchange, clave, null);
    }

    // ==================================================================
    // Apoyo interno
    // ==================================================================

    private void aplicarDeadLetter(QueueBuilder cola, CrearColaRequest request) {
        boolean hayExchange = tieneTexto(request.deadLetterExchange());
        boolean hayClave = tieneTexto(request.deadLetterRoutingKey());

        if (hayExchange != hayClave) {
            throw new SolicitudInvalidaException(
                    "deadLetterExchange y deadLetterRoutingKey deben enviarse juntos");
        }
        if (hayExchange) {
            if (!exchangeExiste(request.deadLetterExchange())) {
                throw new RecursoNoEncontradoException(
                        "El exchange de dead letter '" + request.deadLetterExchange() + "' no existe");
            }
            cola.deadLetterExchange(request.deadLetterExchange())
                    .deadLetterRoutingKey(request.deadLetterRoutingKey());
        }
    }

    private boolean colaExiste(String nombre) {
        return amqpAdmin.getQueueInfo(nombre) != null;
    }

    /** Un exchange que no existe hace que RabbitMQ rechace la consulta (404): eso significa "no existe". */
    private boolean exchangeExiste(String nombre) {
        try {
            Boolean existe = rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(nombre);
                return Boolean.TRUE;
            });
            return Boolean.TRUE.equals(existe);
        } catch (AmqpIOException e) {
            return false;
        }
    }

    /** El tipo se consulta a la API de administración; si no responde, se omite sin fallar. */
    private String tipoDeCola(String nombre) {
        try {
            return managementClient.listarColas().stream()
                    .filter(c -> c.name().equals(nombre))
                    .map(ColaResponse::type)
                    .findFirst()
                    .orElse(TIPO_CLASSIC);
        } catch (ServicioNoDisponibleException e) {
            return null;
        }
    }

    private String validarNombre(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new SolicitudInvalidaException("Nombre inválido. " + ReglasNombre.MENSAJE_NOMBRE);
        }
        return nombre;
    }

    private void exigirNoReservado(String nombre) {
        if (nombre.startsWith(RESERVADO)) {
            throw new SolicitudInvalidaException("Los nombres que empiezan con 'amq.' están reservados por RabbitMQ");
        }
    }

    private void exigirNoProtegido(String nombre) {
        if (props.protectedNames().contains(nombre)) {
            throw new ConflictoException("'" + nombre + "' es un recurso del sistema y no puede eliminarse");
        }
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
