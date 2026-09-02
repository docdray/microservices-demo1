package org.acme

import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.OpenConnections
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming

@WebSocket(path = "/ws/messages")
class MessageSocket {

    @OnOpen
    fun onOpen(): String {
        return "Verbunden mit Kafka-Nachrichten-Stream"
    }
}

@ApplicationScoped
class KafkaMessageConsumer {

    @Inject
    lateinit var connections: OpenConnections

    @Incoming("kafkatest-in")
    fun consume(message: String) {
        connections.forEach { connection ->
            connection.sendTextAndAwait(message)
        }
    }
}