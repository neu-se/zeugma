import base64
from io import BytesIO

import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import pandas as pd

import report_util
import tables


def plot(data, subject, cmap):
    fuzzers = sorted(data['fuzzer'].unique())
    if cmap is None:
        colors = [k for k in mcolors.TABLEAU_COLORS]
        cmap = {k[0]: k[1] for k in zip(fuzzers, colors)}
    data = data[data['subject'] == subject]
    plt.rcParams["font.family"] = 'sans-serif'
    fig, ax = plt.subplots(figsize=(8, 4))
    stats = data.groupby(by=['time', 'fuzzer'])['covered_branches'] \
        .agg([min, max, 'median']) \
        .reset_index() \
        .sort_values('time')
    for fuzzer in fuzzers:
        color = cmap[fuzzer]
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
    ax.set_title(subject.title())
    return fig


def plot_legend():
    ax1 = plt.gca()
    fig, ax = plt.subplots(figsize=(8, 1))
    ax.axis('off')
    legend = ax.legend(*ax1.get_legend_handles_labels(), frameon=False, loc='lower center', ncol=8)
    fig = legend.figure
    fig.canvas.draw()
    bbox = legend.get_window_extent().transformed(fig.dpi_scale_trans.inverted())
    buffer = BytesIO()
    plt.savefig(buffer, dpi=600, bbox_inches=bbox, format='png')
    plt.close()
    return f'<img src="data:image/png;base64,{base64.b64encode(buffer.getvalue()).decode("utf-8")}">'


def create_plots_subsection(data):
    subjects = sorted(data['subject'].unique())
    fuzzers = sorted(data['fuzzer'].unique())
    colors = [k for k in mcolors.TABLEAU_COLORS]
    cmap = {k[0]: k[1] for k in zip(fuzzers, colors)}
    content = ''
    for subject in subjects:
        plot(data, subject, cmap)
        content += report_util.fig_to_html(close=False)
    return f'<div><h3>Coverage Over Time</h3>{plot_legend()}<div class="wrapper">{content}</div></div>'


def create_pairwise_subsection(data):
    content = ''
    groups = data[['subject', 'time']].drop_duplicates().sort_values(by=['subject', 'time']) \
        .itertuples(index=False, name=None)
    for subject, time in groups:
        selected = report_util.select(data, subject=subject, time=time)
        caption = f'{subject} at {report_util.format_time_delta(time)}.'
        content += report_util.pairwise_heatmap(selected, 'fuzzer', 'covered_branches', caption) \
            .to_html()
    return f'<div><h3>Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def create(data, times):
    print('Creating coverage section.')
    content = tables.create_coverage_table(data, times).to_html()
    content += create_pairwise_subsection(data[data['time'].isin(times)])
    content += create_plots_subsection(data)
    print(f'\tCreated coverage section.')
    return f'<div><h2>Coverage</h2>{content}</div>'
