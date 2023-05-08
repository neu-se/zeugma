import json
import os
import sys

import numpy as np
import pandas as pd

INFO_FILE_NAME = 'fuzz-info.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'
ZEUGMA_STATS_FILE_NAME = 'zeugma.csv'
ZEST_STATS_FILE_NAME = 'zest.csv'
MAX_SAMPLES = 1000


class Trial:
    def __init__(self, trial_dir):
        self.trial_dir = trial_dir
        self.coverage_file = os.path.join(trial_dir, COVERAGE_FILE_NAME)
        self.info_file = os.path.join(trial_dir, INFO_FILE_NAME)
        self.summary_file = os.path.join(trial_dir, SUMMARY_FILE_NAME)
        self.zest_stats_file = os.path.join(trial_dir, ZEST_STATS_FILE_NAME)
        self.zeugma_stats_file = os.path.join(trial_dir, ZEUGMA_STATS_FILE_NAME)
        self.fuzzer, self.subject, self.coverage, self.duration, self.executions = None, None, None, None, None

    def is_valid(self):
        if all(os.path.isfile(f) for f in [self.coverage_file, self.info_file, self.summary_file]):
            return True
        else:
            print(f"> Missing results for {self.trial_dir}")
            return False

    def initialize_info(self):
        with open(self.info_file, 'r') as f:
            info = json.load(f)
            self.fuzzer = info['fuzzer']
            self.subject = info['subject']
        with open(self.summary_file, 'r') as f:
            summary = json.load(f)
            self.duration = summary['configuration']['duration']
        return self

    def initialize_coverage(self, duration):
        data = pd.read_csv(self.coverage_file) \
            .rename(columns=lambda x: x.strip()) \
            .rename(columns={'covered_branches': 'coverage'})
        data['time'] = pd.to_timedelta(data['time'], 'ms')
        self.coverage = resample(data, duration)
        self.coverage['subject'] = self.subject
        self.coverage['fuzzer'] = self.fuzzer
        self.coverage['trial'] = self.trial_dir

    def initialize_executions(self, duration):
        if os.path.isfile(self.zest_stats_file):
            data = pd.read_csv(self.zest_stats_file) \
                .rename(columns=lambda x: x.strip())
            data['executions'] = data['valid_inputs'] + data['invalid_inputs']
            data['time'] = pd.to_timedelta(data['# unix_time'], 's')
            min_time = min(data['time'])
            data['time'] = data['time'] - min_time
        elif os.path.isfile(self.zeugma_stats_file):
            data = pd.read_csv(self.zeugma_stats_file) \
                .rename(columns=lambda x: x.strip())
            data['time'] = pd.to_timedelta(data['elapsed_time_ms'], 'ms')
        else:
            return
        data = pd.DataFrame(data[['time', 'executions']])
        self.executions = resample(data, duration)
        self.executions['subject'] = self.subject
        self.executions['fuzzer'] = self.fuzzer
        self.executions['trial'] = self.trial_dir


def resample(data, duration):
    number_of_samples = min(duration, MAX_SAMPLES)
    data = data.set_index('time').sort_index()
    # Create an index from 0 to duration milliseconds (inclusive) with number_of_samples sample times
    resample_index = pd.timedelta_range(start=pd.Timedelta(0, 'ms'), end=pd.Timedelta(duration, 'ms'), closed=None,
                                        periods=number_of_samples)
    # Create a dummy data frame indexed at the sample times filled with NaNs
    dummy = pd.DataFrame(np.NaN, index=resample_index, columns=data.columns)
    # Combine the dummy data frame with the original replacing
    # the NaN's in dummy with the last value at or before the sample time in the original
    data = data.combine_first(dummy).ffill().fillna(0)
    # Drop times not in the resample index
    return data.loc[data.index.isin(resample_index)] \
        .reset_index() \
        .rename(columns={'index': 'time'}) \
        .drop_duplicates(subset=['time'])


def collect_trials(job_dir):
    trials = list(map(Trial, filter(os.path.isdir, [os.path.join(job_dir, f) for f in os.listdir(job_dir)])))
    print(f"Found {len(trials)} trials.")
    return list(map(Trial.initialize_info, filter(Trial.is_valid, trials)))


def extract(job_dir):
    coverage_file = os.path.join(job_dir, "coverage.csv")
    executions_file = os.path.join(job_dir, "executions.csv")
    trials = collect_trials(job_dir)
    duration = min(t.duration for t in trials)
    for t in trials:
        t.initialize_coverage(duration)
        t.initialize_executions(duration)
    pd.concat([t.coverage for t in trials]) \
        .reset_index(drop=True) \
        .to_csv(coverage_file, index=False)
    print(f"Wrote coverage CSV to {coverage_file}.")
    executions = [t.executions for t in trials if t.executions is not None]
    if len(executions) != 0:
        pd.concat(executions) \
            .reset_index(drop=True) \
            .to_csv(executions_file, index=False)
        print(f"Wrote executions CSV to {executions_file}.")
    else:
        print("Execution data not found.")


def main():
    extract(sys.argv[1])


if __name__ == "__main__":
    main()
