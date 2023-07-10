import base64
from io import BytesIO

import matplotlib.pyplot as plt
import pandas as pd
import scipy

A12_BOUNDS = [0.56, 0.64, 0.71]
ODDS_RATIO_BOUNDS = [1.25, 1.5, 2.0]


def a12(values1, values2):
    """
    Returns Vargha-Delaney A12 statistic.
    Vargha, A., and Delaney, H. D. (2000).
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
    """
    Computes the odd ratio for two boolean lists of samples.
    Applies a modified Haldane-Anscombe correction as described by:
    Weber, F., Knapp, G., Ickstadt, K., Kundt, G., & Glass, Ä. (2020).
    Zero-cell corrections in random-effects meta-analyses.
    Research synthesis methods, 11(6), 913–919.
    https://doi.org/10.1002/jrsm.1460
    """
    table = [[truth_values1.sum(), truth_values2.sum()],
             [(~truth_values1).sum(), (~truth_values2).sum()]]
    if 0 in table[0] or 0 in table[1]:
        table = [[x + 0.5 for x in row] for row in table]
    odds0 = table[0][0]/table[0][1]
    odds1 = table[1][0]/table[1][1]
    return odds0/odds1


def compute_bucket(value, bounds):
    for i, bound in enumerate(bounds):
        if value < bound:
            return i
    return len(bounds)


def to_props(text_colors, background_colors):
    props = []
    for t_row, b_row in zip(text_colors, background_colors):
        props.append(f'color: {t}; background-color: {b};' for t, b in zip(t_row, b_row))
    return pd.DataFrame(props)


def compute_pairwise(data, x, y):
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
    n = len(unique_x)
    pairs = (n * (n - 1)) / 2
    significance_level = 0.05 / pairs
    text = [[x2 for x2 in unique_x] for _ in unique_x]
    text_colors = [['black' for _ in unique_x] for _ in unique_x]
    background_colors = [['white' for _ in unique_x] for _ in unique_x]
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            values1 = data[data[x] == x1][y]
            values2 = data[data[x] == x2][y]
            if r > c:
                p = f1(values1, values2)
                text[r][c] = f'{p:.2E}'
                if p < significance_level:
                    text_colors[r][c] = 'white'
                    background_colors[r][c] = '#000080'
            elif r < c:
                e = f2(values1, values2)
                text[r][c] = f'{e:.3f}'
                bucket = compute_bucket(e, bounds2)
                text_colors[r][c] = ['black', 'white', 'white', 'white'][bucket]
                background_colors[r][c] = ['#f0e6e6', '#ff8080', '#ff0000', '#800000'][bucket]
    return text, text_colors, background_colors


def pairwise_heatmap(data, x, y, caption):
    text, text_colors, background_colors = compute_pairwise(data, x, y)
    return pd.DataFrame(text).style \
        .set_caption(caption) \
        .apply(lambda _: to_props(text_colors, background_colors), axis=None) \
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


def compute_slice_times(duration):
    ideal = [pd.to_timedelta(5, 'm'), pd.to_timedelta(3, 'h')]
    return [duration] if duration not in ideal else ideal[:ideal.index(duration) + 1]
