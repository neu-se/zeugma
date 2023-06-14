import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import pandas as pd

import extract_coverage
import report_util
import tables


def plot(data, subject):
    data = data[data['subject'] == subject]
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


def create_heatmap(data, subject, time):
    data = data[data['subject'] == subject]
    data = data[data['time'] == time]
    return report_util.pairwise_heatmap(data, 'fuzzer', 'covered_branches', report_util.format_time_delta(time))


def create_subject_subsection(coverage, subject, times):
    plot(coverage, subject)
    content = f'<div class="overview">{report_util.fig_to_html()}</div>'
    content += '<div class="slices">'
    for time in times:
        content += create_heatmap(coverage, subject, time).to_html()
    content += "</div>"
    return f'<div class="subject" id="{subject}"><h3>{subject.title()}</h3>{content}</div>'


def create(campaigns, times):
    print('Creating coverage section.')
    print("\tExtracting coverage data.")
    coverage = extract_coverage.create_coverage_csv(campaigns, times)
    print("\t\tExtracted coverage data.")
    summary = tables.create_coverage_table(coverage, times)
    content = tables.style_table(summary, precision=1) \
        .set_caption('Median Branch Coverage') \
        .to_html()
    subjects = sorted(coverage['subject'].unique())
    for subject in subjects:
        print(f"\tCreating {subject} subsection.")
        content += create_subject_subsection(coverage, subject, times)
        print(f"\t\tCreated {subject} subsection.")
    print(f'\tCreated coverage section.')
    return f'<div id="coverage"><h2>Coverage</h2>{content}</div>'
