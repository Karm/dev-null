package biz.karms;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeForeignAccess;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Method;


public class JLineWindowsFeature implements Feature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        // Register a downcall for Kernel32 GetConsoleScreenBufferInfo method
        RuntimeForeignAccess.registerForDowncall(
                FunctionDescriptor.of(ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS, // HANDLE
                        ValueLayout.JAVA_INT), // Info structure
                Linker.Option.critical(true)
        );

        // Register a generic downcall (you may replace these with actual signatures)
        RuntimeForeignAccess.registerForDowncall(
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT)
        );

        // Register an upcall, if you expect callbacks from native code
        RuntimeForeignAccess.registerForUpcall(
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT)
        );
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Ensure the JLine classes related to Windows are initialized at runtime.
        // This will make sure GraalVM doesn't optimize away their dynamic behavior.
        try {
            // Register Kernel32 and WindowsAnsiWriter methods for reflection
            Class<?> kernel32Class = Class.forName("jdk.internal.org.jline.terminal.impl.ffm.Kernel32");
            RuntimeReflection.register(kernel32Class);
            for (Method method : kernel32Class.getDeclaredMethods()) {
                RuntimeReflection.register(method);
            }

            Class<?> windowsAnsiWriterClass = Class.forName("jdk.internal.org.jline.terminal.impl.ffm.WindowsAnsiWriter");
            RuntimeReflection.register(windowsAnsiWriterClass);
            for (Method method : windowsAnsiWriterClass.getDeclaredMethods()) {
                RuntimeReflection.register(method);
            }

            // Register other necessary internal JLine classes
            Class<?> ansiWriterClass = Class.forName("jdk.internal.org.jline.utils.AnsiWriter");
            RuntimeReflection.register(ansiWriterClass);
            for (Method method : ansiWriterClass.getDeclaredMethods()) {
                RuntimeReflection.register(method);
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Required JLine classes not found", e);
        }

        // Ensure JLine native methods and classes are not prematurely initialized during image building
        RuntimeClassInitialization.initializeAtRunTime("jdk.internal.org.jline.terminal.impl.ffm");
/*
        // Register the native library for Kernel32
        NativeImageClassLoaderSupport.registerNativeLibrary("Kernel32");
NativeImage.registerJniConfig(new JniConfig.Builder()
                .addDynamicLibrary("Kernel32")
                .build());

        RuntimeJNIAccess.

        // Ensure proper runtime initialization for native resources
        NativeImage.registerNativeCallHandler((nativeImage, function) -> {
            if ("Kernel32".equals(function.getLibraryName())) {
                return true;
            }
            return false;
        });*/
    }
}

