import json
import os
import sys

import numpy as np
import pandas as pd

FAILURES_FILE_NAME = 'failures.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'
MAX_SAMPLES = 1000


class Trial:
    def __init__(self, trial_dir):
        self.trial_dir = trial_dir
        self.coverage_file = os.path.join(trial_dir, COVERAGE_FILE_NAME)
        self.summary_file = os.path.join(trial_dir, SUMMARY_FILE_NAME)
        self.failures_file = os.path.join(trial_dir, FAILURES_FILE_NAME)
        self.valid = all(os.path.isfile(f) for f in [self.coverage_file, self.summary_file, self.failures_file])
        if self.valid:
            with open(self.summary_file, 'r') as f:
                summary = json.load(f)
                self.subject = summary['configuration']['testClassName'].split('.')[-1].replace('Fuzz', '')
                self.fuzzer = summary['frameworkClassName'].split('.')[-1].replace('Framework', '')
                if self.fuzzer == 'BeDivFuzz':
                    if '-Djqf.div.SAVE_ONLY_NEW_STRUCTURES=true' in summary['configuration']['javaOptions']:
                        self.fuzzer += '-Structure'
                    else:
                        self.fuzzer += '-Simple'
                elif self.fuzzer == 'Zeugma':
                    crossover_type = 'None'
                    for opt in summary['configuration']['javaOptions']:
                        if opt.startswith('-Dzeugma.crossover='):
                            crossover_type = opt[len('-Dzeugma.crossover='):].title()
                    self.fuzzer += "-" + crossover_type
                self.duration = summary['configuration']['duration']

    def get_coverage_data(self, duration):
        data = pd.read_csv(self.coverage_file) \
            .rename(columns=lambda x: x.strip())
        data['time'] = pd.to_timedelta(data['time'], 'ms')
        coverage = resample(data, duration)
        coverage['subject'] = self.subject
        coverage['fuzzer'] = self.fuzzer
        coverage['trial'] = self.trial_dir
        return coverage


def resample(data, duration):
    number_of_samples = min(duration, MAX_SAMPLES)
    data = data.set_index('time').sort_index()
    # Create an index from 0 to duration milliseconds (inclusive) with number_of_samples sample times
    resample_index = pd.timedelta_range(start=pd.Timedelta(0, 'ms'), end=pd.Timedelta(duration, 'ms'), closed=None,
                                        periods=number_of_samples)
    # Create a placeholder data frame indexed at the sample times filled with NaNs
    placeholder = pd.DataFrame(np.NaN, index=resample_index, columns=data.columns)
    # Combine the placeholder data frame with the original
    # Replace the NaN's in placeholder with the last value at or before the sample time in the original
    data = data.combine_first(placeholder).ffill().fillna(0)
    # Drop times not in the resample index
    return data.loc[data.index.isin(resample_index)] \
        .reset_index() \
        .rename(columns={'index': 'time'}) \
        .drop_duplicates(subset=['time'])


def collect_trials(job_dir):
    trials = list(map(Trial, filter(os.path.isdir, [os.path.join(job_dir, f) for f in os.listdir(job_dir)])))
    print(f"Found {len(trials)} trials.")
    for trial in trials:
        if not trial.valid:
            print(f"> Missing results for {trial.trial_dir}")
    return list(filter(lambda t: t.valid, trials))


def extract(job_dir, output_file):
    trials = collect_trials(job_dir)
    duration = min(t.duration for t in trials)
    pd.concat([t.get_coverage_data(duration) for t in trials]) \
        .reset_index(drop=True) \
        .to_csv(output_file, index=False)
    print(f"Wrote coverage CSV to {output_file}.")


def main():
    extract(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
