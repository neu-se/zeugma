import json
import os
from dataclasses import dataclass

import pandas as pd

FAILURES_FILE_NAME = 'failures.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'


@dataclass(order=True, frozen=True)
class StackTraceElement:
    declaringClass: str
    fileName: str = None
    methodName: str = None
    lineNumber: int = -1

    def __repr__(self):
        if self.lineNumber == -2:
            x = 'Native Method'
        elif self.fileName is None:
            x = "Unknown Source"
        elif self.lineNumber >= 0:
            x = f"{self.fileName}:{self.lineNumber}"
        else:
            x = self.fileName
        return f"{self.declaringClass}.{self.methodName}({x})"


class Trial:
    """Represents the results of a fuzzing campaign performed by a specific fuzzer on a specific subject."""

    def __init__(self, trial_dir, job_dir):
        self.trial_dir = trial_dir
        self.trial_id = os.path.relpath(trial_dir, job_dir)
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

    def add_trial_info(self, df):
        df['subject'] = self.subject
        df['trial'] = self.trial_id
        df['fuzzer'] = self.fuzzer

    def get_coverage_data(self):
        df = pd.read_csv(self.coverage_file) \
            .rename(columns=lambda x: x.strip())
        df['time'] = pd.to_timedelta(df['time'], 'ms')
        self.add_trial_info(df)
        return df

    def get_failure_data(self):
        with open(self.failures_file, 'r') as f:
            records = json.load(f)
        if len(records) == 0:
            return pd.DataFrame()
        df = pd.DataFrame.from_records(records) \
            .rename(columns=lambda x: x.strip())
        df['type'] = df['failure'].apply(lambda x: x['type'])
        df['trace'] = df['trace'] = df['failure'].apply(
            lambda x: tuple(map(lambda y: StackTraceElement(**y), x['trace'])))
        df['detection_time'] = pd.to_timedelta(df['firstTime'], 'ms')
        df = df.rename(columns={'inducingInputs': 'inducing_inputs'})
        df = df[['type', 'trace', 'detection_time', 'inducing_inputs']]
        self.add_trial_info(df)
        return df


def collect_trials(job_dir):
    trial_dirs = filter(os.path.isdir, [os.path.join(job_dir, f) for f in os.listdir(job_dir)])
    trials = [Trial(t, job_dir) for t in trial_dirs]
    print(f"Found {len(trials)} trials.")
    for trial in trials:
        if not trial.valid:
            print(f"> Missing results for {trial.trial_dir}")
    return list(filter(lambda t: t.valid, trials))
