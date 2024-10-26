```
rm -rf AGENT target && mvn clean package && java -agentlib:native-image-agent=config-output-dir=AGENT -jar target/SystemConsole.jar

native-image --no-fallback --link-at-build-time -march=native -H:ConfigurationFileDirectories=./AGENT -jar target/SystemConsole.jar 

./SystemConsole

```

$ rm -rf AGENT target && mvn clean package && java -agentlib:native-image-agent=config-output-dir=AGENT -cp target/SystemConsole.jar biz.karms.Main

$ native-image -H:+ForeignAPISupport --features=biz.karms.JLineWindowsFeature --no-fallback --link-at-build-time -march=native -H:ConfigurationFileDirectories=./AGENT -jar target/SystemConsole.jar





