package biz.karms;

public class Main {
    public static void main(String[] args) {
        final char[] redLineBytes = new char[] {
                0x1B, 0x5B, 0x33, 0x31, 0x6D, // ESC[31m Set to red
                0x52, 0x45, 0x44, // "RED"
                0x1B, 0x5B, 0x30, 0x6D  // ESC[0m Reset
        };
        try (final java.io.PrintWriter w = System.console().writer()) {
            w.write(redLineBytes);
            w.println(System.lineSeparator() + "NOT RED");
            w.flush();
        }
    }
}
