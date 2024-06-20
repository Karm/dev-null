# Web service to convert HTML to PNG

This demo demonstrates a use case for [Quarkus AWT extension](https://github.com/quarkusio/quarkus/tree/main/extensions/awt) and [OpenHTML](https://github.com/danfickle/openhtmltopdf/).
There is a single POST endpoint that consumes an HTML file a and returns an octet stream with a PNG image.

[Quarkus AWT extension](https://github.com/quarkusio/quarkus/tree/main/extensions/awt) enables a set of
ImageIO and AWT functionality in Quarkus Native images that is essential for Java2D rendering.
See the extension documentation and tests to learn the available scope.
Given the nature of native libraries in JDK implementing various image processing algorithms,
venturing outside the tested scope might result in native image build time or runtime failure.

# Additional system dependencies
Note `microdnf` command installing `fontconfig` library in [Dockerfile.jvm](./src/main/docker/Dockerfile.jvm)
and [Dockerfile.legacy-jar](./src/main/docker/Dockerfile.legacy-jar) to support jvm mode. 
Both `freetype` and `fontconfig` libraries are needed for native mode in [Dockerfile.native](./src/main/docker/Dockerfile.native).

# Usage with curl

e.g.

```bash
    curl -X POST --data-binary @src/test/resources/test-html.html -H "Content-Type: text/html" http://localhost:8080/html2png -o doc/example.png
```

Converts the first page of the given HTML to a PNG file and returns it.

# Usage with a client code

See [HTML2PNGResourceTest.java](./src/test/java/org/acme/awt/rest/HTML2PNGResourceTest.java). The test is executed
in native mode with:

```bash
./mvnw clean verify -Pnative
```
To run native tests locally, a JDK 21 with Mandrel (or GraalVM) 23.1 is recommended.
Additionally, `freetype-devel` and `fontconfig` libraries must be installed. 

# Container
We can use a builder image, so as we don't need to have GraalVM/Mandrel locally: 
```
./mvnw package -Pnative -Dquarkus.native.container-build=true
```
To build a runtime image with the app:

```
podman build -f src/main/docker/Dockerfile.native -t quarkus/openhtmltopdf-java2d .
```

Then run the container using:

```
podman run -i --rm -p 8080:8080 quarkus/openhtmltopdf-java2d
```

# What the result looks like

You can use the attached [test-html.html](./src/test/resources/test-html.html) and see how it converts to the image below:

![Result png image from html file](./doc/example.png)
