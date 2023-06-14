import json
import os
import pathlib

import pandas as pd

import campaign
import report
import report_util


def style_table(table, precision=3, color=None, props='color:{violet};'):
    styles = [
        dict(selector='thead', props='border-bottom: black solid 1px;'),
        dict(selector='*', props='font-size: 12px; font-weight: normal; text-align: right; padding: 5px;'),
        dict(selector='',
             props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
    ]
    return table.style.format(precision=precision, na_rep='---') \
        .highlight_max(subset=None, axis=1, color=color, props=props) \
        .set_table_styles(styles)


def create_heritability_table(heritability_csv):
    df = pd.read_csv(heritability_csv)
    df.columns = df.columns.map(lambda x: x.replace('_', ' '))
    df = df.groupby(by=['crossover operator', 'subject'])[['inheritance rate', 'hybrid']] \
        .agg(['median', 'mean']) \
        .reset_index()
    df.columns = df.columns.map(lambda x: ' '.join(x[::-1]).strip().title())
    df = df.drop(columns=['Median Hybrid']) \
        .rename(columns={'Mean Hybrid': '%HY',
                         'Median Inheritance Rate': 'Median IR',
                         'Mean Inheritance Rate': 'Mean IR'}) \
        .pivot(index=['Subject'], values=['Median IR', 'Mean IR', '%HY'], columns=['Crossover Operator']) \
        .stack(level=0) \
        .sort_index(axis=1) \
        .sort_index(axis=0)
    df.index.name = None
    df.index.names = ['Subject', 'Metric']
    df.columns.names = [None]
    return df


def create_failures_table(campaigns):
    failures = pd.concat([t.get_failure_data() for t in campaigns]) \
        .reset_index(drop=True) \
        .sort_values(['subject', 'type', 'trace'])
    # Read known failures
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'data', 'failures.json'), 'r') as f:
        known_failures = json.load(f)
    for f in known_failures:
        f['trace'] = tuple(map(lambda y: campaign.StackTraceElement(**y), f['trace']))
    # Match failures against known failures which have been manually mapped to defects
    return pd.merge(failures, pd.DataFrame.from_records(known_failures), on=["subject", "type", "trace"], how="left")


def create_campaign_count_table(campaigns):
    # Count the number of campaigns for each fuzzer on each subject
    return pd.DataFrame.from_records(
        {'fuzzer': t.fuzzer, 'subject': t.subject, 'campaign_id': t.id} for t in campaigns) \
        .groupby(['subject', 'fuzzer']) \
        .apply('count') \
        .reset_index()


def compute_detection_rates(detections, time, campaign_counts, full_index):
    # Select detections at or before the cut-off time
    # Count the number of detections of each defect for each fuzzer
    counts = detections[detections['time'] <= time] \
        .groupby(by=['subject', 'defect', 'fuzzer'])['campaign_id'] \
        .agg(['count']) \
        .reset_index()
    counts = pd.merge(counts, campaign_counts, on=["subject", "fuzzer"], how="left")
    # Compute the detection rate from the counts and the number of campaigns
    counts['detection rate'] = counts['count'] / counts['campaign_id']
    # Fill in zeros for missing values
    rates = counts[['defect', 'fuzzer', 'detection rate']] \
        .set_index(['defect', 'fuzzer']) \
        .reindex(full_index, fill_value=0) \
        .reset_index()
    rates['time'] = time
    return rates


def create_defect_table(campaigns):
    times = report.compute_slice_times(pd.to_timedelta(min(c.duration for c in campaigns), 'ms'))
    failures = create_failures_table(campaigns)
    # Find the first detection time of each defect for each campaign.
    detections = failures[failures['associatedDefects'].notnull()][
        ['subject', 'campaign_id', 'fuzzer', 'detection_time', 'associatedDefects']] \
        .explode('associatedDefects') \
        .dropna() \
        .rename(columns={'associatedDefects': 'defect', 'detection_time': 'time'}) \
        .groupby(['campaign_id', 'fuzzer', 'defect']) \
        .min() \
        .reset_index()
    counts = create_campaign_count_table(campaigns)
    full_index = pd.MultiIndex.from_product([detections['defect'].unique(), counts['fuzzer'].unique()],
                                            names=['defect', 'fuzzer'])
    rates = pd.concat([compute_detection_rates(detections, t, counts, full_index) for t in times])
    rates.columns = rates.columns.map(lambda c: c.replace('_', ' ').title())
    rates = rates.pivot(index=['Defect', 'Time'], values=['Detection Rate'], columns=['Fuzzer']) \
        .reorder_levels(axis=0, order=['Defect', 'Time']) \
        .reorder_levels(axis=1, order=['Fuzzer', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(1, axis=1)
    rates.index.name = None
    rates.columns.names = [None]
    rates.index = rates.index.map(lambda x: (x[0], report_util.format_time_delta(x[1])))
    return rates


def create_coverage_table(data, times):
    data = data[data['time'].isin(times)]
    data = data.groupby(by=['time', 'fuzzer', 'subject'])['covered_branches'] \
        .agg(['median']) \
        .reset_index()
    data.columns = data.columns.map(str.title)
    data = data.pivot(index=['Subject', 'Time'], values=['Median'], columns=['Fuzzer']) \
        .reorder_levels(axis=0, order=['Subject', 'Time']) \
        .reorder_levels(axis=1, order=['Fuzzer', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(1, axis=1)
    data.index.name = None
    data.index.names = (None, None)
    data.columns.names = [None]
    data.index = data.index.map(lambda x: (x[0], report_util.format_time_delta(x[1])))
    return data
