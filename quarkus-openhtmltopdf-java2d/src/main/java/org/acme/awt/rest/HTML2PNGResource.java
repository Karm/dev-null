package org.acme.awt.rest;

import com.openhtmltopdf.java2d.api.BufferedImagePageProcessor;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Path("/html2png")
public class HTML2PNGResource {

    @POST
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response html2png(InputStream htmlInputStream) throws IOException {
        // WARNING:
        // Karm: Note that I have no idea what the library does,
        // I have never used it and this might be a very bad idea, e.g.
        // due to blocking the thread, memory leaks, etc.
        // This whole Quarkus project is merely a showcase of how to
        // integrate the library with native-image.
        final Java2DRendererBuilder builder = new Java2DRendererBuilder();
        builder.useEnvironmentFonts(true);
        builder.withHtmlContent(new String(htmlInputStream.readAllBytes()), "");
        builder.useFastMode();
        final BufferedImagePageProcessor bufferedImagePageProcessor =
                new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 1.0);
        builder.toSinglePage(bufferedImagePageProcessor);
        builder.runFirstPage();
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImagePageProcessor.getPageImages().get(0), "PNG", bos);
            return Response.ok(bos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"output.png\"")
                    .build();
        }
    }
}
