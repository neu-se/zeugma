import pandas as pd

import report_util


def pairwise_p_values(data, x, y, caption):
    test = report_util.get_stat_functions(data, y)[0]
    unique_x = sorted(data[x].unique())
    sig_level = report_util.compute_sig_level(unique_x)
    values = [[test(data[data[x] == x1][y], data[data[x] == x2][y]) for x2 in unique_x] for x1 in unique_x]
    sigs = [['color: red;' if value < sig_level else '' for value in row] for row in values]
    values = pd.DataFrame(values, columns=unique_x, index=unique_x)
    sigs = pd.DataFrame(sigs, columns=unique_x, index=unique_x)
    return pd.DataFrame(values).style \
        .set_caption(caption) \
        .format(formatter='{:.2E}') \
        .apply(lambda _: sigs, axis=None)


def add_info(table, info):
    for k, v in info.items():
        table[k] = v


def create_tables(data, f, columns):
    groups = data[columns].drop_duplicates().sort_values(by=columns).to_dict(orient='records')
    return [f(group, report_util.select(data, **group)) for group in groups]
