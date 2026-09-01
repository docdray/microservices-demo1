package org.acme

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import jakarta.ws.rs.core.MediaType
import java.time.Instant

@Path("/hello")
class ExampleResource {

    @Channel("kafkatest-out")
    lateinit var emitter: Emitter<String>

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(): String {
        emitter.send("Hello aufgerufen um ${Instant.now()}")
        return "Hello from Quarkus REST"
    }
}