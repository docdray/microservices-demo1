package org.acme

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

@QuarkusTest
class WorldResourceTest {

    @InjectMock
    @RestClient
    lateinit var homeClient: HomeClient

    @BeforeEach
    fun setup() {
        `when`(homeClient.getHome(null)).thenReturn("mi casa es su casa")
        `when`(homeClient.getHome("Ada")).thenReturn("mi casa es su casa, Ada")
    }

    @Test
    fun testWorldWithoutNameDelegatesToHomeClient() {
        given()
            .`when`().get("/world")
            .then()
            .statusCode(200)
            .body(`is`("mi casa es su casa"))
    }

    @Test
    fun testWorldWithNameDelegatesToHomeClient() {
        given()
            .queryParam("name", "Ada")
            .`when`().get("/world")
            .then()
            .statusCode(200)
            .body(`is`("mi casa es su casa, Ada"))
    }
}
