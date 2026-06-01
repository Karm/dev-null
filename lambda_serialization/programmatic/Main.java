import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Function;

/**
 * This works because GraalVM's SubstrateGraphBuilderPlugins intercepts
 * ObjectInputFilter.Config.createFilter() at build time and registers
 * the capturing class's $deserializeLambda$ method.
 */
public class Main {
    public static Function<String, String> createLambda(String prefix) {
        return (Function<String, String> & Serializable) (String input) -> prefix + ": " + input;
    }

    public static void main(String[] args) {
        System.out.println("Programmatic Lambda Serialization Test\n");
        try {
            final Function<String, String> originalLambda = createLambda("Hello");
            System.out.println("1. Created lambda: " + originalLambda.getClass().getName());
            System.out.println("   Test: " + originalLambda.apply("World"));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(originalLambda);
            oos.close();
            byte[] serialized = baos.toByteArray();
            System.out.println("\n2. Serialized lambda (" + serialized.length + " bytes)");
            final ObjectInputFilter filter = ObjectInputFilter.Config.createFilter("Main$$Lambda*;java.lang.invoke.SerializedLambda;");
            System.out.println("\n3. Created ObjectInputFilter for pattern: Main$$Lambda*");
            final ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            ois.setObjectInputFilter(filter);
            @SuppressWarnings("unchecked")
            final Function<String, String> deserializedLambda = (Function<String, String>) ois.readObject();
            ois.close();
            System.out.println("\n4. Deserialized lambda: " + deserializedLambda.getClass().getName());
            System.out.println("   Test: " + deserializedLambda.apply("World"));
            System.out.println("\n✅ SUCCESS: Lambda serialization works with ObjectInputFilter!");
        } catch (Exception e) {
            System.err.println("\n❌ FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
