import report_util
import tables

TEMPLATE = """
<div>
    <h2>Heritability</h2>
    $h-t
    <h3>Pairwise IR</h3>
    <div class="pairwise">
        $h-ir
    </div>
    <h3>Pairwise HY</h3>
    <div class="pairwise">
        $h-hy
    </div>
</div>
"""


def create_heatmap(data, subject, stat):
    return report_util.pairwise_heatmap(data[data['subject'] == subject], 'crossover_operator', stat, subject) \
        .set_table_attributes('class="heatmap"') \
        .to_html()


def create(data):
    print('Creating heritability section.')
    subjects = sorted(data['subject'].unique())
    table = tables.create_heritability_table(data) \
        .set_table_attributes('class="data-table"')
    content = TEMPLATE.replace('$h-t', table.to_html()) \
        .replace('$h-ir', ''.join(create_heatmap(data, s, 'inheritance_rate') for s in subjects)) \
        .replace('$h-hy', ''.join(create_heatmap(data, s, 'hybrid') for s in subjects))
    print(f'\tCreated heritability section.')
    return content
