# ms-pedidos360-rabbit-admin

Microservicio administrador de RabbitMQ. Puerto 8084.

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | /api/queues | Crear cola | 201, 400, 404, 409 |
| GET | /api/queues | Listar colas | 200, 503 |
| GET | /api/queues/{name} | Detalle de una cola | 200, 404 |
| DELETE | /api/queues/{name} | Eliminar cola | 204, 404, 409 |
| DELETE | /api/queues/{name}/messages | Vaciar cola | 200, 404 |
| POST | /api/exchanges | Crear exchange | 201, 400, 409 |
| DELETE | /api/exchanges/{name} | Eliminar exchange | 204, 404, 409 |
| POST | /api/bindings | Unir cola y exchange | 201, 400, 404 |
| DELETE | /api/bindings?queue=&exchange=&routingKey= | Quitar binding | 204, 404, 409 |

Estructura: controller (HTTP) -> service (toda la lógica de RabbitMQ) -> RabbitMQ.
Nombres, credenciales y recursos protegidos: application.yml.

Ejecutar:  mvn spring-boot:run
