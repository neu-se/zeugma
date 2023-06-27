import base64
import copy
from io import BytesIO

import matplotlib.pyplot as plt
import pandas as pd
import scipy

A12_BOUNDS = [0.56, 0.64, 0.71]
P_BOUNDS = [0.001, 0.01, 0.05]
ODDS_RATIO_BOUNDS = [1.25, 1.5, 2.0]


def a12(values1, values2):
    """
    Returns Vargha-Delaney A12 statistic.
    Vargha, A., & Delaney, H. D. (2000).
    A Critique and Improvement of the "CL" Common Language Effect Size Statistics of McGraw and Wong.
    Journal of Educational and Behavioral Statistics, 25(2), 101–132.
    https://doi.org/10.2307/1165329
    """
    a = scipy.stats.mannwhitneyu(values1, values2)[0] / (len(values1) * len(values2))
    return 1 - a if a < 0.5 else a


def mann_whitney(values1, values2):
    return scipy.stats.mannwhitneyu(values1, values2, alternative='two-sided', use_continuity=True)[1]


def fisher_exact(truth_values1, truth_values2):
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    return scipy.stats.fisher_exact(table, alternative='two-sided')[1]


def odds_ratio(truth_values1, truth_values2):
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    return scipy.stats.fisher_exact(table, alternative='two-sided')[0]


def compute_bucket(value, bounds):
    for i, bound in enumerate(bounds):
        if value < bound:
            return i
    return len(bounds)


def pairwise_heatmap(data, x, y, caption):
    if data[y].dtypes == bool:
        f1 = fisher_exact
        f2 = odds_ratio
        bounds2 = ODDS_RATIO_BOUNDS
    elif pd.api.types.is_numeric_dtype(data[y]):
        f1 = mann_whitney
        f2 = a12
        bounds2 = A12_BOUNDS
    else:
        raise ValueError
    unique_x = sorted(data[x].unique())
    text = [[None for _ in unique_x] for _ in unique_x]
    classes = copy.deepcopy(text)
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            values1 = data[data[x] == x1][y]
            values2 = data[data[x] == x2][y]
            if r == c:
                text[r][c] = x1
                classes[r][c] = 'label'
            elif r > c:
                z = f1(values1, values2)
                text[r][c] = f'{z:.2E}'
                classes[r][c] = f'p{compute_bucket(z, P_BOUNDS)}'
            else:
                z = f2(values1, values2)
                text[r][c] = f'{z:.3f}'
                classes[r][c] = f'e{compute_bucket(z, bounds2)}'
    return pd.DataFrame(text).style \
        .set_table_attributes('class="heatmap"') \
        .set_td_classes(pd.DataFrame(classes)) \
        .set_caption(caption) \
        .hide(axis="index") \
        .hide(axis="columns")


def format_time_delta(time_delta):
    for unit in ['d', 'h', 'm', 's']:
        value = time_delta / pd.to_timedelta(1, unit)
        if value.is_integer():
            return f'{int(value)}{unit.upper()}'
    value = time_delta / pd.to_timedelta(1, 'm')
    return f'{round(value, 3)}M'


def fig_to_html():
    buffer = BytesIO()
    plt.savefig(buffer, dpi=600, bbox_inches='tight', format='png')
    plt.close()
    return f'<img src="data:image/png;base64,{base64.b64encode(buffer.getvalue()).decode("utf-8")}">'
