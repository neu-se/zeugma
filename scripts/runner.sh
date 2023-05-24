#!/bin/bash
readonly RESULTS_DIRECTORY=$1
readonly SUBJECT=$2
readonly FUZZER=$3
readonly DURATION=$4
readonly SETTINGS_FILE=$5
readonly PROJECT_ROOT=$(pwd)

# Write a trace for each command to standard error
set -x
# Exit immediately if any simple command fails
set -e

# Export Java home
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Build the project
echo "Building project"
mvn -q -f "$PROJECT_ROOT" -s "$SETTINGS_FILE" -DskipTests install

# Run the fuzzing campaign
bash scripts/fuzz.sh "$@"