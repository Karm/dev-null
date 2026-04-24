package quarkus.gateway;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class EmployeeProfileResourceTest {

    @Test
    public void testProcessEmployeeProfile() {
        final String validXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <EmployeeProfile>
                    <EmployeeId>EMP-12345</EmployeeId>
                    <Demographics>
                        <FullName>John Doe</FullName>
                        <ContactInfo>
                            <Email>john.doe@example.com</Email>
                            <Address>
                                <Street>123 Main St</Street>
                                <City>Springfield</City>
                            </Address>
                        </ContactInfo>
                    </Demographics>
                    <Security>
                        <Clearance>
                            <Level>SECRET</Level>
                            <BackgroundCheck>
                                <Passed>true</Passed>
                                <Date>2024-01-15</Date>
                            </BackgroundCheck>
                        </Clearance>
                    </Security>
                    <ProfilePictureBase64>iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==</ProfilePictureBase64>
                </EmployeeProfile>
                """;
        given()
            .contentType(ContentType.XML)
            .body(validXml)
            .when()
            .post("/gateway/profile")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(containsString("ACK: EMP-12345"));
    }

    @Test
    public void testProcessEmployeeProfileWithInvalidXml() {
        final String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <EmployeeProfile>
                    <Demographics>
                        <FullName>Jane Doe</FullName>
                    </Demographics>
                    <ProfilePictureBase64>iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==</ProfilePictureBase64>
                </EmployeeProfile>
                """;
        given()
            .contentType(ContentType.XML)
            .body(invalidXml)
            .when()
            .post("/gateway/profile")
            .then()
            .statusCode(400);
    }

    @Test
    public void testProcessEmployeeProfileWithShortName() {
        // FullName too short - should fail @Size validation (min=2)
        final String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <EmployeeProfile>
                    <EmployeeId>EMP-99999</EmployeeId>
                    <Demographics>
                        <FullName>X</FullName>
                    </Demographics>
                    <ProfilePictureBase64>iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==</ProfilePictureBase64>
                </EmployeeProfile>
                """;
        given()
            .contentType(ContentType.XML)
            .body(invalidXml)
            .when()
            .post("/gateway/profile")
            .then()
            .statusCode(400);
    }
}
