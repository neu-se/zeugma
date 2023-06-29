import os
import sys

import numpy as np
import pandas as pd

import campaign
import report_util


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


def write_coverage_csv(campaigns, times, file):
    print('Creating coverage CSV.')
    create_coverage_csv(campaigns, times).to_csv(file, index=False)
    print(f'\tWrote coverage CSV to {file}.')


def read_coverage_csv(file):
    df = pd.read_csv(file)
    df['time'] = pd.to_timedelta(df['time'])
    return df


def main():
    input_dir = sys.argv[1]
    campaigns = campaign.check_campaigns(campaign.find_campaigns(input_dir))
    times = report_util.compute_slice_times(pd.to_timedelta(min(c.duration for c in campaigns), 'ms'))
    write_coverage_csv(campaigns, times, os.path.join(input_dir, 'coverage.csv'))


if __name__ == "__main__":
    main()
