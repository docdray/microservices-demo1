package org.example

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@QuarkusTest
class AuthorResourceTest {

    @Test
    fun testCrudLifecycle() {
        val id = given()
            .contentType(ContentType.JSON)
            .body("""{"firstName":"Ada","lastName":"Lovelace","birthDate":"1815-12-10"}""")
            .`when`().post("/authors")
            .then()
            .statusCode(201)
            .body("firstName", equalTo("Ada"))
            .extract().path<Int>("id")

        given()
            .`when`().get("/authors/$id")
            .then()
            .statusCode(200)
            .body("lastName", equalTo("Lovelace"))

        given()
            .queryParam("search", "lovelace")
            .`when`().get("/authors")
            .then()
            .statusCode(200)
            .body("size()", equalTo(1))

        given()
            .contentType(ContentType.JSON)
            .body("""{"firstName":"Ada","lastName":"King","birthDate":"1815-12-10"}""")
            .`when`().put("/authors/$id")
            .then()
            .statusCode(200)
            .body("lastName", equalTo("King"))

        given()
            .`when`().delete("/authors/$id")
            .then()
            .statusCode(204)

        given()
            .`when`().get("/authors/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun testNotFoundForUnknownId() {
        given()
            .`when`().get("/authors/999999")
            .then()
            .statusCode(404)

        given()
            .contentType(ContentType.JSON)
            .body("""{"firstName":"Ada","lastName":"Lovelace","birthDate":"1815-12-10"}""")
            .`when`().put("/authors/999999")
            .then()
            .statusCode(404)

        given()
            .`when`().delete("/authors/999999")
            .then()
            .statusCode(404)
    }

    @Test
    fun testValidation() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"firstName":"","lastName":"Lovelace","birthDate":"1815-12-10"}""")
            .`when`().post("/authors")
            .then()
            .statusCode(400)

        given()
            .contentType(ContentType.JSON)
            .body("""{"firstName":"Ada","lastName":"Lovelace","birthDate":"not-a-date"}""")
            .`when`().post("/authors")
            .then()
            .statusCode(400)
    }
}
