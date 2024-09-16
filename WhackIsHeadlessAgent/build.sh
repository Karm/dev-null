#!/bin/bash
# shellcheck disable=SC2164
pushd src
javac -classpath /home/karm/Tools/jtreg/build/images/jtreg/lib/asmtools.jar IsHeadlessAgent.java
jar cmf MANIFEST.MF ../WhackIsHeadlessAgent.jar IsHeadlessAgent.class IsHeadlessAgent\$GraphicsEnvironmentTransformer.class
javac Test.java
echo "WITHOUT AGENT:"
java Test
echo
echo "WITH AGENT:"
java -Xbootclasspath/a:/home/karm/Tools/jtreg/build/images/jtreg/lib/asmtools.jar -javaagent:../WhackIsHeadlessAgent.jar Test
echo
popd
