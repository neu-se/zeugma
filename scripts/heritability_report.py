import os
import pathlib
import shutil
import sys

import pandas as pd
import plotly.graph_objects as go

import report_util


def plot_subject(data, subject):
    selected = data[data['subject'] == subject]
    table_data = selected.groupby(by=['crossover operator'])[['inheritance rate', 'hybrid']] \
        .agg(['mean', 'median']) \
        .reset_index()
    table_data.columns = [' '.join(col[::-1]).strip() for col in table_data.columns]
    table_data = table_data.drop(columns=['median hybrid']) \
        .rename(columns={'mean hybrid': 'hybrid proportion'})
    trace1 = go.Table(
        header=dict(values=list(map(str.title, table_data.columns.tolist())), align='right'),
        cells=dict(values=table_data.transpose().values.tolist(), align='right', format=["", ".3f", ".3f", ".3f"])
    )
    trace2 = report_util.plot_significance(selected, 'crossover operator', 'inheritance rate')
    trace3 = report_util.plot_significance(selected, 'crossover operator', 'hybrid')
    specs = [[{"colspan": 2, "type": "table"}, None],
             [{}, {}]]
    titles = ["", "Pairwise Inheritance Rate", "Pairwise Hybrid Proportion"]
    fig = go.Figure().set_subplots(2, 2, vertical_spacing=0.25,
                                   specs=specs, subplot_titles=titles)
    fig.add_trace(trace1, 1, 1)
    fig.add_trace(trace2, 2, 1)
    fig.add_trace(trace3, 2, 2)
    for i in [1, 2]:
        fig.update_xaxes(visible=False, row=2, col=i)
        fig.update_yaxes(visible=False, autorange='reversed', row=2, col=i)
    fig.update_layout(template="none")
    fig.update_layout(title_text=f'{subject.title()}', title_x=0.5)
    return fig


def read_template():
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'resources', 'template.html'), 'r') as f:
        return f.read()


def create_subject_div(data, subject, output_dir):
    fig = plot_subject(data, subject)
    fig.update_layout(template="none")
    fig.write_html(os.path.join(output_dir, f'{subject}.html'))
    return f'<div class="subject" id="{subject}"><h2>{subject.title()}</h2><iframe src="{f"{subject}.html"}">' \
           f'</iframe></div>'


def main(input_file, output_dir):
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)
    os.makedirs(output_dir, exist_ok=True)
    data = pd.read_csv(input_file) \
        .rename(columns=lambda c: c.replace('_', ' '))
    subjects = sorted(data['subject'].unique())
    content = ""
    nav = ""
    for subject in subjects:
        nav += f'<a href="#{subject}">{subject.title()}</a>'
        content += create_subject_div(data, subject, output_dir)
    report = read_template() \
        .replace('$1', nav) \
        .replace('$2', content)
    with open(os.path.join(output_dir, 'report.html'), 'w') as f:
        f.write(report)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
