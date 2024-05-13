package io.quarkus.brotli4j;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class Brotli4JHttpTest {

    private final static String BROTLI_ENCODING = "br";

    @Test
    public void assertBrotli4JCompression() {
        byte[] responseBody = given()
                .when()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.ACCEPT_ENCODING, BROTLI_ENCODING)
                .get("/compression/text")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_ENCODING, BROTLI_ENCODING)
                .extract().response().asByteArray();
        int compressedContentLength = responseBody.length;
        System.out.println("Compressed: " + compressedContentLength +
                " bytes, original: " + Brotli4JResource.DEFAULT_TEXT_PLAIN.length() + " bytes.");
        assertTrue(compressedContentLength < Brotli4JResource.DEFAULT_TEXT_PLAIN.length());
    }
}
