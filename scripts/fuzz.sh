readonly RESULTS_DIRECTORY=$1
readonly SUBJECT=$2
readonly FRAMEWORK=$3
readonly ARG_LINE=$4
readonly FUZZER=$5
readonly DURATION=$6

readonly PROJECT_ROOT=$(pwd)
readonly SETTINGS_FILE="resources/settings.xml"
readonly MERINGUE_PLUGIN="edu.neu.ccs.prl.meringue:meringue-maven-plugin:1.0.0-SNAPSHOT"

# Write a trace for each command to standard error
set -x
# Exit immediately if any simple command fails
set -e

# Print the arguments
echo "Running:
  results_directory=$RESULTS_DIRECTORY,
  subject=$SUBJECT,
  framework=$FRAMEWORK,
  arg_line=$ARG_LINE,
  fuzzer=$FUZZER,
  duration=$DURATION"

# Create a temporary directory for Meringue's output
readonly OUTPUT_DIRECTORY="$PROJECT_ROOT/target/meringue"
mkdir -p "$OUTPUT_DIRECTORY"

# Export Java home and Maven options
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export MAVEN_OPTS="-Dhttps.protocols=TLSv1.2
  -Dorg.slf4j.simpleLogger.showDateTime=true
  -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN"
export MAVEN_CLI_OPTS="--batch-mode --errors --show-version"

# Build the project
echo "Building project"
mvn -f "$PROJECT_ROOT" -q -s "$SETTINGS_FILE" -DskipTests install

# Run the fuzzing campaign
mvn -f "$PROJECT_ROOT" \
  -s "$SETTINGS_FILE" \
  -pl :zeugma-experiments \
  -P"$SUBJECT,$FRAMEWORK" \
  "$MERINGUE_PLUGIN":fuzz \
  "$MERINGUE_PLUGIN":analyze \
  -Dmeringue.argLine="$ARG_LINE" \
  -Dmeringue.duration="$DURATION" \
  -Dmeringue.outputDirectory="$OUTPUT_DIRECTORY"

# Record configuration information
echo "{
  \"subject\": \"$SUBJECT\",
  \"fuzzer\": \"$FUZZER\",
  \"commit_sha\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse HEAD)\",
  \"branch_name\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse --abbrev-ref HEAD)\",
  \"remote_origin_url\": \"$(git --git-dir "$PROJECT_ROOT/.git" config --get remote.origin.url)\"
}" >"$OUTPUT_DIRECTORY/fuzz-info.json"
echo "$CONFIG_INFO"

# Create a TAR archive of the Meringue output directory and move it to the result directory
ARCHIVE_NAME='meringue.tgz'
mkdir -p "$RESULTS_DIRECTORY"
tar -czf "$ARCHIVE_NAME" -C "$OUTPUT_DIRECTORY" .
mv "$ARCHIVE_NAME" "$RESULTS_DIRECTORY/"

# Copy all normal files in the Meringue output directory to the results directory
find "$OUTPUT_DIRECTORY" -maxdepth 1 -type f -exec cp -t "$RESULTS_DIRECTORY" {} +
# If Zeugma's statistics file exists, copy it to the results directory
ZEUGMA_STATS_FILE="$OUTPUT_DIRECTORY/campaign/statistics.csv"
[ -f "$ZEUGMA_STATS_FILE" ] && cp "$ZEUGMA_STATS_FILE" "$RESULTS_DIRECTORY/zeugma.csv"
# If Zest's statistics file exists, copy it to the results directory
ZEST_STATS_FILE="$OUTPUT_DIRECTORY/campaign/plot_data"
[ -f "$ZEST_STATS_FILE" ] && cp "$ZEST_STATS_FILE" "$RESULTS_DIRECTORY/zest.csv"