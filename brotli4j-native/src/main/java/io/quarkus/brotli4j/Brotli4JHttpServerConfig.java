package io.quarkus.brotli4j;

import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Brotli4JHttpServerConfig implements HttpServerOptionsCustomizer {

    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        options.addCompressor(StandardCompressionOptions.brotli());
    }

    @Override
    public void customizeHttpsServer(HttpServerOptions options) {
        options.addCompressor(StandardCompressionOptions.brotli());
    }
}
