import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Function;

/**
 * Legacy serialization-config.json schema (v1.1.0)
 * with lambdaCapturingTypes instead of the newer reachability-metadata.json.
 *
 * The legacy schema allows registering lambda-capturing types by class name,
 * which tells GraalVM to enable serialization for all lambdas declared in
 * methods of that class.
 */
public class Main {
    public static Function<String, String> createLambda(String prefix) {
        return (Function<String, String> & Serializable) (String input) -> prefix + ": " + input;
    }

    public static void main(String[] args) {
        System.out.println("Legacy Lambda Serialization Test (serialization-config.json v1.1.0)\n");
        try {
            final Function<String, String> originalLambda = createLambda("Hello");
            System.out.println("1. Created lambda: " + originalLambda.getClass().getName());
            System.out.println("   Test: " + originalLambda.apply("World"));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(originalLambda);
            oos.close();
            final byte[] serialized = baos.toByteArray();
            System.out.println("\n2. Serialized lambda (" + serialized.length + " bytes)");
            System.out.println("\n3. Attempting deserialization without ObjectInputFilter");
            System.out.println("   (relying on legacy serialization-config.json)");
            final ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            @SuppressWarnings("unchecked")
            final Function<String, String> deserializedLambda = (Function<String, String>) ois.readObject();
            ois.close();
            System.out.println("\n4. Deserialized lambda: " + deserializedLambda.getClass().getName());
            System.out.println("   Test: " + deserializedLambda.apply("World"));
            System.out.println("\n✅ SUCCESS: Lambda serialization works with legacy serialization-config.json.");
        } catch (Exception e) {
            System.err.println("\n❌ FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
