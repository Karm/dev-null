package org.acme.awt.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.BINARY;

@QuarkusTest
public class HTML2PNGResourceTest {
    @Test
    public void testHTML2PNG() throws IOException {
        final String html;
        try (InputStream is = HTML2PNGResourceTest.class.getResourceAsStream("/test-html.html")) {
            // Port that serves the test image called from HTML is different in test profile.
            html = new String(is.readAllBytes()).replace("8080", "8081");
        }
        final byte[] imgBytes = given()
                .accept(BINARY)
                .contentType(ContentType.HTML)
                .body(html)
                .when()
                .post("/html2png")
                .asByteArray();
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
        Assertions.assertNotNull(image, "The image returned is not a valid, known format, e.g. PNG");
        final int[] dimensions = { 595, 1586 };
        Assertions.assertTrue(image.getWidth() == dimensions[0] && image.getHeight() == dimensions[1],
                String.format("Image's expected dimension is %d x %d, but was %d x %d.",
                        dimensions[0], dimensions[1], image.getWidth(), image.getHeight()));
        final int[] pixel = new int[4]; //4BYTE RGBA
        image.getData().getPixel(105, 156, pixel);
        Assertions.assertTrue(pixel[2] > 200, "There should have been more blue. The image is probably not correct.");
        image.getData().getPixel(248, 942, pixel);
        Assertions.assertTrue(pixel[0] > 200, "There should have been more red. The image is probably not correct.");
        image.getData().getPixel(145, 1453, pixel);
        Assertions.assertTrue(pixel[2] > 250, "There should have been more blue. The image is probably not correct.");
        image.getData().getPixel(204, 1453, pixel);
        Assertions.assertTrue(pixel[0] > 250, "There should have been more red. The image is probably not correct.");
    }
}
