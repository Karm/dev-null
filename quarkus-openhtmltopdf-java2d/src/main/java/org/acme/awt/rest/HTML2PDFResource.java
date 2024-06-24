package org.acme.awt.rest;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Path("/html2pdf")
public class HTML2PDFResource {

    @POST
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response html2pdf(InputStream htmlInputStream) throws IOException {
        // WARNING:
        // Karm: Note that I have no idea what the library does,
        // I have never used it and this might be a very bad idea, e.g.
        // due to blocking the thread, memory leaks, etc.
        // This whole Quarkus project is merely a showcase of how to
        // integrate the library with native-image.
        final PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_A);
        // This really should not be done on each request
        builder.useFont(() -> Objects.requireNonNull(HTML2PDFResource.class
                        .getResourceAsStream("/MyFreeSerif.ttf"),
                "MyFreeSerif.ttf not found."), "Serif");
        builder.withHtmlContent(new String(htmlInputStream.readAllBytes()), "");
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            builder.toStream(bos);
            builder.run();
            return Response.ok(bos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"output.png\"")
                    .build();
        }
    }
}
