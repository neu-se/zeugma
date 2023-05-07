import copy
from collections import defaultdict

import pandas as pd
import plotly.graph_objects as go
import scipy


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


def p_color(value):
    if value < 0.001:
        return 128, 0, 0
    elif value < 0.01:
        return 255, 0, 0
    elif value < 0.05:
        return 255, 128, 128
    else:
        return 240, 230, 230


def a_level(value):
    if value < 0.56:
        return 'Negligible'
    elif value < 0.64:
        return 'Small'
    elif value < 0.71:
        return 'Medium'
    else:
        return 'Large'


def effect_color(value, bounds):
    if value < bounds[0]:
        return 230, 230, 240
    elif value < bounds[1]:
        return 128, 128, 255
    elif value < bounds[2]:
        return 0, 0, 255
    else:
        return 0, 0, 128


def compute_pairwise(data, x, y, f1, f2):
    unique_x = sorted(data[x].unique())
    z = [[None for _ in unique_x] for _ in unique_x]
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            z[r][c] = (f1 if r > c else f2)(data[data[x] == x1][y], data[data[x] == x2][y])
    return z


def pairwise_internal(data, x, y, f1, f1_name, f2, f2_name, bounds2, level2):
    unique_x = sorted(data[x].unique())
    z = [[None for _ in unique_x] for _ in unique_x]
    values = compute_pairwise(data, x, y, f1, f2)
    hovers = copy.deepcopy(z)
    text = copy.deepcopy(z)
    color_index_map = defaultdict(lambda: len(color_index_map))
    for r, x1 in enumerate(unique_x):
        for c, x2 in enumerate(unique_x):
            if r == c:
                text[r][r] = hovers[r][c] = unique_x[r]
                color = (255, 255, 255)
            else:
                value = values[r][c]
                text[r][c] = f'{values[r][c]:.4f}'
                f_name = f1_name if r > c else f2_name
                level = "" if r > c else level2(value)
                hovers[r][c] = f'{unique_x[r]} vs. {unique_x[c]}<br>{f_name}: {text[r][c]}<br>{level}'
                color = p_color(value) if r > c else effect_color(value, bounds2)
            z[r][c] = color_index_map[color]
    colors = [f'rgb{k}' for k, _ in sorted(color_index_map.items(), key=lambda item: item[1])]
    lower = [(i / len(colors), color) for i, color in enumerate(colors)]
    upper = [((i + 1) / len(colors), color) for i, color in enumerate(colors)]
    c_scale = [item for pair in zip(lower, upper) for item in pair]
    return go.Heatmap(
        z=z,
        zmin=0,
        zmax=len(color_index_map),
        text=text,
        texttemplate="%{text}",
        colorscale=c_scale,
        customdata=hovers,
        hovertemplate="%{customdata}<extra></extra>",
        showscale=False
    )


def plot_significance(data, x, y):
    if data[y].dtypes == bool:
        return pairwise_internal(data, x, y, fisher_exact, 'Fisher-Exact Two-Tailed P-Value',
                                 odds_ratio, 'Odds Ratio', [1.25, 1.5, 2.0], lambda _: "")
    elif pd.api.types.is_numeric_dtype(data[y]):
        return pairwise_internal(data, x, y, mann_whitney, 'Mann–Whitney Two-Tailed P-Value',
                                 a12, u'Vargha-Delaney Â₁₂ Statistic',
                                 [0.56, 0.64, 0.71], a_level)
    else:
        raise ValueError
