package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.concurrent.Executor;

@Path("/hello")
public class GreetingResource {

    // Trying not to kill native-image with GC, reusing handler:
    private static final Handler<Void> NOOP = v -> {};
    private final Executor pool = Infrastructure.getDefaultWorkerPool();
    private static final int ITERATIONS = 40;

    @GET
    public Uni<String> hello() {
        final Context c = Vertx.currentContext();
        return Uni.createFrom().emitter(em -> pool.execute(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                c.runOnContext(NOOP);
            }
            em.complete("Hahahahahahahaha.");
        }));
    }
}
