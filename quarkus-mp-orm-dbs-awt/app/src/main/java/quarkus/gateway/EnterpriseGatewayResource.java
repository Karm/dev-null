package quarkus.gateway;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import quarkus.client.Service;
import quarkus.client.SummaryDto;
import quarkus.orm.db1.DB1Entity;
import quarkus.orm.db2.DB2Entity;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Path("/gateway/profile")
public class EnterpriseGatewayResource {

    @Inject
    @RestClient
    Service restClient;

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public String processEmployeeProfile(@Valid EmployeeProfileXml profile) throws Exception {
        final byte[] imageBytes = Base64.getDecoder().decode(profile.profilePictureBase64);
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        final BufferedImage watermarkedImage = applyWatermark(image);
        // Save text data to DB1
        QuarkusTransaction.requiringNew().run(() -> {
            final DB1Entity db1Entity = new DB1Entity();
            db1Entity.employeeId = profile.employeeId;
            db1Entity.fullName = profile.demographics.fullName;
            db1Entity.field = "Enterprise Profile";
            db1Entity.persist();
        });
        // Save image metadata to DB2
        QuarkusTransaction.requiringNew().run(() -> {
            final DB2Entity db2Entity = new DB2Entity();
            db2Entity.employeeId = profile.employeeId;
            db2Entity.hasWatermarkedImage = (watermarkedImage != null);
            db2Entity.field = "Image processed";
            db2Entity.persist();
        });
        final SummaryDto summary = new SummaryDto();
        summary.employeeId = profile.employeeId;
        summary.status = "PROCESSED";
        summary.processed = true;
        return restClient.sendSummary(summary);
    }

    private static BufferedImage applyWatermark(BufferedImage image) {
        if (image == null || image.getWidth() < 5 || image.getHeight() < 5) {
            return image;
        }
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.RED);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.drawString("LOL LOL LOL", 10, 30);
        g.dispose();
        return image;
    }
}
