#!/bin/sh
rm -rf META-INF main-* *.class reflect-config.json*
javac Main.java
echo "reflect-config.json:"
cat << 'EOF' > reflect-config.json
[
  {
    "name": "Main$ResourceA",
    "methods": [
      { "name": "<init>", "parameterTypes": [] }
    ],
    "fields": [
      { "name": "fieldA" }
    ]
  }
]
EOF
native-image -H:FutureDefaults=none -H:ReflectionConfigurationFiles=reflect-config.json Main main-legacy
echo ""
echo "OUTPUT reflect-config.json:"
./main-legacy
echo ""
native-image -H:FutureDefaults=complete-reflection-types -H:ReflectionConfigurationFiles=reflect-config.json Main main-legacy-crt
echo ""
echo "OUTPUT reflect-config.json + Complete Reflection Types:"
./main-legacy-crt
echo ""
echo "reachability-metadata.json:"
mv reflect-config.json reflect-config.json_BAK
mkdir -p META-INF/native-image
cat << 'EOF' > META-INF/native-image/reachability-metadata.json
{
  "reflection": [
    {
      "type": "Main$ResourceA",
      "methods": [
        { "name": "<init>", "parameterTypes": [] }
      ],
      "fields": [
        { "name": "fieldA" }
      ]
    }
  ]
}
EOF
native-image -H:FutureDefaults=none Main main-new-none
echo ""
echo "OUTPUT reachability-metadata.json:"
./main-new-none
echo ""
echo "reachability-metadata.json + Complete Reflection Types"
native-image -H:FutureDefaults=complete-reflection-types Main main-new-crt
echo ""
echo "OUTPUT reachability-metadata.json + Complete Reflection Types:"
./main-new-crt

