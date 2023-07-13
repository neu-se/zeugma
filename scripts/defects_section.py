import report_util
import tables


def create_pairwise_subsection(data):
    content = ''
    groups = data[['defect', 'time']].drop_duplicates().sort_values(by=['defect', 'time']) \
        .itertuples(index=False, name=None)
    for defect, time in groups:
        selected = report_util.select(data, defect=defect, time=time)
        caption = f'{defect} at {report_util.format_time_delta(time)}.'
        content += report_util.pairwise_heatmap(selected, 'fuzzer', 'detected', caption).to_html()
    return f'<div><h3>Pairwise P-Values and Effect Sizes</h3><div class="wrapper">{content}</div></div>'


def create(data, times):
    print('Creating defects section.')
    content = tables.create_defect_table(data, times) \
        .set_table_attributes('class="data-table"') \
        .to_html()
    content += create_pairwise_subsection(tables.times_to_detected(data, times))
    print(f'\tCreated defects section.')
    return f'<div><h2>Defects</h2>{content}</div>'
