#!/bin/bash
readonly RESULTS_DIRECTORY=$1
readonly SUBJECT=$2
readonly DURATION=$3
readonly PROJECT_ROOT=$(pwd)

# Exit immediately if any simple command fails
set -e

# Print the arguments
echo "Running: results_directory=$RESULTS_DIRECTORY, subject=$SUBJECT, fuzzer=$FUZZER, duration=$DURATION"

# Create a temporary directory for Meringue's output
readonly OUTPUT_DIRECTORY="$PROJECT_ROOT/target/meringue"
mkdir -p "$OUTPUT_DIRECTORY"

# Export Maven options
export MAVEN_OPTS="-Dhttps.protocols=TLSv1.2
  -Dorg.slf4j.simpleLogger.showDateTime=true
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN"

# Run the fuzzing campaign
mvn -ntp -B -e \
  -f "$PROJECT_ROOT" \
  -pl :zeugma-hint-evaluation \
  -P"$SUBJECT" \
  meringue:fuzz \
  meringue:analyze \
  -Dmeringue.duration="$DURATION" \
  -Dmeringue.outputDirectory="$OUTPUT_DIRECTORY"

# Record configuration information
echo "{
  \"subject\": \"$SUBJECT\",
  \"commit_sha\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse HEAD)\",
  \"branch_name\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse --abbrev-ref HEAD)\",
  \"remote_origin_url\": \"$(git --git-dir "$PROJECT_ROOT/.git" config --get remote.origin.url)\"
}" >"$OUTPUT_DIRECTORY/fuzz-info.json"

# Create a TAR archive of the Meringue output directory and move it to the result directory
ARCHIVE_NAME='meringue.tgz'
mkdir -p "$RESULTS_DIRECTORY"
tar -czf "$ARCHIVE_NAME" -C "$OUTPUT_DIRECTORY" .
mv "$ARCHIVE_NAME" "$RESULTS_DIRECTORY/"

# Copy all normal files in the Meringue output directory to the results directory
find "$OUTPUT_DIRECTORY" -maxdepth 1 -type f -exec cp -t "$RESULTS_DIRECTORY" {} +
# If Zeugma's statistics file exists, copy it to the results directory
ZEUGMA_STATS_FILE="$OUTPUT_DIRECTORY/campaign/statistics.csv"
if [ -f "$ZEUGMA_STATS_FILE" ]; then
  cp "$ZEUGMA_STATS_FILE" "$RESULTS_DIRECTORY/zeugma.csv"
fi