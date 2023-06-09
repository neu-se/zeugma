import matplotlib.colors as mcolors
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

import report_util


class CoverageData:
    def __init__(self, campaigns, times):
        self.times = sorted(times)
        self.duration = max(times)
        # Create an index from 0 to duration (inclusive) with 1000 sample times
        index = pd.timedelta_range(start=pd.Timedelta(0, 'ms'), end=self.duration, closed=None, periods=1000)
        # Ensure that the specified times are included
        index = index.union(pd.TimedeltaIndex(sorted(times)))
        # Resample at the index times
        self.data = pd.concat([CoverageData.resample(c.get_coverage_data(), index) for c in campaigns]).reset_index()

    @staticmethod
    def resample(data, time_index):
        data = data.copy()
        data = data.set_index('time').sort_index()
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

    def plot(self, subject):
        data = self.data[self.data['subject'] == subject]
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

    def to_summary_table(self):
        data = self.data
        data = data[data['time'].isin(self.times)]
        data = data.groupby(by=['time', 'fuzzer', 'subject'])['covered_branches'] \
            .agg(['median']) \
            .reset_index()
        data.columns = data.columns.map(str.title)
        data['Rank'] = data.groupby(by=['Time', 'Subject'])['Median'].rank(method='average', ascending=False)
        ranks = data.copy() \
            .drop(columns=['Median']) \
            .rename(columns={'Rank': 'Value'})
        ranks['Stat'] = 'Rank'
        medians = data.copy() \
            .drop(columns=['Rank']) \
            .rename(columns={'Median': 'Value'})
        medians['Stat'] = 'Med'
        data = pd.concat([medians, ranks])
        data = data.pivot(index=['Fuzzer', 'Stat'], values=['Value'], columns=['Subject', 'Time']) \
            .reorder_levels(axis=1, order=['Subject', 'Time', None]) \
            .sort_index(axis=1) \
            .sort_index(axis=0) \
            .droplevel(2, axis=1)
        data.index.name = None
        data.index.names = (None, None)
        data.columns.names = (None, None)
        data.columns = data.columns.map(lambda x: (x[0], report_util.format_time_delta(x[1])))
        styles = [
            dict(selector='.row_heading', props='text-align: left;'),
            dict(selector='.col_heading.level0', props='text-align: center; text-decoration: underline;'),
            dict(selector='.data, .col_heading.level1', props='text-align: right; padding: 0 0.5em;'),
            dict(selector='thead', props='border-bottom: black solid 1px;'),
            dict(selector='*', props='font-size: 12px; font-weight: normal;'),
            dict(selector=f'tr > td:nth-of-type({len(self.times)}n+1)', props='padding-left: 2em;'),
            dict(selector='tbody tr:nth-child(4n-2), tbody tr:nth-child(4n-3)',
                 props='background-color: rgb(240,240,240);'),
            dict(selector='caption', props='text-align: left;'),
            dict(selector='',
                 props='border-bottom: black 1px solid; border-top: black 1px solid; border-collapse: collapse;')
        ]
        return data.style.format(formatter='{:.1f}', na_rep='---') \
            .set_caption('Median Branch Coverage') \
            .set_table_styles(styles)

    def create_heatmap(self, subject, time):
        data = self.data[self.data['subject'] == subject]
        data = data[data['time'] == time]
        return report_util.pairwise_heatmap(data, 'fuzzer', 'covered_branches', report_util.format_time_delta(time))


def create_subject_subsection(coverage, subject):
    coverage.plot(subject)
    content = f'<div class="overview">{report_util.fig_to_html()}</div>'
    content += '<div class="slices">'
    for time in coverage.times:
        content += coverage.create_heatmap(subject, time).to_html()
    content += "</div>"
    return f'<div class="subject" id="{subject}"><h3>{subject.title()}</h3>{content}</div>'


def create(campaigns, times):
    print('Creating coverage section.')
    print("\tExtracting coverage data.")
    coverage = CoverageData(campaigns, times)
    print("\t\tExtracted coverage data.")
    content = coverage.to_summary_table().to_html()
    subjects = sorted(coverage.data['subject'].unique())
    for subject in subjects:
        print(f"\tCreating {subject} subsection.")
        content += create_subject_subsection(coverage, subject)
        print(f"\t\tCreated {subject} subsection.")
    print(f'\tCreated coverage section.')
    return f'<div id="coverage"><h2>Coverage</h2>{content}</div>'
