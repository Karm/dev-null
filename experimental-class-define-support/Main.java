import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;

public class Main {
    static void main() throws Exception {
        final byte[] classBytes = ClassFile.of().build(ClassDesc.of("Hi"), cb -> {
            cb.withSuperclass(ClassDesc.of("java.lang.Object"));
            cb.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL);
            cb.withMethod("<init>",
                    MethodTypeDesc.of(ConstantDescs.CD_void),
                    AccessFlag.PUBLIC.mask(),
                    method -> method.withCode(code -> {
                        code.aload(0);
                        code.invokespecial(ClassDesc.of("java.lang.Object"), "<init>",
                                MethodTypeDesc.of(ConstantDescs.CD_void));
                        code.return_();
                    }));
            cb.withMethod("sayHi",
                    MethodTypeDesc.of(ClassDesc.of("java.lang.String")),
                    AccessFlag.PUBLIC.mask(),
                    method -> method.withCode(code -> {
                        code.ldc("Hello from dynamic class.");
                        code.areturn();
                    }));
        });
        final ClassLoader loader = new ClassLoader(Main.class.getClassLoader()) {
            public Class<?> define(String name, byte[] b) {
                return defineClass(name, b, 0, b.length);
            }
        };
        final Class<?> generatedClass = (Class<?>) loader.getClass()
                .getMethod("define", String.class, byte[].class)
                .invoke(loader, "Hi", classBytes);
        System.out.println("Output: " + generatedClass.getMethod("sayHi")
                .invoke(generatedClass.getConstructor().newInstance()));
    }
}
