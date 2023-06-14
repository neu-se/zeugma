#!/bin/bash
readonly DURATION="PT10S"
readonly SETTINGS_FILE="$(pwd)/resources/settings.xml"
readonly RESULTS_DIRECTORY="$(pwd)/target/"
readonly PROJECT_ROOT=$(pwd)

# Exit immediately if any simple command fails
set -e

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

echo "Building project"
mvn -q -f "$PROJECT_ROOT" -s "$SETTINGS_FILE" -DskipTests install

echo "Running fuzzing campaigns"
for subject in maven rhino; do
  for fuzzer in bedivfuzz-simple bedivfuzz-structure rlcheck zest zeugma-linked zeugma-none zeugma-one_point zeugma-two_point; do
    for trial in 1 2; do
      bash scripts/fuzz.sh "$RESULTS_DIRECTORY/$subject-$fuzzer-$trial" "$subject" "$fuzzer" "$DURATION" "$SETTINGS_FILE"
    done
  done
done

echo "Performing heritability experiment"
HERITABILITY_CSV="$RESULTS_DIRECTORY/heritability.csv"
mvn -B -ntp \
  -f "$PROJECT_ROOT" \
  -s "$SETTINGS_FILE" \
  -pl :zeugma-evaluation-heritability \
  -Pcompute install \
  -Dheritability.corpora="$RESULTS_DIRECTORY" \
  -Dheritability.output="$HERITABILITY_CSV"
head "$HERITABILITY_CSV"

echo "Creating fuzzing report"
REPORT_FILE="$RESULTS_DIRECTORY/report.html"
python3 scripts/report.py "$RESULTS_DIRECTORY" "$REPORT_FILE"
