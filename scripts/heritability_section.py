import pandas as pd

import report_util
import tables

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


def create_heatmap(data, subject, stat):
    return report_util.pairwise_heatmap(data[data['subject'] == subject], 'crossover_operator', stat, subject) \
        .set_table_attributes('class="heatmap"') \
        .to_html()


def create(heritability_csv):
    print('Creating heritability section.')
    data = pd.read_csv(heritability_csv)
    subjects = sorted(data['subject'].unique())
    heritability = tables.create_heritability_table(heritability_csv)
    table = tables.style_table(heritability, precision=3).set_table_attributes('class="data-table"')
    content = TEMPLATE.replace('$h-t', table.to_html()) \
        .replace('$h-ir', ''.join(create_heatmap(data, s, 'inheritance_rate') for s in subjects)) \
        .replace('$h-hy', ''.join(create_heatmap(data, s, 'hybrid') for s in subjects))
    print(f'\tCreated heritability section.')
    return content
