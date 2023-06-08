import sys

import numpy as np
import pandas as pd

import trial

MAX_SAMPLES = 1000


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


def extract(job_dir, output_file):
    trials = trial.collect_trials(job_dir)
    duration = min(t.duration for t in trials)
    pd.concat([resample(t.get_coverage_data(duration), duration) for t in trials]) \
        .reset_index(drop=True) \
        .to_csv(output_file, index=False)
    print(f"Wrote coverage CSV to {output_file}.")


def main():
    extract(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
