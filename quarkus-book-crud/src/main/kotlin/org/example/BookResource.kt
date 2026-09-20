package org.example

import jakarta.persistence.PersistenceException
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
import org.eclipse.microprofile.rest.client.inject.RestClient

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BookResource {

    @RestClient
    lateinit var authorClient: AuthorClient

    @GET
    fun list(@QueryParam("search") search: String?): List<BookDto> {
        val books = if (search.isNullOrBlank()) Book.listAll() else Book.search(search)
        return books.map { toDto(it) }
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): BookDto {
        val book = Book.findById(id) ?: throw NotFoundException()
        return toDto(book)
    }

    @POST
    @Transactional
    fun create(input: BookInput, @Context uriInfo: UriInfo): Response {
        if (input.title.isBlank()) throw badRequest("Titel darf nicht leer sein")
        val authorIds = validateAuthorIds(input.authorIds)
        val isbn = normalizeAndValidateIsbn(input.isbn)

        val book = Book()
        book.title = input.title
        book.isbn = isbn
        book.authorIds = authorIds.toMutableSet()
        persistOrConflict(book, isbn)

        val location = uriInfo.absolutePathBuilder.path(book.id.toString()).build()
        return Response.created(location).entity(toDto(book)).build()
    }

    @PUT
    @Path("/{id}")
    @Transactional
    fun update(@PathParam("id") id: Long, input: BookInput): BookDto {
        val book = Book.findById(id) ?: throw NotFoundException()
        if (input.title.isBlank()) throw badRequest("Titel darf nicht leer sein")
        val authorIds = validateAuthorIds(input.authorIds)
        val isbn = normalizeAndValidateIsbn(input.isbn)

        book.title = input.title
        book.isbn = isbn
        book.authorIds = authorIds.toMutableSet()
        persistOrConflict(book, isbn)

        return toDto(book)
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        if (!Book.deleteById(id)) throw NotFoundException()
        return Response.noContent().build()
    }

    private fun persistOrConflict(book: Book, isbn: String) {
        try {
            book.persistAndFlush()
        } catch (e: PersistenceException) {
            throw conflict("ISBN bereits vergeben: $isbn")
        }
    }

    private fun toDto(book: Book): BookDto {
        val authors = book.authorIds.map { authorId ->
            val author = fetchAuthor(authorId)
            if (author != null) {
                AuthorSummary(id = authorId, firstName = author.firstName, lastName = author.lastName)
            } else {
                AuthorSummary(id = authorId, firstName = "unbekannt", lastName = "(gelöscht)")
            }
        }
        return BookDto(id = book.id!!, title = book.title, isbn = book.isbn, authors = authors)
    }

    private fun fetchAuthor(id: Long): AuthorClientDto? =
        try {
            authorClient.getAuthor(id)
        } catch (e: WebApplicationException) {
            null
        }

    private fun validateAuthorIds(authorIds: List<Long>): List<Long> {
        if (authorIds.isEmpty()) {
            throw badRequest("Ein Buch benötigt mindestens einen Autor")
        }
        val distinct = authorIds.distinct()
        val invalid = distinct.filter { fetchAuthor(it) == null }
        if (invalid.isNotEmpty()) {
            throw badRequest("Unbekannte Autoren-IDs: ${invalid.joinToString(", ")}")
        }
        return distinct
    }

    private fun normalizeAndValidateIsbn(raw: String): String {
        val normalized = raw.replace("-", "").replace(" ", "")
        if (!ISBN_PATTERN.matches(normalized)) {
            throw badRequest("ISBN muss aus 10 oder 13 Ziffern bestehen (ISBN-10 darf mit X enden)")
        }
        return normalized
    }

    private fun badRequest(message: String) = WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).entity(ErrorMessage(message)).build()
    )

    private fun conflict(message: String) = WebApplicationException(
        Response.status(Response.Status.CONFLICT).entity(ErrorMessage(message)).build()
    )

    companion object {
        private val ISBN_PATTERN = Regex("^(\\d{9}[\\dXx]|\\d{13})$")
    }
}
