#!/bin/bash
readonly DURATION="PT10S"
readonly SETTINGS_FILE="$(pwd)/resources/settings.xml"
readonly RESULTS_DIRECTORY="$(pwd)/target/"
readonly PROJECT_ROOT=$(pwd)

# Exit immediately if any simple command fails
set -e

# Export Java home
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Build the project
echo "Building project"
mvn -q -f "$PROJECT_ROOT" -s "$SETTINGS_FILE" -DskipTests install

# Run two campaigns for each combination
for subject in maven rhino; do
  for fuzzer in bedivfuzz-simple bedivfuzz-structure rlcheck zest zeugma-linked zeugma-none zeugma-one_point zeugma-two_point; do
    for trial in 1 2; do
      bash scripts/fuzz.sh "$RESULTS_DIRECTORY/$subject-$fuzzer-$trial" "$subject" "$fuzzer" "$DURATION" "$SETTINGS_FILE"
    done
  done
done

# Analyze heritability
HERITABILITY_CSV="$RESULTS_DIRECTORY/heritability.csv"
mvn -B -ntp \
  -f "$PROJECT_ROOT" \
  -s "$SETTINGS_FILE" \
  -pl :zeugma-evaluation-heritability \
  dependency:properties exec:java@instrument exec:exec@compute \
  -Dheritability.corporaDir="$RESULTS_DIRECTORY" \
  -Dheritability.outputFile="$HERITABILITY_CSV"
head "$HERITABILITY_CSV"

# Extract coverage data
COVERAGE_CSV="$RESULTS_DIRECTORY/coverage.csv"
python3 scripts/extract_coverage.py "$RESULTS_DIRECTORY" "$COVERAGE_CSV"
head "$COVERAGE_CSV"
