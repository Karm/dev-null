# Platforms

This example works with Linux amd64 (linux-x86_64),
if you need to build on e.g. Linux aarch64, edit
`src/main/resources/resource-config.json`:

```diff
diff --git a/brotli4j-native/src/main/resources/resource-config.json b/brotli4j-native/src/main/resources/resource-config.json
index 988d6d3..1054c37 100644
--- a/brotli4j-native/src/main/resources/resource-config.json
+++ b/brotli4j-native/src/main/resources/resource-config.json
@@ -5,7 +5,7 @@
         "pattern": "\\QMETA-INF/services/com.aayushatharva.brotli4j.service.BrotliNativeProvider\\E"
       },
       {
-        "pattern": "\\Qlib/linux-x86_64/libbrotli.so\\E"
+        "pattern": "\\Qlib/linux-aarch64/libbrotli.so\\E"
       }
     ]
   },
```

It is possible to bundle all libs, but it inflates the executable size needlessly.

# Testing

e.g.

```
$ export JAVA_HOME=/home/tester/karm/mandrel-java21-23.1.3.1-Final;export GRAALVM_HOME=${JAVA_HOME};export PATH=${JAVA_HOME}/bin:${PATH}

$ mvn clean verify -Pnative
```
