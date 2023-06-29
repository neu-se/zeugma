import json
import os
from dataclasses import dataclass

import pandas as pd

FAILURES_FILE_NAME = 'failures.json'
SUMMARY_FILE_NAME = 'summary.json'
COVERAGE_FILE_NAME = 'coverage.csv'


@dataclass(order=True, frozen=True)
class StackTraceElement:
    """Represents an element in a Java stack trace."""
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


class Campaign:
    """Represents the results of a fuzzing campaign."""

    def __init__(self, campaign_dir):
        self.id = os.path.basename(campaign_dir)
        self.coverage_file = os.path.join(campaign_dir, COVERAGE_FILE_NAME)
        self.summary_file = os.path.join(campaign_dir, SUMMARY_FILE_NAME)
        self.failures_file = os.path.join(campaign_dir, FAILURES_FILE_NAME)
        self.valid = all(os.path.isfile(f) for f in [self.coverage_file, self.summary_file, self.failures_file])
        if self.valid:
            with open(self.summary_file, 'r') as f:
                summary = json.load(f)
                self.subject = summary['configuration']['testClassName'].split('.')[-1].replace('Fuzz', '')
                self.fuzzer = Campaign.get_fuzzer(summary)
                self.duration = summary['configuration']['duration']

    @staticmethod
    def get_fuzzer(summary):
        fuzzer = summary['frameworkClassName'].split('.')[-1].replace('Framework', '')
        if fuzzer == 'BeDivFuzz':
            if '-Djqf.div.SAVE_ONLY_NEW_STRUCTURES=true' in summary['configuration']['javaOptions']:
                fuzzer += '-Structure'
            else:
                fuzzer += '-Simple'
        elif fuzzer == 'Zeugma':
            crossover_type = 'None'
            for opt in summary['configuration']['javaOptions']:
                if opt.startswith('-Dzeugma.crossover='):
                    crossover_type = opt[len('-Dzeugma.crossover='):].title()
            fuzzer += "-" + crossover_type
        return fuzzer.replace('-None', '') \
            .replace('One_Point', '1PT') \
            .replace('Two_Point', '2PT')

    def add_trial_info(self, df):
        df['subject'] = self.subject
        df['campaign_id'] = self.id
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


def find_campaigns(input_dir):
    print(f'Searching for campaigns in {input_dir}.')
    files = [os.path.join(input_dir, f) for f in os.listdir(input_dir)]
    campaigns = list(map(Campaign, filter(os.path.isdir, files)))
    print(f"\tFound {len(campaigns)} campaigns.")
    return campaigns


def check_campaigns(campaigns):
    print(f'Checking campaigns.')
    result = []
    for c in campaigns:
        if not c.valid:
            print(f"\tMissing required files for {c.id}.")
        else:
            result.append(c)
    print(f'\t{len(result)} campaigns were valid.')
    return result
