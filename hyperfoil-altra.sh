#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <absolute_path_to_app_A> <absolute_path_to_app_B>"
    exit 1
fi

APP_A=$(realpath "$1")
APP_B=$(realpath "$2")

if [ ! -x "$APP_A" ] || [ ! -x "$APP_B" ]; then
    echo "[ERROR] Both arguments must be paths to executable files."
    exit 1
fi

# Ampere Altra 80-core allocation
# Leaving cores 78,79 unpinned
CPU_SET_A="0-19"           # 20 cores for Native App A
CPU_SET_B="20-39"          # 20 cores for Native App B
CPU_SET_HYPERFOIL="40-77"  # 38 cores for Hyperfoil load generator

HF_HOST="127.0.0.1"
HF_API="http://${HF_HOST}:8090"

# Stress Test Parameters
DURATION="300s"          # 5 minutes
CONCURRENT_USERS=1800    # concurrent pool looping continuously
CONNECTIONS=7000         # connection pool for both endpoints
ENDPOINT="/sunset"
LABEL="Native_AB_Inlining_Stress_Test"

REPORT_DIR="$(pwd)/reports_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$REPORT_DIR"

PID_A=""
PID_B=""

turbo() {
    echo "Setting CPU governor to performance..."
    sudo cpupower frequency-set -g performance > /dev/null 2>&1 || true
}

cleanup() {
    echo "Resetting CPU governor to powersave..."
    sudo cpupower frequency-set -g powersave > /dev/null 2>&1 || true
    
    echo "Killing Quarkus Native processes..."
    [ -n "$PID_A" ] && kill -9 "$PID_A" 2>/dev/null || true
    [ -n "$PID_B" ] && kill -9 "$PID_B" 2>/dev/null || true
    
    echo "Stopping Hyperfoil Server..."
    sudo podman stop hyperfoil-server > /dev/null 2>&1
    sudo podman rm -f hyperfoil-server > /dev/null 2>&1
}
trap cleanup EXIT

start_hyperfoil_server() {
    echo "Starting Hyperfoil on Cores ($CPU_SET_HYPERFOIL)..."
    sudo podman run -d --rm \
        --cpuset-cpus="$CPU_SET_HYPERFOIL" \
        --network host \
        --name hyperfoil-server \
        -e JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Dio.hyperfoil.controller.host=0.0.0.0" \
        quay.io/hyperfoil/hyperfoil:latest \
        standalone > /dev/null
        
    echo "Waiting for API..."
    for i in {1..15}; do
        if curl -s "$HF_API/info" > /dev/null; then
            echo "[OK] Hyperfoil Server is UP!"
            return 0
        fi
        sleep 1
    done
    echo "[ERROR] Hyperfoil Server failed to start."
    exit 1
}

wait_for_endpoints() {
    echo "Waiting for App A (8080) and App B (8081)..."
    for i in {1..20}; do
        if curl -s http://127.0.0.1:8080${ENDPOINT} > /dev/null && \
           curl -s http://127.0.0.1:8081${ENDPOINT} > /dev/null; then
            echo "[OK] Both endpoints are UP!"
            return 0
        fi
        sleep 0.5
    done
    echo "[ERROR] One or both Quarkus instances failed to start."
    exit 1
}

run_concurrent_benchmark() {
    turbo
    start_hyperfoil_server
    
    echo -e "\n\n"
    echo " Local benchmark: $LABEL"
    echo " Comparing: "
    echo "   A: $APP_A"
    echo "   B: $APP_B"
    echo -e "\n\n"

    echo "[START] Launching App A on Cores ($CPU_SET_A) -> Port 8080"
    taskset -c $CPU_SET_A \
        "$APP_A" \
        -Dquarkus.http.port=8080 \
        > "$REPORT_DIR/app_a.log" 2>&1 &
    PID_A=$!

    echo "[START] Launching App B on Cores ($CPU_SET_B) -> Port 8081"
    taskset -c $CPU_SET_B \
        "$APP_B" \
        -Dquarkus.http.port=8081 \
        > "$REPORT_DIR/app_b.log" 2>&1 &
    PID_B=$!

    wait_for_endpoints

    YAML_FILE="$REPORT_DIR/${LABEL}.hf.yaml"
    cat <<EOF > "$YAML_FILE"
name: ${LABEL}
http:
  - host: http://localhost:8080
    sharedConnections: ${CONNECTIONS}
  - host: http://localhost:8081
    sharedConnections: ${CONNECTIONS}
phases:
- stress:
    always:
      users: ${CONCURRENT_USERS}
      duration: ${DURATION}
      scenario:
      - test-app-a:
        - httpRequest:
            authority: localhost:8080
            GET: ${ENDPOINT}
            metric: App-A
            sync: true
      - test-app-b:
        - httpRequest:
            authority: localhost:8081
            GET: ${ENDPOINT}
            metric: App-B
            sync: true
EOF

    echo "Uploading Benchmark YAML..."
    curl -s -X POST -H "Content-Type: text/vnd.yaml" --data-binary "@$YAML_FILE" "$HF_API/benchmark"
    
    echo "Starting Run..."
    RUN_RESPONSE=$(curl -s "$HF_API/benchmark/${LABEL}/start")
    RUN_ID=$(echo "$RUN_RESPONSE" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
    
    if [ -z "$RUN_ID" ]; then
        echo -e "\n[ERROR] Failed to start benchmark. Hyperfoil responded with:"
        echo "$RUN_RESPONSE"
        exit 1
    fi
    
    echo "   >> Run ID: $RUN_ID"
    
    while true; do
        STATUS_JSON=$(curl -s "$HF_API/run/${RUN_ID}")
        if echo "$STATUS_JSON" | grep -q '"completed"[[:space:]]*:[[:space:]]*true'; then
            echo -e "\n[OK] Run Completed."
            break
        elif echo "$STATUS_JSON" | grep -q '"cancelled"[[:space:]]*:[[:space:]]*true'; then
            echo -e "\n[ERROR] Run Cancelled."
            exit 1
        fi
        sleep 3
    done
    
    echo "Downloading HTML Report..."
    REPORT_FILE="$REPORT_DIR/report_${LABEL}.html"
    curl -s -f "$HF_API/run/${RUN_ID}/report" > "$REPORT_FILE"
    echo "[DONE] Benchmark Complete. HTML: $REPORT_FILE"
}

run_concurrent_benchmark

echo ""
echo "Starting Python HTTP Server..."
cd "$REPORT_DIR" || exit
python3 -m http.server 8000

