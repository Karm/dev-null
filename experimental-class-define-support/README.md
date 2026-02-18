```bash
$ javac --release 25 Main.java
$ mkdir -p META-INF/native-image
$ java -agentlib:native-image-agent=config-output-dir=META-INF/native-image,experimental-class-define-support Main
Output: Hello from dynamic class.
$ native-image --link-at-build-time= --no-fallback -march=native -cp . Main
$ ./main
Output: Hello from dynamic class.
```
