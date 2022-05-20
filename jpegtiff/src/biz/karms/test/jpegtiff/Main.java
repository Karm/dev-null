package biz.karms.test.jpegtiff;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        final String[] filenames = new String[] {
                "test_1x1_jpeg.tiff",
                // Works. RGB. No transparency.
                "test_2x1_jpeg.tiff",
                // RGB Misinterpreted as CMYK.
                "test_4x4_jpeg.tiff",
                // RGB Misinterpreted as CMYK.
                "test_4x4_jpeg_grayscale.tiff",
                // Unsupported - Grayscale
        };
        for (final String filename : filenames) {
            try {
                final BufferedImage image = ImageIO.read(Objects.requireNonNull(Main.class.getResource(filename)).openStream());
                ImageIO.write(image, "PNG", Path.of(".", filename + ".png").toFile());
                System.out.println(filename + ": " + filename + ".png");
            } catch (IOException e) {
                System.out.println(filename + ": " + e.getMessage());
                //e.printStackTrace();
            }
        }
    }
}
