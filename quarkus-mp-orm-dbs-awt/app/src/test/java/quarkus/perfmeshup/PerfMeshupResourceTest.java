package quarkus.perfmeshup;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import quarkus.pdfbox.PDFBoxResourceTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class PerfMeshupResourceTest {

    private static final String CACHED_XML_PATH = "/tmp/employee-profiles-test.xml";
    private static final String CACHED_PDF_PATH = "/tmp/employee-profiles-test.pdf";
    private static final int EMPLOYEES = 383; // up to what is in payloads_base64.txt

    @Test
    public void testPerfMeshupEndpoint() throws IOException {
        final String xmlPayload = generateEmployeeProfilesXml();
        // store the XML for inspection
        try (FileWriter writer = new FileWriter(CACHED_XML_PATH)) {
            writer.write(xmlPayload);
        }
        System.out.println("XML payload cached at: " + CACHED_XML_PATH);
        final byte[] pdfBytes = given()
                .contentType(ContentType.XML)
                .body(xmlPayload)
                .when()
                .post("/perfMeshup")
                .then()
                .statusCode(200)
                .contentType("application/pdf")
                .header("Content-Disposition", notNullValue())
                .extract()
                .asByteArray();
        // store PDF for inspection
        try (FileOutputStream fos = new FileOutputStream(CACHED_PDF_PATH)) {
            fos.write(pdfBytes);
        }
        System.out.println("PDF response cached at: " + CACHED_PDF_PATH);
    }

    private static String generateEmployeeProfilesXml() throws IOException {
        final List<String> base64Lines = Files.readAllLines(
                new File(Objects.requireNonNull(PDFBoxResourceTest.class.getResource("/payloads_base64.txt"))
                        .getPath()).toPath());
        if (base64Lines.size() < EMPLOYEES) {
            throw new IllegalStateException("Expected at least " + EMPLOYEES + " base64 lines, found: " + base64Lines.size());
        }
        final StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<EmployeeProfiles>\n");
        for (int i = 0; i < EMPLOYEES; i++) {
            final String employeeId = String.format("EMP%05d", i + 1);
            final String firstName = (i % 2 == 0) ? "John" : "Jane";
            final String lastName = "Doe";
            final String fullName = firstName + " " + lastName + " " + (i + 1);
            final String email = firstName.toLowerCase() + ".doe" + (i + 1) + "@example.com";
            final String street = (i + 1) + " Main Street";
            final String city = "City" + ((i % 10) + 1);
            final String clearanceLevel = getClearanceLevel(i);
            final String base64Image = base64Lines.get(i).trim();
            xml.append("  <EmployeeProfile>\n");
            xml.append("    <EmployeeId>").append(employeeId).append("</EmployeeId>\n");
            xml.append("    <Demographics>\n");
            xml.append("      <FullName>").append(fullName).append("</FullName>\n");
            xml.append("      <ContactInfo>\n");
            xml.append("        <Email>").append(email).append("</Email>\n");
            xml.append("        <Address>\n");
            xml.append("          <Street>").append(street).append("</Street>\n");
            xml.append("          <City>").append(city).append("</City>\n");
            xml.append("        </Address>\n");
            xml.append("      </ContactInfo>\n");
            xml.append("    </Demographics>\n");
            xml.append("    <Security>\n");
            xml.append("      <Clearance>\n");
            xml.append("        <Level>").append(clearanceLevel).append("</Level>\n");
            xml.append("        <BackgroundCheck>\n");
            xml.append("          <Passed>").append(i % 3 != 0).append("</Passed>\n");
            xml.append("          <Date>2024-01-").append(String.format("%02d", (i % 28) + 1)).append("</Date>\n");
            xml.append("        </BackgroundCheck>\n");
            xml.append("      </Clearance>\n");
            xml.append("    </Security>\n");
            xml.append("    <ProfilePictureBase64>").append(base64Image).append("</ProfilePictureBase64>\n");
            xml.append("  </EmployeeProfile>\n");
        }
        xml.append("</EmployeeProfiles>\n");
        return xml.toString();
    }

    private static String getClearanceLevel(int index) {
        return switch (index % 5) {
            case 0 -> "PUBLIC";
            case 1 -> "CONFIDENTIAL";
            case 2 -> "SECRET";
            case 3 -> "TOP_SECRET";
            case 4 -> "COSMIC_TOP_SECRET";
            default -> "PUBLIC";
        };
    }
}
