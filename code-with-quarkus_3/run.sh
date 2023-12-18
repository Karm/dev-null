#!/bin/sh

images=(
"quay.io/quarkus/ubi-quarkus-mandrel-builder-image:22.3.2.1-Final-java17"
#"quay.io/quarkus/ubi-quarkus-mandrel-builder-image:22.3.1.0-Final-java17" \
#"quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3.1-java19" \
#"quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3.1-java17" \
#"quay.io/karmkarm/ubi-quarkus-mandrel-builder-image:23.0-java20"
)

qversions=(
"3.0.4.Final"
)

for image in "${images[@]}"; do
  for qversion in "${qversions[@]}"; do
    echo $image $qversion;
    ./mvnw clean package \
        -Pnative -Dquarkus.platform.group-id=io.quarkus \
        -Dquarkus.platform.version=${qversion} \
        -Dquarkus.native.additional-build-args=-H:BuildOutputJSONFile=quarkus.json \
        -Dquarkus.native.container-build=true \
        -Dquarkus.native.builder-image=${image}
    pushd target
    TAG="${image},${qversion}" URL=http://127.0.0.1:8080/api/v1/image-stats ../stats.sh
    popd
  done
done
