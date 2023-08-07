#!/bin/bash
readonly REPOSITORY=$1
readonly BRANCH=$2
readonly JOB_NAME=$3
readonly SCRIPT_PATH=$4
readonly NUMBER_OF_TRIALS=$5
readonly CONFIGURATIONS_CSV=$6

readonly ARCHIVE_DIRECTORY="/experiment/$USER/archives/$REPOSITORY/"
readonly BASE_RESULTS_DIRECTORY="/experiment/$USER/results/$JOB_NAME/"
readonly SCRATCH="/scratch/temp/"
readonly ARCHIVE_CONFIGURATIONS_CSV="slurm-configuration.csv"

# Exit immediately if any simple command fails
set -e

# Compute the number of configurations and tasks
readonly NUMBER_OF_CONFIGURATIONS=$((1 + $(wc <"$CONFIGURATIONS_CSV" -l)))
readonly NUMBER_OF_TASKS=$((NUMBER_OF_CONFIGURATIONS * NUMBER_OF_TRIALS))

# Build an archive of the repository
# Create an empty temporary directory
rm -rf "$SCRATCH" && mkdir -p "$SCRATCH"
# Clone the the specified branch for the repository; truncate the history to a single commit
git clone --branch "$BRANCH" --depth 1 https://github.com/"$REPOSITORY".git "$SCRATCH"
# Copy the configurations file into the archive
cp "$CONFIGURATIONS_CSV" "$SCRATCH/$ARCHIVE_CONFIGURATIONS_CSV"
# Create an archive of the clone
tar -C "$SCRATCH" -cf experiment.tar .
# Move the archive to the archive directory
mkdir -p "$ARCHIVE_DIRECTORY"
commit_hash=$(git --git-dir "$SCRATCH"/.git rev-parse HEAD)
readonly ARCHIVE_FILE="$ARCHIVE_DIRECTORY/$commit_hash.tar"
mv experiment.tar "$ARCHIVE_FILE"
# Delete the temporary directory
rm -rf "$SCRATCH"

# Use a here-document to redirect a script into sbatch
sbatch <<EOT
#!/bin/bash
#SBATCH --job-name="$JOB_NAME"
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --output=/scratch/slurm.out
#SBATCH --error=/scratch/slurm.out
#SBATCH --partition all
#SBATCH --array=0-$((NUMBER_OF_TASKS - 1))

# Write a trace for each command to standard error
set -x

# Print the name of the current host system
hostname

# Print the start time
echo "Start time: \$(date)"

# Change the working directory
mkdir -p /scratch/experiment && cd /scratch/experiment

# Copy the repository archive and untar it
cp "$ARCHIVE_FILE" experiment.tar && tar -xf experiment.tar

# Create the results directory
readonly RESULTS_DIRECTORY="$BASE_RESULTS_DIRECTORY/slurm-\${SLURM_ARRAY_JOB_ID}/\${SLURM_ARRAY_TASK_ID}"
mkdir -p "\$RESULTS_DIRECTORY"

# Extract the configuration
readonly INDEX="\$SLURM_ARRAY_TASK_ID"
IFS="," read -r -a array <<< "\$(sed "\$(((INDEX % $NUMBER_OF_CONFIGURATIONS) + 1))q;d" "$ARCHIVE_CONFIGURATIONS_CSV")"

# Run the script and pass along any extra arguments supplied to this script
bash "$SCRIPT_PATH" "\$RESULTS_DIRECTORY" "\${array[@]}" ${@:7}
exit_code=\$?
echo "Script exited with code \$exit_code"

# Print the end time
echo "End time: \$(date)"

# Copy the stdout and stderr file to the results directory
cp "/scratch/slurm.out" "\$RESULTS_DIRECTORY/"

exit "\$exit_code"
EOT
