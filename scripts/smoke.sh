#!/bin/bash
readonly DURATION="PT10S"
readonly SETTINGS_FILE="$(pwd)/resources/settings.xml"
readonly RESULTS_DIRECTORY="$(pwd)/target/"

# Exit immediately if any simple command fails
set -e

# Run three trials for each combination
for subject in ant closure maven nashorn rhino tomcat; do
  for fuzzer in bedivfuzz-simple bedivfuzz-structure rlcheck zest zeugma-linked zeugma-none zeugma-one_point zeugma-two_point; do
    for trial in 1 2 3; do
      bash "scripts/fuzz.sh" "$RESULTS_DIRECTORY/$subject-$fuzzer-$trial" "$subject" "$fuzzer" "$DURATION" "$SETTINGS_FILE"
    done
  done
done

# Analyze heritability
HERITABILITY_CSV="$RESULTS_DIRECTORY/heritability.csv"
mvn -B -ntp -pl :zeugma-evaluation-heritability \
  dependency:properties exec:java@instrument exec:exec@compute \
  -Dheritability.corporaDir="$RESULTS_DIRECTORY" \
  -Dheritability.outputFile="$HERITABILITY_CSV"
head "$HERITABILITY_CSV"