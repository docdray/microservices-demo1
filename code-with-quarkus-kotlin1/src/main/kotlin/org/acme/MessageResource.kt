package org.acme

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
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class MessageResource {

    @GET
    fun list(
        @QueryParam("search") search: String?,
        @QueryParam("createdFrom") createdFrom: String?,
        @QueryParam("createdTo") createdTo: String?,
        @QueryParam("updatedFrom") updatedFrom: String?,
        @QueryParam("updatedTo") updatedTo: String?
    ): List<MessageDto> {
        val messages = Message.search(
            text = search,
            createdFrom = parseBoundary(createdFrom, "createdFrom", endOfDay = false),
            createdTo = parseBoundary(createdTo, "createdTo", endOfDay = true),
            updatedFrom = parseBoundary(updatedFrom, "updatedFrom", endOfDay = false),
            updatedTo = parseBoundary(updatedTo, "updatedTo", endOfDay = true)
        )
        return messages.map { it.toDto() }
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): MessageDto {
        val message = Message.findById(id) ?: throw NotFoundException()
        return message.toDto()
    }

    @POST
    @Transactional
    fun create(input: MessageInput, @Context uriInfo: UriInfo): Response {
        validateText(input)

        val message = Message()
        message.text = input.text
        message.persist()

        val location = uriInfo.absolutePathBuilder.path(message.id.toString()).build()
        return Response.created(location).entity(message.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun update(@PathParam("id") id: Long, input: MessageInput): MessageDto {
        val message = Message.findById(id) ?: throw NotFoundException()
        validateText(input)

        message.text = input.text
        return message.toDto()
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        if (!Message.deleteById(id)) throw NotFoundException()
        return Response.noContent().build()
    }

    private fun validateText(input: MessageInput) {
        if (input.text.isBlank()) {
            throw badRequest("Text darf nicht leer sein")
        }
    }

    private fun parseBoundary(value: String?, field: String, endOfDay: Boolean): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        try {
            return LocalDateTime.parse(value)
        } catch (e: DateTimeParseException) {
            // war kein Datum mit Uhrzeit, als reines Datum versuchen
        }
        try {
            val date = LocalDate.parse(value)
            return if (endOfDay) date.atTime(23, 59, 59, 999_999_999) else date.atStartOfDay()
        } catch (e: DateTimeParseException) {
            throw badRequest("$field muss im Format yyyy-MM-dd oder yyyy-MM-ddTHH:mm:ss angegeben werden")
        }
    }

    private fun badRequest(message: String) = WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).entity(ErrorMessage(message)).build()
    )
}
