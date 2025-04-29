package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Path("/")
public class GreetingResource {

    @GET
    @Path("/resource/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    // Don't try this at home :)
    public Response get(@PathParam(value = "key") String key) {
        final String resourcePath = key.replace("%2F", "/");
        try (InputStream is = Objects.requireNonNull(GreetingResource.class.getResourceAsStream("/" + resourcePath))) {
            return Response.ok(new String(is.readAllBytes(), StandardCharsets.UTF_8)).build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Resource not found: " + resourcePath)
                    .build();
        }
    }
}
