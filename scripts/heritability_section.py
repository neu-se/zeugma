import tables


def create_pairwise_tables(data, stat):
    return tables.create_pairwise(data, x='crossover_operator', y=stat, columns=['subject'],
                                  caption_f=lambda subject: f'{subject.title()}.')


def create_pairwise_subsection(data, stat, name):
    content = ''.join(t.to_html() for t in create_pairwise_tables(data, stat))
    return f'<div><h3>{name} Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def create(data):
    print('Creating heritability section.')
    content = tables.create_heritability_table(data).to_html()
    content += create_pairwise_subsection(data, 'hybrid', 'HY')
    content += create_pairwise_subsection(data, 'inheritance_rate', 'IR')
    print(f'\tCreated heritability section.')
    return f'<div><h2>Heritability</h2>{content}</div>'
