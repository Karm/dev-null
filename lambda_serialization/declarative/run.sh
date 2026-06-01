#!/bin/bash
set -e
echo "Building Declarative Lambda Serialization Example"
echo ""
export JAVA_HOME=/home/karm/X/JDKs/openjdk-25
export GRAALVM_HOME=/home/karm/X/JDKs/mandrel-java25-25.0.3.0-Final
export PATH=${JAVA_HOME}/bin:${GRAALVM_HOME}/bin:${PATH}
rm -f Main.class main-native
javac Main.java
echo "Build native image with reachability-metadata.json"
echo "Using lambda definition in reachability-metadata.json:"
cat META-INF/native-image/reachability-metadata.json | grep -A 10 '"lambda"'
native-image \
    -H:+ReportExceptionStackTraces \
    -H:ConfigurationFileDirectories=META-INF/native-image \
    --no-fallback \
    -o main-native \
    Main
echo ""
echo "Run native image (expected to fail)"
./main-native || echo "Expected failure"
echo ""
echo "Done"
