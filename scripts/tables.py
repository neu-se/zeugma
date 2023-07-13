import pandas as pd

import report_util

BASELINE_FUZZER = 'Zeugma-Link'
FUZZER_ORDER = ['BeDiv-Simple', 'BeDiv-Struct', 'RLCheck', 'Zest', 'Zeugma-X', 'Zeugma-1PT', 'Zeugma-2PT',
                'Zeugma-Link']


def highlight_max(data, props):
    if data.index.nlevels == 2:
        is_max = data.groupby(level=1).transform('max') == data
    else:
        is_max = data == data.max()
    return is_max.replace({True: props, False: ''})


def style_table(table, precision=3, axis=1):
    return table.style.format(precision=precision, na_rep='---') \
        .apply(lambda x: highlight_max(x, 'background-color: yellow;'), axis=axis)


def pivot(data, col, row, value, col2='time'):
    data = data.pivot(index=[row], values=[value], columns=[col, col2]) \
        .reorder_levels(axis=1, order=[col, col2, None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(2, axis=1)
    data.index.name = data.index.name.title()
    data.columns.names = [None, None]
    if col2 == 'time':
        data.columns = data.columns.map(lambda l: (l[0], report_util.format_time_delta(l[1])))
    return data


def create_stat_table_frame(data, x, baseline_x, y):
    treatments = sorted(data[x].unique())
    rows = []
    baseline_values = data[data[x] == baseline_x][y]
    sig_level = report_util.compute_sig_level(treatments)
    test, _, _, stat = report_util.get_stat_functions(data, y)
    for treatment in treatments:
        values = data[data[x] == treatment][y]
        sig = 'color: red;' if test(baseline_values, values) < sig_level else ''
        rows.append([treatment, stat(values), sig])
    return pd.DataFrame(rows, columns=[x, 'stat', 'sig'])


def create_stat_table(data, x, baseline_x, y, columns):
    groups = data[columns].drop_duplicates().to_dict(orient='records')
    frames = []
    for group in groups:
        frame = create_stat_table_frame(report_util.select(data, **group), x=x, baseline_x=baseline_x, y=y)
        for k, v in group.items():
            frame[k] = v
        frames.append(frame)
    return pd.concat(frames)


def create_coverage_table(data, times):
    table = create_stat_table(data[data['time'].isin(times)],
                              x='fuzzer', baseline_x=BASELINE_FUZZER, y='covered_branches',
                              columns=['time', 'subject'])
    stats = pivot(table, 'subject', 'fuzzer', 'stat').reindex(FUZZER_ORDER)
    sigs = pivot(table, 'subject', 'fuzzer', 'sig')
    return style_table(stats, precision=1, axis=0) \
        .apply(lambda _: sigs, axis=None) \
        .set_caption('Branch Coverage.')


def times_to_detected(data, times):
    # Convert detection times into boolean detected by time
    elements = []
    for time in times:
        temp = data.copy()
        temp['detected'] = temp['time'] < time
        temp['time'] = time
        elements.append(temp)
    return pd.concat(elements)


def create_defect_table(data, times):
    data = times_to_detected(data, times)
    table = create_stat_table(data, x='fuzzer', baseline_x=BASELINE_FUZZER, y='detected', columns=['time', 'defect'])
    stats = pivot(table, 'defect', 'fuzzer', 'stat').reindex(FUZZER_ORDER)
    sigs = pivot(table, 'defect', 'fuzzer', 'sig')
    return style_table(stats, precision=2, axis=0) \
        .apply(lambda _: sigs, axis=None) \
        .set_caption('Defect Detection Rates.')


def create_heritability_table(data):
    ir = create_stat_table(data, x='crossover_operator', baseline_x='Linked', y='inheritance_rate', columns=['subject'])
    ir['metric'] = 'IR'
    hy = create_stat_table(data, x='crossover_operator', baseline_x='Linked', y='hybrid', columns=['subject'])
    hy['metric'] = 'HY'
    table = pd.concat([ir, hy])
    stats = pivot(table, col='crossover_operator', row='subject', col2='metric', value='stat')
    sigs = pivot(table, col='crossover_operator', row='subject', col2='metric', value='sig')
    return style_table(stats, precision=3, axis=1) \
        .apply(lambda _: sigs, axis=None) \
        .set_caption('Heritability Metrics.')
