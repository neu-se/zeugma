import base64
import copy
import os
import pathlib
import sys
from io import BytesIO

import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import pandas as pd
import scipy

TEMPLATE_FILE_NAME = 'template.html'

A12_BOUNDS = [0.56, 0.64, 0.71]
P_BOUNDS = [0.001, 0.01, 0.05]
ODDS_RATIO_BOUNDS = [1.25, 1.5, 2.0]


def a12(values1, values2):
    """
    Returns Vargha-Delaney A12 statistic.
    Vargha, A., & Delaney, H. D. (2000).
    A Critique and Improvement of the "CL" Common Language Effect Size Statistics of McGraw and Wong.
    Journal of Educational and Behavioral Statistics, 25(2), 101–132.
    https://doi.org/10.2307/1165329
    """
    a = scipy.stats.mannwhitneyu(values1, values2)[0] / (len(values1) * len(values2))
    return 1 - a if a < 0.5 else a


def mann_whitney(values1, values2):
    return scipy.stats.mannwhitneyu(values1, values2, alternative='two-sided', use_continuity=True)[1]


def fisher_exact(truth_values1, truth_values2):
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    return scipy.stats.fisher_exact(table, alternative='two-sided')[1]


def odds_ratio(truth_values1, truth_values2):
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    return scipy.stats.fisher_exact(table, alternative='two-sided')[0]


def compute_class(value, bounds):
    for i, bound in enumerate(bounds):
        if value < bound:
            return i
    return len(bounds)


def pairwise_heatmap(data, x, y, caption):
    if data[y].dtypes == bool:
        f1 = fisher_exact
        f2 = odds_ratio
        bounds2 = ODDS_RATIO_BOUNDS
    elif pd.api.types.is_numeric_dtype(data[y]):
        f1 = mann_whitney
        f2 = a12
        bounds2 = A12_BOUNDS
    else:
        raise ValueError
    unique_x = sorted(data[x].unique())
    text = [[None for _ in unique_x] for _ in unique_x]
    classes = copy.deepcopy(text)
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            values1 = data[data[x] == x1][y]
            values2 = data[data[x] == x2][y]
            if r == c:
                text[r][c] = x1
                classes[r][c] = 'label'
            elif r > c:
                z = f1(values1, values2)
                text[r][c] = f'{z:.4f}'
                classes[r][c] = f'p{compute_class(z, P_BOUNDS)}'
            else:
                z = f2(values1, values2)
                text[r][c] = f'{z:.4f}'
                classes[r][c] = f'e{compute_class(z, bounds2)}'

    return pd.DataFrame(text).style \
        .set_table_attributes('class="heatmap"') \
        .set_td_classes(pd.DataFrame(classes)) \
        .set_caption(caption) \
        .hide(axis="index") \
        .hide(axis="columns")


def sample_at_time(data, time):
    return data[data['time'] == max(data['time'][data['time'] <= time])]


def create_coverage_plot(data):
    plt.rcParams["font.family"] = 'sans-serif'
    fig, ax = plt.subplots(figsize=(8, 4))
    fuzzers = sorted(data['fuzzer'].unique())
    colors = [k for k in mcolors.TABLEAU_COLORS]
    stats = data.groupby(by=['time', 'fuzzer'])['covered_branches'] \
        .agg([min, max, 'median']) \
        .reset_index() \
        .sort_values('time')
    for color, fuzzer in zip(colors, fuzzers):
        selected = stats[stats['fuzzer'] == fuzzer]
        times = (selected['time'] / pd.to_timedelta(1, 'm')).tolist()
        ax.plot(times, selected['median'], color=color, label=fuzzer)
        ax.fill_between(times, selected['min'], selected['max'], color=color, alpha=0.2)
    ax.set_xlabel('Time (Minutes)')
    ax.set_ylabel('Covered Branches')
    ax.xaxis.get_major_locator().set_params(integer=True)
    ax.yaxis.get_major_locator().set_params(integer=True)
    ax.set_ylim(bottom=0)
    ax.set_xlim(left=0)
    plt.legend(bbox_to_anchor=(0, 1.02, 1, 0.2), loc="lower left", mode="expand", borderaxespad=0, ncol=4)
    return fig


def sample_at_times(data, ideal_times):
    slice_times = {max(data['time'][data['time'] < ideal]): ideal for ideal in ideal_times}
    samples = pd.DataFrame(data[data['time'].apply(lambda x: x in slice_times)])
    samples['time'].replace(slice_times, inplace=True)
    return samples


def format_time_delta(time_delta):
    return f'{round(time_delta / pd.to_timedelta(1, "m"), 3)} Minutes'


def create_stats_table(data):
    stats = data.groupby(by=['time', 'fuzzer'])['covered_branches'] \
        .agg([min, 'median', max, 'count']) \
        .reset_index()
    stats.columns = stats.columns.map(lambda x: x.replace('_', ' ').title())
    stats = stats.pivot_table(index='Fuzzer', columns='Time') \
        .swaplevel(axis=1) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .reindex(['Count', 'Min', 'Median', 'Max'], axis=1, level=1) \
        .rename(columns={'Median': 'Med'})
    stats.index.name = None
    stats.columns.names = (None, None)
    stats.columns = stats.columns.map(lambda x: (format_time_delta(x[0]), x[1]))
    formats = {c: '{:.0f}' if c[1] == 'Count' else '{:.1f}' for c in stats.columns}
    return stats.style.format(formats) \
        .set_caption('Covered Branches') \
        .set_table_attributes('class="coverage_table"')


def read_template():
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'resources', 'template.html'), 'r') as f:
        return f.read()


def create_subject_div(data, subject, slice_times):
    create_coverage_plot(data)
    coverage_plot = BytesIO()
    plt.savefig(coverage_plot, dpi=600, bbox_inches='tight', format='png')
    plt.close()
    slices = sample_at_times(data, slice_times)
    stats_table = create_stats_table(slices).to_html()
    content = f'<div class="overview">{plot_to_image(coverage_plot)}{stats_table}</div>'
    content += '<div class="slices">'
    if len(slices['fuzzer'].unique()) > 1:
        for time in slices['time'].unique():
            content += pairwise_heatmap(slices[slices['time'] == time], 'fuzzer', 'covered_branches',
                                        format_time_delta(time)).to_html()
    content += "</div>"
    return f'<div class="subject" id="{subject}"><h2>{subject.title()}</h2>{content}</div>'


def plot_to_image(plot):
    return f'<img src="data:image/png;base64,{base64.b64encode(plot.getvalue()).decode("utf-8")}">'


def read_coverage_data(file):
    data = pd.read_csv(file)
    data['time'] = pd.to_timedelta(data['time'])
    return data


def compute_ideal_slice_times(data):
    max_duration = max(data['time'])
    targets = [pd.to_timedelta(5, 'm'), pd.to_timedelta(15, 'm'), pd.to_timedelta(1, 'h'),
               pd.to_timedelta(3, 'h')]
    return [max_duration] if max_duration not in targets else targets[:targets.index(max_duration) + 1]


def main():
    data = read_coverage_data(sys.argv[1])
    output_file = sys.argv[2]
    os.makedirs(pathlib.Path(output_file).parent, exist_ok=True)
    ideal_slice_times = compute_ideal_slice_times(data)
    subjects = sorted(data['subject'].unique())
    report = read_template() \
        .replace('$1', ''.join(f'<a href="#{s}">{s.title()}</a>' for s in subjects)) \
        .replace('$2', ''.join(create_subject_div(data[data['subject'] == s], s, ideal_slice_times) for s in subjects))
    with open(output_file, 'w') as f:
        f.write(report)


if __name__ == "__main__":
    main()
