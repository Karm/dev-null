package mandrel_perf_demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {

    public static void thisIsTheEnd(List<ClassA> ays) throws NoSuchAlgorithmException {
        final ByteArrayOutputStream ba = new ByteArrayOutputStream(ays.size() * 60); // ballpark magic constant hardcoded for test_data.txt purpose
        ays.forEach(i -> ba.writeBytes(i.toString().getBytes(UTF_8)));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(ba.toByteArray());
        System.out.printf("%064x%n", new BigInteger(1, digest.digest()));
        /* Used to verify the hash is what we think it is:
        $ sha256sum  /tmp/TEST.out
        b6951775b0375ea13fc977581e54eb36d483e95ed3bc1e62fcb8da59830f1ef9  /tmp/TEST.out
        try (FileOutputStream fos = new FileOutputStream("/tmp/TEST.out")) {
            fos.write(ba.toByteArray());
        }*/
        System.exit(0);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        if (args.length != 1) {
            System.err.println("Single argument with a path to a file is expected.");
            System.exit(1);
        }
        final Path file = Path.of(args[0]);
        final Pattern p = Pattern.compile("([\\+-]?\\d+)");
        final List<ClassA> ays = new ArrayList<>();
        String myString = null;
        int myNumber = Integer.MIN_VALUE;
        System.out.println("Q to quit");
        try (Scanner sc = new Scanner(file, UTF_8)) {
            while (sc.hasNextLine()) {
                if (myString != null && myNumber != Integer.MIN_VALUE) {
                    ays.add(new ClassA(myNumber, myString));
                    myString = null;
                    myNumber = Integer.MIN_VALUE;
                }
                String l = sc.nextLine();
                if ("Q".equals(l)) {
                    thisIsTheEnd(ays);
                }
                if (myNumber == Integer.MIN_VALUE) {
                    Matcher m = p.matcher(l);
                    if (m.matches()) {
                        myNumber = Integer.parseInt(m.group(1));
                    } else {
                        myString = l;
                    }
                } else {
                    myString = l;
                }
            }
        }
    }
}
