package org.acme

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class HomeResourceTest {

    @Test
    fun testHomeWithoutName() {
        given()
            .`when`().get("/home")
            .then()
            .statusCode(200)
            .body(`is`("mi casa es su casa"))
    }

    @Test
    fun testHomeWithBlankNameIsTreatedAsMissing() {
        given()
            .queryParam("name", "")
            .`when`().get("/home")
            .then()
            .statusCode(200)
            .body(`is`("mi casa es su casa"))
    }

    @Test
    fun testHomeWithName() {
        given()
            .queryParam("name", "Ada")
            .`when`().get("/home")
            .then()
            .statusCode(200)
            .body(`is`("mi casa es su casa, Ada"))
    }
}
