import json
import os
import pathlib

import pandas as pd

import campaign
import report_util


def highlight_max(data, props):
    if data.index.nlevels == 2:
        is_max = data.groupby(level=1).transform('max') == data
    else:
        is_max = data == data.max()
    return is_max.replace({True: props, False: ''})


def style_table(table, precision=3, axis=1):
    return table.style.format(precision=precision, na_rep='---') \
        .apply(lambda x: highlight_max(x, 'background-color: yellow;'), axis=axis)


def create_heritability_table(heritability_csv):
    df = pd.read_csv(heritability_csv)
    df.columns = df.columns.map(lambda x: x.replace('_', ' '))
    df = df.groupby(by=['crossover operator', 'subject'])[['inheritance rate', 'hybrid']] \
        .agg(['median', 'mean']) \
        .reset_index()
    df.columns = df.columns.map(lambda x: ' '.join(x[::-1]).strip().title())
    df = df.drop(columns=['Median Hybrid', 'Mean Inheritance Rate']) \
        .rename(columns={'Mean Hybrid': 'HY', 'Median Inheritance Rate': 'IR'}) \
        .pivot(index=['Subject'], values=['HY', 'IR'], columns=['Crossover Operator']) \
        .reorder_levels(axis=1, order=['Crossover Operator', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0)
    df.index.name = 'Subject'
    df.columns.names = [None, None]
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


def create_detections_table(campaigns):
    # 1. Read detected failure
    # 2. Remove failures not manually mapped to defects
    # 3. Transform each associated defect into a row
    # 4. Remove rows for failures associated with no defects
    # 5. Find the first detection of each defect for each campaign
    return create_failures_table(campaigns) \
        .dropna(subset=['associatedDefects']) \
        .explode('associatedDefects') \
        .rename(columns={'associatedDefects': 'defect', 'detection_time': 'time'}) \
        .dropna(subset=['defect']) \
        .groupby(['campaign_id', 'fuzzer', 'defect', 'subject']) \
        .min() \
        .reset_index()[['campaign_id', 'fuzzer', 'subject', 'defect', 'time']]


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


def create_defect_table(campaigns, times):
    detections = create_detections_table(campaigns)
    counts = create_campaign_count_table(campaigns)
    full_index = pd.MultiIndex.from_product([detections['defect'].unique(), counts['fuzzer'].unique()],
                                            names=['defect', 'fuzzer'])
    rates = pd.concat([compute_detection_rates(detections, t, counts, full_index) for t in times])
    return pivot(rates, 'defect', 'fuzzer', 'detection rate')


def create_coverage_table(data, times):
    data = data[data['time'].isin(times)]
    data = data.groupby(by=['time', 'fuzzer', 'subject'])['covered_branches'] \
        .agg(['median']) \
        .reset_index()
    return pivot(data, 'subject', 'fuzzer', 'median')


def pivot(data, col, row, value):
    data = data.pivot(index=[row], values=[value], columns=[col, 'time']) \
        .reorder_levels(axis=1, order=[col, 'time', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(2, axis=1)
    data.index.name = data.index.name.title()
    data.columns.names = [col.title(), None]
    data.columns = data.columns.map(lambda l: (l[0], report_util.format_time_delta(l[1])))
    return data
