package org.acme.awt.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.BINARY;

@QuarkusTest
public class HTML2PDFResourceTest {
    @Test
    public void testHTML2PDF() throws IOException {
        final String html;
        try (InputStream is = HTML2PDFResourceTest.class.getResourceAsStream("/test-html.html")) {
            // Port that serves the test image called from HTML is different in test profile.
            html = new String(is.readAllBytes()).replace("8080", "8081");
        }
        final byte[] pdfBytes = given()
                .accept(BINARY)
                .contentType(ContentType.HTML)
                .body(html)
                .when()
                .post("/html2pdf")
                .asByteArray();

        // We test the PDF looks by converting it to an image
        // and sampling some pixels for places where letters
        // or colors are expected.
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            final PDFRenderer pdfRenderer = new PDFRenderer(document);
            // There are two pages, but we only need the first one
            final BufferedImage image = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);

            //ImageIO.write(image, "png", new File("/tmp/test.png"));

            Assertions.assertNotNull(image, "The image returned is not a valid, known format, e.g. PNG");
            final int[] dimensions = { 595, 841 };
            Assertions.assertTrue(image.getWidth() == dimensions[0] && image.getHeight() == dimensions[1],
                    String.format("Image's expected dimension is %d x %d, but was %d x %d.",
                            dimensions[0], dimensions[1], image.getWidth(), image.getHeight()));
            final int[] pixel = new int[3]; //4BYTE RGB
            image.getData().getPixel(119, 764, pixel);
            Assertions.assertTrue(pixel[2] > 200,
                    "There should have been more blue. The image is probably not correct.");
            image.getData().getPixel(315, 449, pixel);
            Assertions.assertTrue(pixel[0] > 250,
                    "There should have been more red. The image is probably not correct.");
            image.getData().getPixel(276, 132, pixel);
            Assertions.assertTrue(pixel[2] > 250,
                    "There should have been more blue. The image is probably not correct.");
            image.getData().getPixel(88, 60, pixel);
            Assertions.assertTrue((pixel[0] < 5) && (pixel[1] < 5) && (pixel[2] < 5),
                    "There should have been a black pixel in a letter.");
        }
    }
}
