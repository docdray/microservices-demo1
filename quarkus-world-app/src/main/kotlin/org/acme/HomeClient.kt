package org.acme

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "home-client")
@Path("/home")
interface HomeClient {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun getHome(@QueryParam("name") name: String?): String
}