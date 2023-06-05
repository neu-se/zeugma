import html
import os.path
import sys

import pandas as pd

import extract_coverage
from coverage_report import write_report


def list_unique_failures(detections):
    # Take the first detecting trial for each unique failure
    unique_failures = detections.groupby(['failure_id', 'subject', 'type', 'trace'], group_keys=True) \
        [['fuzzer', 'inducingInputs', 'trial']] \
        .apply(lambda x: x.sort_values('fuzzer', ascending=False).iloc[0]) \
        .sort_index() \
        .reset_index()
    # Take the first inducing input list for the selected trial for each failure
    unique_failures['inducing_input'] = unique_failures['inducingInputs'] \
        .apply(lambda x: x[0]) \
        .apply(lambda x: x[x.index('/meringue/campaign/') + len('/meringue/campaign/'):])
    return unique_failures.drop(columns=['inducingInputs'])


def read_detections(trials, job_dir):
    detections = pd.concat([t.get_failure_data() for t in trials]) \
        .reset_index(drop=True) \
        .sort_values(['subject', 'type', 'trace'])
    detections['failure_id'] = detections.groupby(['subject', 'type', 'trace']).ngroup()
    detections['trial'] = detections['trial'].apply(lambda x: os.path.relpath(x, job_dir))
    return detections.sort_values('failure_id').reset_index(drop=True)


def style_unique(unique):
    styles = [
        dict(selector='thead', props='border-bottom: black solid 1px;'),
        dict(selector='*', props='font-size: 12px; font-weight: normal; text-align: left; padding: 5px;'),
        dict(selector='tbody tr:nth-child(odd)', props='background-color: rgb(240,240,240);'),
        dict(selector='',
             props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
    ]
    return unique.style \
        .format_index(axis=1, formatter=lambda x: x.replace('_', ' ').title()) \
        .format({'trace': lambda x: "<br>".join(f"{html.escape(repr(y))}" for y in x)}) \
        .set_table_styles(styles) \
        .hide(axis="index")


def main():
    job_dir = sys.argv[1]
    trials = extract_coverage.collect_trials(job_dir)
    detections = read_detections(trials, job_dir)
    unique = list_unique_failures(detections)
    write_report(sys.argv[2], 'Detected Failures', style_unique(unique).to_html())


if __name__ == "__main__":
    main()
