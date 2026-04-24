package quarkus.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

@Path("/client/service")
public class ServiceController {

    @GET
    @Path("/{parameter}")
    public String doSomething(@PathParam("parameter") String parameter) {
        return String.format("Processed parameter value '%s'", parameter);
    }

    @POST
    @Path("/summary")
    @Consumes(MediaType.APPLICATION_JSON)
    public String sendSummary(SummaryDto summary) {
        return "ACK: " + summary.employeeId;
    }
}
