import json
import os
import pathlib
import sys

import numpy as np
import pandas as pd

import campaign


def resample(data, time_index):
    data = data.copy() \
        .set_index('time') \
        .sort_index()
    # Create a placeholder data frame indexed at the sample times filled with NaNs
    placeholder = pd.DataFrame(np.NaN, index=time_index, columns=data.columns)
    # Combine the placeholder data frame with the original
    # Replace the NaN's in placeholder with the last value at or before the sample time in the original
    data = data.combine_first(placeholder).ffill().fillna(0)
    # Drop times not in the resample index
    return data.loc[data.index.isin(time_index)] \
        .reset_index() \
        .rename(columns={'index': 'time'}) \
        .drop_duplicates(subset=['time'])


def create_coverage_csv(campaigns, times):
    duration = max(times)
    # Create an index from 0 to duration (inclusive) with 1000 sample times
    index = pd.timedelta_range(start=pd.Timedelta(0, 'ms'), end=duration, closed=None, periods=1000)
    # Ensure that the specified times are included in the index
    index = index.union(pd.TimedeltaIndex(sorted(times)))
    # Resample the data for each campaign at the index times
    return pd.concat([resample(c.get_coverage_data(), index) for c in campaigns]) \
        .reset_index()


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


def create_detections_table(campaigns):
    # 1. Read detected failure
    # 2. Remove failures not manually mapped to defects
    # 3. Transform each associated defect into a row
    # 4. Simplify column names.
    # 5. Remove rows for failures associated with no defects
    # 6. Find the first detection of each defect for each campaign
    # 7. Flatten the table and select desired columns
    return create_failures_table(campaigns) \
        .dropna(subset=['associatedDefects']) \
        .explode('associatedDefects') \
        .rename(columns={'associatedDefects': 'defect', 'detection_time': 'time'}) \
        .dropna(subset=['defect']) \
        .groupby(['campaign_id', 'fuzzer', 'defect', 'subject']) \
        .min() \
        .reset_index()[['campaign_id', 'fuzzer', 'subject', 'defect', 'time']]


def create_defects_csv(campaigns):
    # Find the first detection of each defect for each campaign
    detections = create_detections_table(campaigns)
    # If a campaign never detected a particular defect, fill in NaT
    defect_pairs = detections[['defect', 'subject']].drop_duplicates().itertuples(index=False, name=None)
    rows = []
    for defect, subject in defect_pairs:
        rows.extend([c.id, c.fuzzer, c.subject, defect, pd.NaT] for c in campaigns if c.subject == subject)
    return pd.DataFrame(rows, columns=['campaign_id', 'fuzzer', 'subject', 'defect', 'time']) \
        .set_index(['campaign_id', 'fuzzer', 'subject', 'defect']) \
        .combine_first(detections.set_index(['campaign_id', 'fuzzer', 'subject', 'defect'])) \
        .reset_index() \
        .sort_values(by=['campaign_id', 'defect'])


def extract_coverage_data(campaigns, times, output_dir):
    file = os.path.join(output_dir, 'coverage.csv')
    print('Creating coverage CSV.')
    coverage = create_coverage_csv(campaigns, times)
    coverage.to_csv(file, index=False)
    print(f'\tWrote coverage CSV to {file}.')
    return coverage


def extract_detections_data(campaigns, output_dir):
    file = os.path.join(output_dir, 'detections.csv')
    print('Creating defect detections CSV.')
    defects = create_defects_csv(campaigns)
    defects.to_csv(file, index=False)
    print(f'\tWrote defects detections CSV to {file}.')
    return defects


def main():
    input_dir = sys.argv[1]
    output_dir = sys.argv[2]
    campaigns = campaign.read_campaigns(input_dir)
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(3, 'h')]
    extract_coverage_data(campaigns, times, output_dir)
    extract_detections_data(campaigns, output_dir)


if __name__ == "__main__":
    main()
