package org.example

import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthorResource {

    @GET
    fun list(@QueryParam("search") search: String?): List<AuthorDto> {
        val authors = if (search.isNullOrBlank()) Author.listAll() else Author.search(search)
        return authors.map { it.toDto() }
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): AuthorDto {
        val author = Author.findById(id) ?: throw NotFoundException()
        return author.toDto()
    }

    @POST
    @Transactional
    fun create(input: AuthorInput, @Context uriInfo: UriInfo): Response {
        validateNames(input)
        val birthDate = parseBirthDate(input.birthDate)

        val author = Author()
        author.firstName = input.firstName
        author.lastName = input.lastName
        author.birthDate = birthDate
        author.persist()

        val location = uriInfo.absolutePathBuilder.path(author.id.toString()).build()
        return Response.created(location).entity(author.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun update(@PathParam("id") id: Long, input: AuthorInput): AuthorDto {
        val author = Author.findById(id) ?: throw NotFoundException()
        validateNames(input)
        val birthDate = parseBirthDate(input.birthDate)

        author.firstName = input.firstName
        author.lastName = input.lastName
        author.birthDate = birthDate
        return author.toDto()
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        if (!Author.deleteById(id)) throw NotFoundException()
        return Response.noContent().build()
    }

    private fun validateNames(input: AuthorInput) {
        if (input.firstName.isBlank() || input.lastName.isBlank()) {
            throw badRequest("Vorname und Nachname dürfen nicht leer sein")
        }
    }

    private fun parseBirthDate(value: String): LocalDate {
        try {
            return LocalDate.parse(value)
        } catch (e: DateTimeParseException) {
            throw badRequest("birthDate muss im Format yyyy-MM-dd angegeben werden")
        }
    }

    private fun badRequest(message: String) = WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).entity(ErrorMessage(message)).build()
    )
}
