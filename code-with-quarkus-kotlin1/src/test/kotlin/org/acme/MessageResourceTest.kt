package org.acme

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import java.time.LocalDate

@QuarkusTest
class MessageResourceTest {

    @Test
    fun testCrudLifecycle() {
        val id = given()
            .contentType("application/json")
            .body("""{"text":"Erste Nachricht"}""")
            .`when`().post("/messages")
            .then()
            .statusCode(201)
            .body("text", equalTo("Erste Nachricht"))
            .body("createdAt", notNullValue())
            .body("updatedAt", notNullValue())
            .extract().path<Int>("id")

        given()
            .`when`().get("/messages/$id")
            .then()
            .statusCode(200)
            .body("text", equalTo("Erste Nachricht"))

        given()
            .contentType("application/json")
            .body("""{"text":"Geänderte Nachricht"}""")
            .`when`().put("/messages/$id")
            .then()
            .statusCode(200)
            .body("text", equalTo("Geänderte Nachricht"))

        given()
            .`when`().delete("/messages/$id")
            .then()
            .statusCode(204)

        given()
            .`when`().get("/messages/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun testGetUnknownIdReturns404() {
        given()
            .`when`().get("/messages/999999")
            .then()
            .statusCode(404)
    }

    @Test
    fun testUpdateUnknownIdReturns404() {
        given()
            .contentType("application/json")
            .body("""{"text":"Egal"}""")
            .`when`().put("/messages/999999")
            .then()
            .statusCode(404)
    }

    @Test
    fun testDeleteUnknownIdReturns404() {
        given()
            .`when`().delete("/messages/999999")
            .then()
            .statusCode(404)
    }

    @Test
    fun testBlankTextRejected() {
        given()
            .contentType("application/json")
            .body("""{"text":"   "}""")
            .`when`().post("/messages")
            .then()
            .statusCode(400)
    }

    @Test
    fun testSearchByText() {
        given()
            .contentType("application/json")
            .body("""{"text":"Fliegender Fisch"}""")
            .`when`().post("/messages")
            .then().statusCode(201)

        given()
            .contentType("application/json")
            .body("""{"text":"Schwimmender Vogel"}""")
            .`when`().post("/messages")
            .then().statusCode(201)

        given()
            .queryParam("search", "fliegender")
            .`when`().get("/messages")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("text", hasItem("Fliegender Fisch"))
    }

    @Test
    fun testFilterByCreatedDateRange() {
        val id = given()
            .contentType("application/json")
            .body("""{"text":"Heutige Nachricht"}""")
            .`when`().post("/messages")
            .then()
            .statusCode(201)
            .extract().path<Int>("id")

        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val yesterday = today.minusDays(1)

        given()
            .queryParam("createdFrom", today.toString())
            .queryParam("createdTo", today.toString())
            .`when`().get("/messages")
            .then()
            .statusCode(200)
            .body("id", hasItem(id))

        given()
            .queryParam("createdFrom", tomorrow.toString())
            .`when`().get("/messages")
            .then()
            .statusCode(200)
            .body("id", not(hasItem(id)))

        given()
            .queryParam("createdTo", yesterday.toString())
            .`when`().get("/messages")
            .then()
            .statusCode(200)
            .body("id", not(hasItem(id)))
    }

    @Test
    fun testInvalidDateFilterRejected() {
        given()
            .queryParam("createdFrom", "not-a-date")
            .`when`().get("/messages")
            .then()
            .statusCode(400)
    }
}
