import html
import json
import os
import pathlib
import sys

import pandas as pd

import trial
from coverage_report import format_time_delta, write_report


def read_known_failures():
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'data', 'failures.json'), 'r') as f:
        known_failures = json.load(f)
    for f in known_failures:
        f['trace'] = tuple(map(lambda y: trial.StackTraceElement(**y), f['trace']))
    return pd.DataFrame.from_records(known_failures)


def read_discovered_failures(trials):
    return pd.concat([t.get_failure_data() for t in trials]) \
        .reset_index(drop=True) \
        .sort_values(['subject', 'type', 'trace'])


def choose_representatives(failures):
    # Take the first detecting trial for each unique failure
    unique_failures = failures.groupby(['subject', 'type', 'trace'], group_keys=True)[
        ['fuzzer', 'inducing_inputs', 'trial']] \
        .apply(lambda x: x.sort_values('fuzzer', ascending=False).iloc[0]) \
        .sort_index() \
        .reset_index()
    # Take the first inducing input list for the selected trial for each failure
    unique_failures['inducing_input'] = unique_failures['inducing_inputs'] \
        .apply(lambda x: x[0]) \
        .apply(lambda x: x[x.index('/meringue/campaign/') + len('/meringue/campaign/'):])
    return unique_failures.drop(columns=['inducing_inputs'])


def style_failures(failures):
    styles = [
        dict(selector='thead', props='border-bottom: black solid 1px;'),
        dict(selector='*', props='font-size: 12px; font-weight: normal; text-align: left; padding: 5px;'),
        dict(selector='tbody tr:nth-child(odd)', props='background-color: rgb(240,240,240);'),
        dict(selector='',
             props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
    ]
    return failures.style \
        .format_index(axis=1, formatter=lambda x: x.replace('_', ' ').title()) \
        .format({'trace': lambda x: "<br>".join(f"{html.escape(repr(y))}" for y in x)}) \
        .set_table_styles(styles) \
        .set_caption("Unmatched Failures")


def get_defect_detections(failures):
    # Find the first detection time of each defect for each trial.
    return failures[failures['associatedDefects'].notnull()][
        ['subject', 'trial', 'fuzzer', 'detection_time', 'associatedDefects']] \
        .explode('associatedDefects') \
        .dropna() \
        .rename(columns={'associatedDefects': 'defect', 'detection_time': 'time'}) \
        .groupby(['trial', 'fuzzer', 'defect']) \
        .min() \
        .reset_index()


def count_trials(trials):
    return pd.DataFrame.from_records(
        {'fuzzer': t.fuzzer, 'subject': t.subject, 'trial': t.trial_id} for t in trials) \
        .groupby(['subject', 'fuzzer']) \
        .apply('count') \
        .reset_index()


def compute_detection_rates(detections, time, trial_counts, full_index):
    # Select detections at or before the cut-off time
    # Count the number of detections of each defect for each fuzzer
    counts = detections[detections['time'] <= time] \
        .groupby(by=['subject', 'defect', 'fuzzer'])['trial'] \
        .agg(['count']) \
        .reset_index()
    counts = pd.merge(counts, trial_counts, on=["subject", "fuzzer"], how="left")
    # Compute the detection rate from the counts and the number of trials
    counts['detection_rate'] = counts['count'] / counts['trial']
    # Fill in zeros for missing values
    rates = counts[['defect', 'fuzzer', 'detection_rate']].set_index(['defect', 'fuzzer']) \
        .reindex(full_index, fill_value=0) \
        .reset_index()
    rates['time'] = time
    return rates


def create_detection_rate_table(trials, failures, times):
    # Determine defect detections from failure detections
    detections = get_defect_detections(failures)
    # Count the number of trials for each fuzzer on each subject
    trial_counts = count_trials(trials)
    full_index = pd.MultiIndex.from_product([detections['defect'].unique(), trial_counts['fuzzer'].unique()],
                                            names=['defect', 'fuzzer'])
    return pd.concat([compute_detection_rates(detections, t, trial_counts, full_index) for t in times])


def style_detection_rates(rates, times):
    rates.columns = rates.columns.map(lambda c: c.replace('_', ' ').title())
    rates = rates.pivot(index=['Defect'], values=['Detection Rate'], columns=['Fuzzer', 'Time']) \
        .reorder_levels(axis=1, order=['Fuzzer', 'Time', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(2, axis=1)
    rates.index.name = None
    rates.columns.names = (None, None)
    rates.columns = rates.columns.map(lambda x: (x[0], format_time_delta(x[1])))
    styles = [
        dict(selector='.row_heading', props='text-align: left;'),
        dict(selector='.col_heading.level0', props='text-align: center; text-decoration: underline;'),
        dict(selector='.data, .col_heading.level1', props='text-align: right; padding: 0 0.5em;'),
        dict(selector='thead', props='border-bottom: black solid 1px;'),
        dict(selector='*', props='font-size: 12px; font-weight: normal;'),
        dict(selector=f'tr > td:nth-of-type({len(times)}n+1)', props='padding-left: 2em;'),
        dict(selector='tbody tr:nth-child(odd)', props='background-color: rgb(240,240,240);'),
        dict(selector='caption', props='text-align: left;'),
        dict(selector='',
             props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
    ]
    return rates.style \
        .format(formatter='{:.3f}', na_rep='---') \
        .set_caption('Detection Rates') \
        .set_table_styles(styles) \
        .to_html()


def main():
    job_dir = sys.argv[1]
    report_file = sys.argv[2]
    trials = trial.collect_trials(job_dir)
    failures = read_discovered_failures(trials)
    known_failures = read_known_failures()
    # Match failures against know failure which have been deduplicated
    failures = pd.merge(failures, known_failures, on=["subject", "type", "trace"], how="left")
    matched = failures[failures['associatedDefects'].notnull()]
    unmatched = failures[failures['associatedDefects'].isnull()]
    # Report an arbitrary, failure-inducing input for each unique, unmatched failure
    unmatched_reps = choose_representatives(unmatched)
    table1 = style_failures(unmatched_reps).to_html()
    # Compute detection rates for failures matching known failures
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(1, 'h'), pd.to_timedelta(3, 'h')]
    rates = create_detection_rate_table(trials, matched, times)
    table2 = style_detection_rates(rates, times)
    write_report(report_file, 'Detected Defects', table1 + table2)


if __name__ == "__main__":
    main()
