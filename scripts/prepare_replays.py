import os
import pathlib
import sys
import tarfile

import extract_coverage
import failure_report


def extract_input(output_dir, row):
    archive = row['archive']
    name = row['inducing_input']
    failure_id = row['failure_id']
    print(f"Extracting input for failure: #{failure_id}")
    with tarfile.open(archive, 'r') as tar:
        for member in tar.getmembers():
            if member.name == os.path.join('.', 'campaign', name):
                member.name = f"{failure_id}.dat"
                tar.extract(member, output_dir)
            elif 'BeDivFuzz' in row['fuzzer'] and member.name == os.path.join('.', 'campaign', f"{name}_secondary"):
                member.name = f"{failure_id}_secondary.dat"
                tar.extract(member, output_dir)


def main():
    job_dir = sys.argv[1]
    output_dir = sys.argv[2]
    os.makedirs(pathlib.Path(output_dir).parent, exist_ok=True)
    trials = extract_coverage.collect_trials(job_dir)
    detections = failure_report.read_detections(trials, job_dir)
    unique = failure_report.list_unique_failures(detections)
    unique['archive'] = unique['trial'].apply(lambda x: os.path.join(job_dir, x, 'meringue.tgz'))
    for index, row in unique.iterrows():
        extract_input(output_dir, row)


if __name__ == "__main__":
    main()
