import os
import pathlib
import sys

import pandas as pd

import coverage_report


def create_heritability_table(data):
    table = data.copy()
    table.columns = table.columns.map(lambda x: x.replace('_', ' '))
    table = table.groupby(by=['crossover operator', 'subject'])[['inheritance rate', 'hybrid']] \
        .agg(['median', 'mean']) \
        .reset_index()
    table.columns = table.columns.map(lambda x: ' '.join(x[::-1]).strip().title())
    table = table.drop(columns=['Median Hybrid']) \
        .pivot_table(index='Subject', columns='Crossover Operator') \
        .swaplevel(axis=1) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .reindex(['Median Inheritance Rate', 'Mean Inheritance Rate', 'Mean Hybrid'], axis=1, level=1) \
        .rename(columns={'Mean Hybrid': '%HY',
                         'Median Inheritance Rate': '<div class="median">IR</div>',
                         'Mean Inheritance Rate': '<div class="mean">IR</div>'})
    table.index.name = None
    table.columns.names = (None, None)
    return table.style.format('{:.3f}') \
        .set_table_attributes('class="heritability_table"')


def create_heatmap(data, subject, stat):
    return coverage_report.pairwise_heatmap(
        data[data['subject'] == subject],
        'crossover_operator', stat, subject
    ).to_html()


def main():
    data = pd.read_csv(sys.argv[1])
    output_file = sys.argv[2]
    os.makedirs(pathlib.Path(output_file).parent, exist_ok=True)
    subjects = sorted(data['subject'].unique())
    stats_table = create_heritability_table(data).to_html()
    report = coverage_report.read_resource('heritability-template.html') \
        .replace('$h-t', stats_table) \
        .replace('$h-ir', ''.join(create_heatmap(data, s, 'inheritance_rate') for s in subjects)) \
        .replace('$h-hy', ''.join(create_heatmap(data, s, 'hybrid') for s in subjects))
    with open(output_file, 'w') as f:
        f.write(report)


if __name__ == "__main__":
    main()
