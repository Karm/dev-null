package quarkus.perfmeshup;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jboss.logging.Logger;
import quarkus.gateway.EmployeeProfileXml;
import quarkus.gateway.EmployeeProfiles;
import quarkus.orm.db1.DB1Entity;
import quarkus.orm.db2.DB2Entity;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Path("/perfMeshup")
public class PerfMeshupResource {

    private static final Logger LOG = Logger.getLogger(PerfMeshupResource.class);
    private static final int TARGET_SIZE = 100;

    @Inject
    @PersistenceUnit("db1")
    EntityManager db1EntityManager;

    @Inject
    @PersistenceUnit("db2")
    EntityManager db2EntityManager;

    /**
     * The idea is to stress XML deserialization and AWT via PDFBox
     *
     * @param employeeProfiles
     * @return
     */
    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_XML)
    @Produces("application/pdf")
    public Response processEmployeeProfiles(@Valid EmployeeProfiles employeeProfiles) {
        LOG.info("Starting perfMeshup processing for " + employeeProfiles.profiles.size() + " profiles");
        try {
            // This is a toy, test endpoint...
            clearDatabases();
            for (EmployeeProfileXml profile : employeeProfiles.profiles) {
                processProfileInDB1(profile);
                processProfileInDB2(profile);
            }
            final byte[] pdfBytes = generatePdf(employeeProfiles.profiles);
            LOG.info("Successfully processed " + employeeProfiles.profiles.size() + " profiles");
            return Response.ok(pdfBytes).header("Content-Disposition", "attachment; filename=employee-profiles.pdf").build();
        } catch (Exception e) {
            LOG.error("Error processing employee profiles", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build();
        }
    }

    private void clearDatabases() {
        db1EntityManager.createQuery("DELETE FROM DB1Entity").executeUpdate();
        db2EntityManager.createQuery("DELETE FROM DB2Entity").executeUpdate();
    }

    private void processProfileInDB1(EmployeeProfileXml profile) throws IOException {
        LOG.debug("Processing profile in DB1: " + profile.employeeId);
        // Decode and convert image to JPEG 100x100
        final byte[] jpegImage = convertToJpeg(profile.profilePictureBase64);
        final DB1Entity db1Entity = new DB1Entity();
        db1Entity.employeeId = profile.employeeId;
        db1Entity.fullName = profile.demographics != null ? profile.demographics.fullName : "Unknown";
        db1Entity.email = profile.demographics != null && profile.demographics.contactInfo != null
                ? profile.demographics.contactInfo.email
                : null;
        db1Entity.city = profile.demographics != null && profile.demographics.contactInfo != null && profile.demographics.contactInfo.address != null
                ? profile.demographics.contactInfo.address.city
                : null;
        db1Entity.clearanceLevel = profile.security != null && profile.security.clearance != null
                ? profile.security.clearance.level
                : null;
        db1Entity.profileImageJpeg = jpegImage;
        db1EntityManager.persist(db1Entity);
    }

    private void processProfileInDB2(EmployeeProfileXml profile) throws IOException {
        LOG.debug("Processing profile in DB2: " + profile.employeeId);
        // Decode and convert image to JPEG 100x100
        final byte[] jpegImage = convertToJpeg(profile.profilePictureBase64);
        final DB2Entity db2Entity = new DB2Entity();
        db2Entity.employeeId = profile.employeeId;
        db2Entity.fullName = profile.demographics != null ? profile.demographics.fullName : "Unknown";
        db2Entity.email = profile.demographics != null && profile.demographics.contactInfo != null
                ? profile.demographics.contactInfo.email
                : null;
        db2Entity.street = profile.demographics != null && profile.demographics.contactInfo != null && profile.demographics.contactInfo.address != null
                ? profile.demographics.contactInfo.address.street
                : null;
        db2Entity.hasWatermarkedImage = true;
        db2Entity.profileImageJpeg = jpegImage;
        db2EntityManager.persist(db2Entity);
    }

    private static byte[] convertToJpeg(String base64Image) throws IOException {
        final byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            LOG.warn(
                    "Failed to decode image, will create placeholder: " + e.getMessage() +
                            " The first 50 chars of the base64 was: " + base64Image.substring(
                            0, 50));
        }
        if (originalImage == null) {
            originalImage = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g = originalImage.createGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE);
            g.setColor(Color.DARK_GRAY);
            g.drawString("OMG, NOTHING", 20, 50);
            g.dispose();
        }
        // Create 100x100 JPEG, butchering aspect ratio, irrelevant for the test...
        final BufferedImage resizedImage = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, TARGET_SIZE, TARGET_SIZE, null);
        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        }
    }

    private byte[] generatePdf(java.util.List<EmployeeProfileXml> profiles) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Load our own font
            final PDFont font = PDType0Font.load(document, getClass().getClassLoader().getResourceAsStream("MyFreeSerif.ttf"));
            for (EmployeeProfileXml profile : profiles) {
                final PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float yPosition = 750;
                    contentStream.beginText();
                    contentStream.setFont(font, 16);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("Employee Profile");
                    contentStream.endText();
                    yPosition -= 30;
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText("ID: " + profile.employeeId);
                    contentStream.endText();
                    yPosition -= 20;
                    if (profile.demographics != null && profile.demographics.fullName != null) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, yPosition);
                        contentStream.showText("Name: " + profile.demographics.fullName);
                        contentStream.endText();
                        yPosition -= 20;
                    }
                    if (profile.demographics != null && profile.demographics.contactInfo != null) {
                        if (profile.demographics.contactInfo.email != null) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, yPosition);
                            contentStream.showText("Email: " + profile.demographics.contactInfo.email);
                            contentStream.endText();
                            yPosition -= 20;
                        }
                        if (profile.demographics.contactInfo.address != null) {
                            if (profile.demographics.contactInfo.address.city != null) {
                                contentStream.beginText();
                                contentStream.newLineAtOffset(50, yPosition);
                                contentStream.showText("City: " + profile.demographics.contactInfo.address.city);
                                contentStream.endText();
                                yPosition -= 20;
                            }
                        }
                    }
                    yPosition -= 20;
                    try {
                        final byte[] jpegImage = convertToJpeg(profile.profilePictureBase64);
                        final PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, jpegImage, "profile");
                        contentStream.drawImage(pdImage, 50, yPosition - 100, 100, 100);
                    } catch (Exception e) {
                        LOG.warn("Failed to add image for profile " + profile.employeeId, e);
                    }
                }
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                document.save(baos);
                return baos.toByteArray();
            }
        }
    }
}
