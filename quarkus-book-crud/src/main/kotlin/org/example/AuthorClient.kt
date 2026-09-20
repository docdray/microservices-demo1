package org.example

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

data class AuthorClientDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

@RegisterRestClient(configKey = "author-client")
@Path("/authors")
interface AuthorClient {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAuthor(@PathParam("id") id: Long): AuthorClientDto
}
