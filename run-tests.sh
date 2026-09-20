#!/bin/bash
# Führt die Tests aller Quarkus-Services nacheinander aus und zeigt am Ende
# eine zusammengefasste Übersicht (bestanden/fehlgeschlagen) an.
#
# Nutzung:
#   ./run-tests.sh            # alle Services
#   ./run-tests.sh kotlin1    # nur Services, deren Verzeichnisname "kotlin1" enthält

set -uo pipefail

SERVICES=(
    "code-with-quarkus-kotlin1"
    "code-with-quarkus-websocket"
    "quarkus-author-crud"
    "quarkus-book-crud"
    "quarkus-home-app"
    "quarkus-world-app"
)

FILTER="${1:-}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

declare -A RESULT
declare -A SUMMARY

for service in "${SERVICES[@]}"; do
    if [[ -n "$FILTER" && "$service" != *"$FILTER"* ]]; then
        continue
    fi

    echo
    echo "==================================================================="
    echo "== $service"
    echo "==================================================================="

    if (cd "$ROOT_DIR/$service" && ./mvnw -q test); then
        RESULT[$service]="OK"
    else
        RESULT[$service]="FAIL"
    fi

    report_dir="$ROOT_DIR/$service/target/surefire-reports"
    if [[ -d "$report_dir" ]]; then
        SUMMARY[$service]=$(grep -h "Tests run:" "$report_dir"/*.txt 2>/dev/null | \
            awk '{ran+=$3; fail+=$5; err+=$7; skip+=$9} END {printf "Tests: %d, Failures: %d, Errors: %d, Skipped: %d", ran, fail, err, skip}')
    fi
done

echo
echo "==================================================================="
echo "== Zusammenfassung"
echo "==================================================================="
for service in "${SERVICES[@]}"; do
    if [[ -n "$FILTER" && "$service" != *"$FILTER"* ]]; then
        continue
    fi
    status="${RESULT[$service]:-SKIPPED}"
    printf "%-32s %-6s %s\n" "$service" "$status" "${SUMMARY[$service]:-}"
done

echo
echo "Detaillierte Reports je Service unter: <service>/target/surefire-reports/*.txt"

for status in "${RESULT[@]}"; do
    if [[ "$status" == "FAIL" ]]; then
        exit 1
    fi
done
