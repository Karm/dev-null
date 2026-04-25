# Quarkus app

The main intention here is to pull in some heavy extensions and yet
do meaningful work that won't feel as an artificially bloated edge case.

The app is an adaptation of one of our test cases from [Mandrel Integration Tests](https://github.com/Karm/mandrel-integration-tests/tree/master/apps/quarkus-mp-orm-dbs-awt)

i.e. Installed features:

    agroal, auxiliary-native-support, awt, cdi, hibernate-orm, hibernate-orm-panache,
    hibernate-validator, jdbc-mariadb, jdbc-postgresql, narayana-jta, opentelemetry,
    qute, rest, rest-client, rest-client-jsonb, rest-jaxb, rest-jsonb, rest-qute,
    smallrye-context-propagation, smallrye-openapi, vertx

Among other endpoints, the most important is `/perfMeshup` of `PerfMeshupResource`. See `PerfMeshupResourceTest`.

* It accepts a giant XML with entities and base64 encoded pictures, side shoots of the authors' own head, encoded in a myriad of compressions, containers, tiff metadata etc. 
* It deserializes entities, deserializes images
* It writes the data into two different databases, covering it in an XA transaction
* It produces and returns a PDF file with the data

## Podman, databases

Having trouble with TestContainers and Podman? Take a look: https://quarkus.io/blog/quarkus-devservices-testcontainers-podman/

You might have to enable:
```
$ systemctl --user enable podman.socket
$ systemctl --user start podman.socket
```

This must return OK:
```
$ curl -H "Content-Type: application/json" --unix-socket /var/run/user/$UID/podman/podman.sock  http://localhost/_ping
```

## What is this?

A demo app comprising various dependencies to intentionally
add slightly more work for the compiler. Each component is
covered with a rudimentary test and each dependency has a simple example so as it doesn't get optimized away.

## Testing

```
$ ./mvnw clean verify -Dnative

```

## Databases

Databases are started and stopped automatically via Quarkus devservices using testcontainers. If you need to work with those manually, you can start them e.g. as:

```
podman run --network=host --ulimit memlock=-1:-1 -it -d --rm=true --name quarkus_test_db -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=db1 quay.io/debezium/postgres:15 -c max_prepared_transactions=100

podman run -it -d --name mariadb -p 49157:3306  --env MARIADB_USER=quarkus --env MARIADB_PASSWORD=quarkus --env MARIADB_ROOT_PASSWORD=quarkus --env MARIADB_DATABASE=db2 docker.io/library/mariadb:11.0
```

When started, the heaviest endpoint is the XML->PDF one:

```
curl -OJL -X POST -H "Content-Type: application/xml" -d @/tmp/employee-profiles-test.xml http://localhost:8080/perfMeshup 
```

## Sources

Various demo endpoints and ideas inspired by:

 * https://github.com/Karm/mandrel-integration-tests/tree/master/apps/quarkus-mp-orm-dbs-awt
 * https://github.com/Karm/fuzz
 * https://github.com/quarkusio/quarkus-quickstarts
 * https://github.com/quarkusio/quarkus/tree/main/integration-tests/awt
 * https://github.com/quarkusio/quarkus-quickstarts/pull/1154
 * https://github.com/eclipse/microprofile-starter/tree/main/src/it
