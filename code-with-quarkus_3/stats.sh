#!/bin/bash
bs=$(find -name "*quarkus.json")
ts=$(find -name "*runner-timing-stats.json")
stat_id=$(curl -s -w '\n' -H "Content-Type: application/json" -H "token: $TOKEN" --post302 --data "@$bs" "$URL/import?t=$TAG" | jq .id)
curl -s -w '\n' -H "Content-Type: application/json" -H "token: $TOKEN" -X PUT --data "@$ts" "$URL/$stat_id" > /dev/null
