package org.example

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.ws.rs.NotFoundException
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.eclipse.microprofile.rest.client.inject.RestClient

@QuarkusTest
class BookResourceTest {

    @InjectMock
    @RestClient
    lateinit var authorClient: AuthorClient

    @BeforeEach
    fun setup() {
        `when`(authorClient.getAuthor(1L)).thenReturn(
            AuthorClientDto(1L, "Ada", "Lovelace", "1815-12-10")
        )
        `when`(authorClient.getAuthor(2L)).thenReturn(
            AuthorClientDto(2L, "Alan", "Turing", "1912-06-23")
        )
        `when`(authorClient.getAuthor(999L)).thenThrow(NotFoundException())
    }

    @Test
    fun testCrudLifecycle() {
        val id = given()
            .contentType(ContentType.JSON)
            .body("""{"title":"The Analytical Engine","isbn":"9783161484100","authorIds":[1]}""")
            .`when`().post("/books")
            .then()
            .statusCode(201)
            .body("title", equalTo("The Analytical Engine"))
            .body("authors[0].lastName", equalTo("Lovelace"))
            .extract().path<Int>("id")

        given()
            .`when`().get("/books/$id")
            .then()
            .statusCode(200)
            .body("isbn", equalTo("9783161484100"))

        given()
            .queryParam("search", "analytical")
            .`when`().get("/books")
            .then()
            .statusCode(200)
            .body("size()", equalTo(1))

        given()
            .contentType(ContentType.JSON)
            .body("""{"title":"On Computable Numbers","isbn":"9783161484100","authorIds":[1,2]}""")
            .`when`().put("/books/$id")
            .then()
            .statusCode(200)
            .body("title", equalTo("On Computable Numbers"))
            .body("authors.size()", equalTo(2))

        given()
            .`when`().delete("/books/$id")
            .then()
            .statusCode(204)

        given()
            .`when`().get("/books/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun testMissingAuthorsRejected() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"title":"Ghostwritten","isbn":"9783161484101","authorIds":[999]}""")
            .`when`().post("/books")
            .then()
            .statusCode(400)
    }

    @Test
    fun testEmptyAuthorListRejected() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"title":"No Author","isbn":"9783161484102","authorIds":[]}""")
            .`when`().post("/books")
            .then()
            .statusCode(400)
    }

    @Test
    fun testDuplicateIsbnRejected() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"title":"First","isbn":"9783161484199","authorIds":[1]}""")
            .`when`().post("/books")
            .then()
            .statusCode(201)

        given()
            .contentType(ContentType.JSON)
            .body("""{"title":"Second","isbn":"9783161484199","authorIds":[1]}""")
            .`when`().post("/books")
            .then()
            .statusCode(409)
    }
}
