#!/bin/bash
set -e
echo "Building Legacy Lambda Serialization Example (serialization-config.json v1.1.0)"
echo ""
export JAVA_HOME=/home/karm/X/JDKs/openjdk-25
export GRAALVM_HOME=/home/karm/X/JDKs/mandrel-java25-25.0.3.0-Final
export PATH=${JAVA_HOME}/bin:${GRAALVM_HOME}/bin:${PATH}
rm -f Main.class main-native
javac Main.java
echo "Build native image with legacy serialization-config.json"
echo "Using lambdaCapturingTypes in serialization-config.json:"
cat META-INF/native-image/serialization-config.json | grep -A 5 '"lambdaCapturingTypes"'
native-image \
    -H:+ReportExceptionStackTraces \
    -H:ConfigurationFileDirectories=META-INF/native-image \
    --no-fallback \
    -o main-native \
    Main
echo ""
echo "Run native image"
./main-native
echo ""
echo "Done"
