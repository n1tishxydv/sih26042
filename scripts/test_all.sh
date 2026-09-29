#!/usr/bin/env bash
# SIH26042 Monorepo Unified Test Runner (Bash)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"
export PYTHONPATH="$REPO_ROOT:$REPO_ROOT/packages/contracts/python:$REPO_ROOT/services/pack-builder:$REPO_ROOT/services/api:$PYTHONPATH"



echo "=========================================================="
echo " [SIH26042] Running Monorepo Unified Smoke & Unit Tests   "
echo "=========================================================="

echo -e "\n>>> [1/4] Testing Language Pack Schema..."
python3 -m pytest "$REPO_ROOT/packages/language-pack-schema" -v --tb=short

echo -e "\n>>> [2/4] Testing Pack Builder Service..."
python3 -m pytest "$REPO_ROOT/services/pack-builder/tests" -v --tb=short

echo -e "\n>>> [3/4] Testing FastAPI Backend & Migrations..."
python3 -m pytest "$REPO_ROOT/services/api/tests" -v --tb=short

echo -e "\n>>> [4/4] Testing Android Application (JVM Unit Tests)..."
cd "$REPO_ROOT/apps/android"
./gradlew test --no-daemon

echo -e "\n=========================================================="
echo " [SUCCESS] All SIH26042 Smoke & Unit Tests Passed!        "
echo "=========================================================="
