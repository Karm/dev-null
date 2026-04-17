public class Main {
    public static class ResourceA {
        public String fieldA;
        
        private class InnerClassOfA {
            public String innerField;
        }
        
        protected static class StaticClassOfA {
            public String staticField;
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Native Image Reflection Test ---");
        checkClass("Main$ResourceA");
        checkClass("Main$ResourceA$InnerClassOfA");
        checkClass("Main$ResourceA$StaticClassOfA");
    }

    private static void checkClass(String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            System.out.print("[LOADED] " + className);
            try {
                clazz.getDeclaredConstructors();
                System.out.print(" -> [EXECUTABLE] Metadata present.");
            } catch (Throwable t) { 
                System.out.print(" -> [NOT EXECUTABLE] " + t.getClass().getSimpleName());
            }
            try {
                clazz.getDeclaredFields();
                System.out.println(" -> [FIELDS] Present.");
            } catch (Throwable t) {
                System.out.println(" -> [NO FIELDS] " + t.getClass().getSimpleName());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[NOT LOADED] " + className + " (Dead-code eliminated)");
        }
    }
}

