#!/bin/bash
# e.g. TAG=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:22.3-java17-3 URL=http://127.0.0.1:8080/api/v1/image-stats ./stats.sh 
bs=$(find -name "*quarkus.json")
ts=$(find -name "*runner-timing-stats.json")
stat_id=$(curl -s -w '\n' -H "Content-Type: application/json" -H "token: $TOKEN" --post302 --data "@$bs" "$URL/import?t=$TAG" | jq .id)
curl -s -w '\n' -H "Content-Type: application/json" -H "token: $TOKEN" -X PUT --data "@$ts" "$URL/$stat_id" > /dev/null

