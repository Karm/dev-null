package org.acme;

import io.netty.channel.EventLoopGroup;
import io.quarkus.netty.MainEventLoopGroup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class EventLoopProbe {

    @Inject
    // TODO: Makes sense?
    @MainEventLoopGroup
    EventLoopGroup group;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("\nAPP RUNTIME: Inspecting the RUNNING NioEventLoop instance...");
        try {
            final boolean ok = (boolean) Class.forName("io.netty.channel.epoll.Epoll")
                    .getMethod("isAvailable").invoke(null);
            System.out.println("APP RUNTIME: Netty Transport: " + (ok ? "Epoll" : "NIO"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("APP RUNTIME: Netty Transport: NIO");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            final Object eventLoop = group.next();
            System.out.println("    eventLoop.getClass().getName(): " + eventLoop.getClass().getName());
            Class<?> clazz = eventLoop.getClass();
            Field queueField = null;
            while (clazz != null) {
                try {
                    queueField = clazz.getDeclaredField("taskQueue");
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (queueField == null) {
                System.out.println(" FAILURE: no 'taskQueue'.");
                return;
            }
            queueField.setAccessible(true);
            final String queueImpl = queueField.get(eventLoop).getClass().getName();
            System.out.println("    Actual Queue Field: " + queueImpl);

            if (queueImpl.contains("LinkedBlockingDeque")) {
                System.out.println("  RESULT: SUBSTITUTED. (slower/safer queue)");
            } else if (queueImpl.contains("JCTools") || queueImpl.contains("Mpsc")) {
                System.out.println("  RESULT: OPTIMIZED. (faster, platform dependent)");
            } else {
                System.out.println("  RESULT: UNEXPECTED (" + queueImpl + ")");
            }
            System.out.println("--------------------------------------------------------------\n");

        } catch (Throwable t) {
            System.out.println(" CRASHED: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
