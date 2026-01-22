package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.concurrent.atomic.AtomicInteger;

@Path("/hello")
public class GreetingResource {

    @Inject
    Vertx vertx;

    public static final int TASKS_PER_REQUEST = 50;

    /**
     * Intentionally weird to try to show the difference between LinkedBlockingDeque and MpscQueue in Netty.
     *
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> hello() {
        final Context ioContext = Vertx.currentContext();
        return Uni.createFrom().voidItem()
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(v -> Uni.createFrom().emitter(emitter -> {
                    AtomicInteger counter = new AtomicInteger(0);
                    for (int i = 0; i < TASKS_PER_REQUEST; i++) {
                        ioContext.runOnContext(() -> {
                            if (counter.incrementAndGet() == TASKS_PER_REQUEST) {
                                emitter.complete("Brrrrrrrrr " + TASKS_PER_REQUEST + " tasks.");
                            }
                        });
                    }
                }));
    }
}
