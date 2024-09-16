import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

public class IsHeadlessAgent {

    public static final File BYTECODE_FILE = new File("GraphicsEnvironment.class");
    public static final File JASM_FILE = new File("GraphicsEnvironment.jasm");
    public static final File MODIFIED_JASM_FILE = new File("GraphicsEnvironment.modified.jasm");

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting IsHeadlessAgent...");
        inst.addTransformer(new GraphicsEnvironmentTransformer());
    }

    public static String processJASM(String jasm) {
        final StringBuilder sb = new StringBuilder();
        int state = 0;
        final Pattern begin = Pattern.compile("^[\\s\\t]*public Method getMaximumWindowBounds.*");
        final Pattern end = Pattern.compile("^[\\s\\t]*}$");
        for (String line : jasm.lines().toList()) {
            if (begin.matcher(line).matches()) {
                state = 1;
            }
            if (state == 1) {
                if (end.matcher(line).matches()) {
                    sb.append("""
                                         public Method getMaximumWindowBounds:"()Ljava/awt/Rectangle;"
                                             throws java/awt/HeadlessException
                                             stack 5 locals 1
                                         {
                                             new class Rectangle;             // Create a new Rectangle
                                             dup;                             // Duplicate the reference to the new Rectangle
                                             iconst_0;                        // Push integer 0 (for x)
                                             iconst_0;                        // Push integer 0 (for y)
                                             iconst_0;                        // Push integer 0 (for width)
                                             iconst_0;                        // Push integer 0 (for height)
                                             invokespecial Method java/awt/Rectangle."<init>":"(IIII)V";  // Call the Rectangle constructor with (x=0, y=0, width=0, height=0)
                                             areturn;                         // Return the new Rectangle
                                         }
                            """);
                    state = 0;
                    continue;
                }
            }
            if (state == 0) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    static class GraphicsEnvironmentTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if ("java/awt/GraphicsEnvironment".equals(className)) {
                System.out.println("Transforming java.awt.GraphicsEnvironment...");
                try {
                    System.out.println("Disassembling bytecode to JASM text...");
                    final String processedJASM = processJASM(disassembleToJASM(classfileBuffer));
                    Files.writeString(MODIFIED_JASM_FILE.toPath(), processedJASM);
                    return assembleFromJASM(processedJASM);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String disassembleToJASM(byte[] bytecode) throws Exception {
            try (FileOutputStream fos = new FileOutputStream(BYTECODE_FILE)) {
                fos.write(bytecode);
            }
            try (final BufferedWriter bw = new BufferedWriter(new FileWriter(JASM_FILE));
                    final PrintWriter pw = new PrintWriter(bw)) {
                org.openjdk.asmtools.jdis.Main disassembler =
                        new org.openjdk.asmtools.jdis.Main(pw, new PrintWriter(System.err), "jdis");
                boolean result = disassembler.disasm(new String[] { BYTECODE_FILE.getAbsolutePath() });
                System.out.println("Disassembly result: " + result);
            }
            return new String(java.nio.file.Files.readAllBytes(JASM_FILE.toPath()));
        }

        private byte[] assembleFromJASM(String jasmText) throws Exception {
            try (FileWriter fw = new FileWriter(JASM_FILE)) {
                fw.write(jasmText);
            }
            System.out.println("Gonna assemble it again....");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(BYTECODE_FILE));
                    PrintWriter pw = new PrintWriter(bw)) {
                org.openjdk.asmtools.jasm.Main assembler = new org.openjdk.asmtools.jasm.Main(pw, "jasm");
                boolean result = assembler.compile(new String[] { JASM_FILE.getAbsolutePath() });
                System.out.println("Assembled result: " + result);
            }
            return java.nio.file.Files.readAllBytes(BYTECODE_FILE.toPath());
        }
    }
}
