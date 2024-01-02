#!/bin/bash
readonly SUBJECT=$1
readonly TRIALS=$2
readonly DURATION=$3
readonly RESULTS_DIRECTORY="$(pwd)/evaluate"
readonly PROJECT_ROOT=$(pwd)

# Exit immediately if any simple command fails
set -e

for fuzzer in bedivfuzz-simple bedivfuzz-structure rlcheck zest zeugma-linked zeugma-none zeugma-one_point zeugma-two_point; do
  if [ "${fuzzer}" = "rlcheck" ] && [ "${SUBJECT}" = "bcel" ]; then
    continue
  fi
  for trial in $(seq 1 "$TRIALS"); do
    echo "Running campaign $trial/$TRIALS for $fuzzer on $SUBJECT."
    output_directory="$RESULTS_DIRECTORY/$SUBJECT-$fuzzer-$trial"
    mvn -ntp -B -e \
      -f "$PROJECT_ROOT" \
      -pl :zeugma-evaluation-tools \
      -P"$SUBJECT,$fuzzer" \
      meringue:fuzz \
      meringue:analyze \
      -Dmeringue.duration="$DURATION" \
      -Dmeringue.outputDirectory="$output_directory" \
      1>/dev/null 2>/dev/null
    ARCHIVE_NAME='meringue.tgz'
    tar -czf "$ARCHIVE_NAME" -C "$output_directory" .
    mv "$ARCHIVE_NAME" "$output_directory/"
  done
done

echo "Performing heritability experiment."
HERITABILITY_CSV="$RESULTS_DIRECTORY/heritability.csv"
mvn -q \
  -f "$PROJECT_ROOT" \
  -pl :zeugma-evaluation-heritability \
  -Pcompute install \
  -Dheritability.corpora="$RESULTS_DIRECTORY" \
  -Dheritability.output="$HERITABILITY_CSV" \
  1>/dev/null 2>/dev/null

readonly REPORT_FILE="$RESULTS_DIRECTORY/report.html"
echo "Creating report: $REPORT_FILE"
python3 scripts/report.py "$RESULTS_DIRECTORY" "$REPORT_FILE"
