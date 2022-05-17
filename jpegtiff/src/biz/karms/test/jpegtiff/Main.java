package biz.karms.test.jpegtiff;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        final String[] filenames = new String[] { "test_jpeg.tiff", "test_jpeg_2.tiff" };
        for (final String filename : filenames) {
            try {
                final BufferedImage image = ImageIO.read(Objects.requireNonNull(Main.class.getResource(filename)).openStream());
                ImageIO.write(image, "PNG", Path.of(".", filename + ".png").toFile());
                System.out.println(filename + ": " + filename + ".png");
            } catch (IOException e) {
                System.out.println(filename + ": " + e.getMessage());
            }
        }
    }
}
