import report_util
import tables


def create_pairwise_tables(data):
    return tables.create_pairwise(data, x='fuzzer', y='detected', columns=['defect', 'time'],
                                  caption_f=lambda defect,
                                                   time: f'{defect} at {report_util.format_time_delta(time)}.')


def create_pairwise_subsection(data):
    content = ''.join(t.to_html() for t in create_pairwise_tables(data))
    return f'<div><h3>Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def create(data, times):
    print('Creating defects section.')
    content = tables.create_defect_table(data, times) \
        .set_table_attributes('class="data-table"') \
        .to_html()
    content += create_pairwise_subsection(tables.times_to_detected(data, times))
    print(f'\tCreated defects section.')
    return f'<div><h2>Defects</h2>{content}</div>'
