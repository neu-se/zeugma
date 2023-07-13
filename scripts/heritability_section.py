import report_util
import tables


def create_pairwise_subsection(data, stat, name):
    content = ''
    for subject in sorted(data['subject'].unique()):
        selected = report_util.select(data, subject=subject)
        caption = f'{subject.title()}.'
        content += report_util.pairwise_heatmap(selected, 'crossover_operator', stat, caption).to_html()
    return f'<div><h3>{name} Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def create(data):
    print('Creating heritability section.')
    content = tables.create_heritability_table(data).to_html()
    content += create_pairwise_subsection(data, 'hybrid', 'HY')
    content += create_pairwise_subsection(data, 'inheritance_rate', 'IR')
    print(f'\tCreated heritability section.')
    return f'<div><h2>Heritability</h2>{content}</div>'
