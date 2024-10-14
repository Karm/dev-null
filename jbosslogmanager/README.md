```
rm -rf target/ ./AGENT/ && mvn clean package && \ 
   java -agentlib:native-image-agent=config-output-dir=AGENT -jar target/jbosslogmanager.jar

native-image --no-fallback --link-at-build-time \
    --initialize-at-build-time=org.jboss,io.smallrye \
    -march=native -H:ConfigurationFileDirectories=./AGENT \
    -jar target/jbosslogmanager.jar
    
./jbosslogmanager
```
