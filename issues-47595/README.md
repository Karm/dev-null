# Test profile
```
$ ./mvnw clean package -Pnative -Dquarkus.profile=test
$ ./target/code-with-quarkus-1.0.0-SNAPSHOT-runner
$ curl http://localhost:8080/resource/test-file.dat
X1
$ curl http://localhost:8080/resource/test-file.txt
X2
$ curl http://localhost:8080/resource/blabla.dat
500 - Internal Server Error
$ curl http://localhost:8080/resource/something.txt
500 - Internal Server Error
```

# Prod profile
```
$ ./mvnw clean package -Pnative -Dquarkus.profile=prod
$ ./target/code-with-quarkus-1.0.0-SNAPSHOT-runner
$ curl http://localhost:8080/resource/test-file.dat
500 - Internal Server Error
$ curl http://localhost:8080/resource/test-file.txt
500 - Internal Server Error
$ curl http://localhost:8080/resource/blabla.dat
Y1
$ curl http://localhost:8080/resource/something.txt
Y2
```
s
