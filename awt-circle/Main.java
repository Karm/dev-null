import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// $ javac Main.java
// $ java -agentlib:native-image-agent=config-output-dir=native-image Main
// $ native-image --no-fallback --link-at-build-time Main
// $ rm image.png
// $ ./main
public class Main {
    public static void main(String[] args) throws IOException, FontFormatException {
        final BufferedImage i = new BufferedImage(320, 200, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = (Graphics2D) i.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 320, 200);
        g.setColor(Color.RED);
        g.fillOval(0, 0, 320, 200);
        g.dispose();
        ImageIO.write(i, "PNG", new File("image.png"));
    }
}
