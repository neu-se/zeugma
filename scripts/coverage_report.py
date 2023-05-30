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
                text[r][c] = f'{z:.2E}'
                classes[r][c] = f'p{compute_class(z, P_BOUNDS)}'
            else:
                z = f2(values1, values2)
                text[r][c] = f'{z:.2f}'
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
    for unit in ['d', 'h', 'm', 's']:
        value = time_delta / pd.to_timedelta(1, unit)
        if value.is_integer():
            return f'{int(value)}{unit.upper()}'
    value = time_delta / pd.to_timedelta(1, 'm')
    return f'{round(value, 3)}M'


def create_coverage_table(data):
    ideal_slice_times = compute_ideal_slice_times(data)
    s = sample_at_times(data, ideal_slice_times) \
        .groupby(by=['time', 'fuzzer', 'subject'])['covered_branches'] \
        .agg(['median']) \
        .reset_index()
    s.columns = s.columns.map(str.title)
    s['Rank'] = s.groupby(by=['Time', 'Subject'])['Median'].rank(method='average', ascending=False)
    ranks = s.copy() \
        .drop(columns=['Median']) \
        .rename(columns={'Rank': 'Value'})
    ranks['Stat'] = 'Rank'
    medians = s.copy() \
        .drop(columns=['Rank']) \
        .rename(columns={'Median': 'Value'})
    medians['Stat'] = 'Med'
    s = pd.concat([medians, ranks])
    s = s.pivot(index=['Fuzzer', 'Stat'], values=['Value'], columns=['Subject', 'Time']) \
        .reorder_levels(axis=1, order=['Subject', 'Time', None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(2, axis=1)
    s.index.name = None
    s.index.names = (None, None)
    s.columns.names = (None, None)
    s.columns = s.columns.map(lambda x: (x[0], format_time_delta(x[1])))
    styles = [
        dict(selector='.row_heading', props='text-align: left;'),
        dict(selector='.col_heading.level0', props='text-align: center; text-decoration: underline;'),
        dict(selector='.data, .col_heading.level1', props='text-align: right; padding: 0 0.5em;'),
        dict(selector='thead', props='border-bottom: black solid 1px;'),
        dict(selector='*', props='font-size: 12px; font-weight: normal;'),
        dict(selector=f'tr > td:nth-of-type({len(ideal_slice_times)}n+1)', props='padding-left: 2em;'),
        dict(selector='tbody tr:nth-child(4n-2), tbody tr:nth-child(4n-3)',
             props='background-color: rgb(240,240,240);'),
        dict(selector='caption', props='text-align: left;'),
        dict(selector='',
             props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
    ]
    s = s.style.format(formatter='{:.1f}', na_rep='---') \
        .set_caption('Median Branch Coverage') \
        .set_table_styles(styles)
    return s.to_html()


def read_resource(name):
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'resources', name), 'r') as f:
        return f.read()


def create_subject_div(data, subject):
    data = data[data['subject'] == subject]
    create_coverage_plot(data)
    coverage_plot = BytesIO()
    plt.savefig(coverage_plot, dpi=600, bbox_inches='tight', format='png')
    plt.close()
    content = f'<div class="overview">{plot_to_image(coverage_plot)}</div>'
    content += '<div class="slices">'
    slices = sample_at_times(data, compute_ideal_slice_times(data))
    slices['fuzzer'] = slices['fuzzer'].apply(shorten_fuzzer)
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
    targets = [pd.to_timedelta(5, 'm'), pd.to_timedelta(1, 'h'), pd.to_timedelta(3, 'h')]
    return [max_duration] if max_duration not in targets else targets[:targets.index(max_duration) + 1]


def get_subject_list(data):
    return sorted(data['subject'].unique())


def shorten_fuzzer(name):
    return name.replace('Zeugma', 'Zg') \
        .replace('-Structure', '-St') \
        .replace('-Simple', '-Si') \
        .replace('-Linked', '-Li') \
        .replace('BeDivFuzz', 'BDF') \
        .replace('RLCheck', 'RL')


def rename_fuzzer(name):
    return name.replace('-None', '') \
        .replace('One_Point', '1PT') \
        .replace('Two_Point', '2PT')


def write_report(output_file, title, content):
    os.makedirs(pathlib.Path(output_file).parent, exist_ok=True)
    report = read_resource('template.html') \
        .replace('$title', title) \
        .replace('$content', content)
    with open(output_file, 'w') as f:
        f.write(report)


def main():
    data = read_coverage_data(sys.argv[1])
    data['fuzzer'] = data['fuzzer'].apply(rename_fuzzer)
    content = create_coverage_table(data) + ''.join(create_subject_div(data, s) for s in get_subject_list(data))
    write_report(sys.argv[2], 'Branch Coverage', content)


if __name__ == "__main__":
    main()
