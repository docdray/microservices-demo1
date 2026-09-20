package org.acme

import io.quarkus.websockets.next.OpenConnections
import io.quarkus.websockets.next.WebSocketConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.util.stream.Stream

/** Minimale Fake-Implementierung, da OpenConnections' iterator()/stream() abstrakt sind. */
private class FixedOpenConnections(private val items: List<WebSocketConnection>) : OpenConnections {
    override fun iterator(): MutableIterator<WebSocketConnection> = items.toMutableList().iterator()
    override fun stream(): Stream<WebSocketConnection> = items.stream()
}

class MessageSocketTest {

    @Test
    fun testOnOpenReturnsGreeting() {
        assertEquals("Verbunden mit Kafka-Nachrichten-Stream", MessageSocket().onOpen())
    }
}

class KafkaMessageConsumerTest {

    @Test
    fun testConsumeBroadcastsToAllOpenConnections() {
        val connectionA = mock(WebSocketConnection::class.java)
        val connectionB = mock(WebSocketConnection::class.java)
        val consumer = KafkaMessageConsumer()
        consumer.connections = FixedOpenConnections(listOf(connectionA, connectionB))

        consumer.consume("Hallo Welt")

        verify(connectionA).sendTextAndAwait("Hallo Welt")
        verify(connectionB).sendTextAndAwait("Hallo Welt")
    }

    @Test
    fun testConsumeWithNoOpenConnectionsDoesNothing() {
        val connection = mock(WebSocketConnection::class.java)
        val consumer = KafkaMessageConsumer()
        consumer.connections = FixedOpenConnections(emptyList())

        consumer.consume("Niemand da")

        verifyNoInteractions(connection)
    }
}
