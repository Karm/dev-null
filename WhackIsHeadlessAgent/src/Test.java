import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class Test {
    public static void main(String[] args) {
        final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final Rectangle r = g.getMaximumWindowBounds();
        System.out.println(r);
    }
}
