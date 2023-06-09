import pandas as pd

import report_util

TEMPLATE = """
<div class="heritability">
    <h2>Heritability</h2>
    <h3>Heritability Metrics</h3>
    $h-t
    <h3>Pairwise Inheritance Rates</h3>
    <div class="pairwise">
        $h-ir
    </div>
    <h3>Pairwise Hybrid Proportions</h3>
    <div class="pairwise">
        $h-hy
    </div>
</div>
"""


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
    return report_util.pairwise_heatmap(data[data['subject'] == subject], 'crossover_operator', stat, subject) \
        .to_html()


def create(heritability_csv):
    print('Creating heritability section.')
    data = pd.read_csv(heritability_csv)
    subjects = sorted(data['subject'].unique())
    stats_table = create_heritability_table(data).to_html()
    content = TEMPLATE.replace('$h-t', stats_table) \
        .replace('$h-ir', ''.join(create_heatmap(data, s, 'inheritance_rate') for s in subjects)) \
        .replace('$h-hy', ''.join(create_heatmap(data, s, 'hybrid') for s in subjects))
    print(f'\tCreated heritability section.')
    return content
