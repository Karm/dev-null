import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Function;

/**
 * This fails because:
 *  * lambda class names include runtime-dependent hashes
 *  * hash at build time differs from runtime
 *  * JSON tries to register a specific class instance that doesn't exist at runtime
 *  * result: Type mismatch error or ClassNotFoundException
 */
public class Main {
    public static Function<String, String> createLambda(String prefix) {
        return (Function<String, String> & Serializable) (String input) -> prefix + ": " + input;
    }

    public static void main(String[] args) {
        System.out.println("Declarative Lambda Serialization Test\n");
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
            System.out.println("   (relying on json metadata)");
            final ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            // DON'T DO THIS AT HOME; this is a demo code only.
            @SuppressWarnings("unchecked")
            final Function<String, String> deserializedLambda = (Function<String, String>) ois.readObject();
            ois.close();
            System.out.println("\n4. Deserialized lambda: " + deserializedLambda.getClass().getName());
            System.out.println("   Test: " + deserializedLambda.apply("World"));
            System.out.println("\n✅ SUCCESS: Lambda serialization works with JSON metadata.");
        } catch (Exception e) {
            System.err.println("\n❌ EXPECTED FAILURE: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
