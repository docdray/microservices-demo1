package org.acme

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RestClient

@Path("/world")
class WorldResource {

    @RestClient
    lateinit var homeClient: HomeClient

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun world(@QueryParam("name") name: String?): String {
        return homeClient.getHome(name)
    }
}