package org.acme

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("/home")
class HomeResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun home(@QueryParam("name") name: String?): String {
        return if (name.isNullOrBlank()) {
            "mi casa es su casa"
        } else {
            "mi casa es su casa, $name"
        }
    }
}