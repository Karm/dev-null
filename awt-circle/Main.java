import java.awt.FontFormatException;
import java.awt.color.ICC_Profile;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// $ javac Main.java
// $ java -agentlib:native-image-agent=config-output-dir=native-image Main
// $ native-image --no-fallback -g -O0 -H:+TrackNodeSourcePosition -H:ConfigurationFileDirectories=native-image Main
// $ rm image.png
// $ ./main
public class Main {

    static byte[] getProfileDataFromStream(InputStream s) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(s);
        bis.mark(128); // 128 is the length of the ICC profile header
        byte[] header = bis.readNBytes(128);
        if (header.length < 128 || header[36] != 0x61 || header[37] != 0x63 ||
                header[38] != 0x73 || header[39] != 0x70) {
            System.out.printf("KARM: INVALID ICC_Profile header.length < 128: %b, header[36] != 0x61: %b, header[37] != 0x63: %b, header[38] != 0x73: %b, header[39] != 0x70: %b\n",
                    header.length < 128, header[36] != 0x61, header[37] != 0x63, header[38] != 0x73, header[39] != 0x70);
            java.nio.file.Files.write(java.nio.file.Path.of("/tmp/KARM-NOK-" + System.currentTimeMillis() + "-header.icc"), header,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.WRITE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            return null;   /* not a valid profile */
        } else {
            System.out.printf("KARM: VALID ICC_Profile header.length < 128: %b, header[36] != 0x61: %b, header[37] != 0x63: %b, header[38] != 0x73: %b, header[39] != 0x70: %b\n",
                    header.length < 128, header[36] != 0x61, header[37] != 0x63, header[38] != 0x73, header[39] != 0x70);
            java.nio.file.Files.write(java.nio.file.Path.of("/tmp/KARM-OK-" + System.currentTimeMillis() + "-header.icc"), header,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.WRITE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        }
        int profileSize = intFromBigEndian(header, 0);
        bis.reset();
        try {
            return bis.readNBytes(profileSize);
        } catch (OutOfMemoryError e) {
            throw new IOException("Color profile is too big");
        }
    }

    private static int intFromBigEndian(byte[] array, int index) {
        return (((array[index] & 0xff) << 24) |
                ((array[index + 1] & 0xff) << 16) |
                ((array[index + 2] & 0xff) << 8) |
                (array[index + 3] & 0xff));
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        /*final BufferedImage i = new BufferedImage(320, 200, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g = (Graphics2D) i.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 320, 200);
        g.setColor(Color.RED);
        g.fillOval(0, 0, 320, 200);
        g.dispose();
        ImageIO.write(i, "PNG", new File("image.png"));
        */

        final ICC_Profile p = ICC_Profile.getInstance(new FileInputStream("/home/karm/workspaceRH/quarkus/integration-tests/awt/src/test/resources/CGATS001Compat-v2-micro.icc"));
        System.out.printf("ColorSpaceType: %d, PCSType: %d, MinorVersion: %d, MajorVersion: %d, NumComponents: %d\n",
                p.getColorSpaceType(), p.getPCSType(), p.getMinorVersion(), p.getMajorVersion(), p.getNumComponents());











        ...List<Klass> c...
        c.add(new KlassBuilder("java.lang.System")
                .addMethods(Map.of("load", List.of("java.lang.String"),
                        "setProperty", List.of("java.lang.String", "java.lang.String")))
                .build()
        );
        c.add(new KlassBuilder("sun.awt.SunToolkit")
                .addMethods(Map.of("awtLock", Collections.emptyList(),
                         "awtLockNotify", Collections.emptyList(),
                        "awtLockNotifyAll", Collections.emptyList(),
                        "awtLockWait", List.of("long"),
                        "awtUnlock", Collections.emptyList()))
                .addFields(List.of("AWT_LOCK", "AWT_LOCK_COND"))
                .build()
        );
        c.add(new KlassBuilder("sun.awt.X11.XErrorHandlerUtil")
                .addMethod(Map.of("init", List.of("long")))
                .build()
        );
        c.add(new KlassBuilder("sun.awt.X11.XToolkit")
                .addAllFields()
                .build()
        );
        c.add(new KlassBuilder("java.lang.Thread")
                .addMethod(Map.of("yield", List.of()))
                .build()
        );














    }
}
