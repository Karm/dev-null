package org.acme.lambda;

//import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("/azurite")
@ApplicationScoped
public class RESTEndpoint {

    @Inject
    QueueClient queueClient;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response post(String text) {
        try {
            return Response.accepted().entity(sendMetadataToAzureStorage(text)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    public String sendMetadataToAzureStorage(String input) {
        return queueClient.sendMessage(Base64.getEncoder()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8))).getInsertionTime().toString();
    }
}
