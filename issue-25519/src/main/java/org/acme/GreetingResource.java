package org.acme;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class GreetingResource {

    @ConfigProperty(name = "my.property")
    String property;

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return property;
    }

    @GET
    @Path("/users")
    @Produces(MediaType.TEXT_PLAIN)
    public String users() {
        return User.listAll().toString();
    }

}