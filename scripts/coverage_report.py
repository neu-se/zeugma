import os
import pathlib
import shutil
import sys

import pandas as pd
import plotly
import plotly.express as px
import plotly.graph_objects as go

import report_util


def read_campaign_data(job_id):
    coverage = pd.read_csv(f'https://ci.in.ripley.cloud/logs/public/katie/zeugma/results/slurm-{job_id}/coverage.csv')
    executions = pd.read_csv(
        f'https://ci.in.ripley.cloud/logs/public/katie/zeugma/results/slurm-{job_id}/executions.csv')
    data = coverage.merge(executions, how='inner', on=coverage.columns.difference(['coverage']).tolist())
    data['time'] = pd.to_timedelta(data['time'])
    return data


def read_template():
    with open(os.path.join(pathlib.Path(__file__).parent.parent, 'resources', 'template.html'), 'r') as f:
        return f.read()


def sample_at_time(data, time):
    times = data['time']
    return data[data['time'] == max(times[times <= time])]


def plot_time_slices(data, times, fig, row):
    fuzzers = sorted(data['fuzzer'].unique())
    color_map = dict(zip(fuzzers, px.colors.qualitative.Plotly))
    steps = []
    first_trace = len(fig.data)
    for time in sorted(times):
        selected = sample_at_time(data, time)
        start = len(fig.data)
        for f in fuzzers:
            trace = go.Box(y=selected[selected['fuzzer'] == f]['coverage'], name=f, marker_color=color_map[f],
                           showlegend=False)
            trace.update(visible=len(steps) == 0)
            fig.add_trace(trace, row, 1)
        trace = report_util.plot_significance(selected, x='fuzzer', y='coverage')
        trace.update(visible=len(steps) == 0)
        fig.add_trace(trace, row=row, col=2)
        visible = [i < first_trace or i >= start for i in range(len(fig.data))]
        label = f'{time.to_pytimedelta()}'
        steps.append(dict(args=[{"visible": visible}, []], method="update", label=label))
    for step in steps:
        missing = len(fig.data) - len(step["args"][0]["visible"])
        step["args"][0]["visible"] = step["args"][0]["visible"] + ([False] * missing)
    included = data[data['time'] >= min(times)]
    included = included[included['time'] <= max(times)]
    fig.update_yaxes(autorange=False, title='Coverage', range=[min(included['coverage']), max(included['coverage'])],
                     row=row, col=1)
    fig.update_xaxes(visible=False, row=row, col=2)
    fig.update_yaxes(visible=False, autorange='reversed', row=row, col=2)
    slider = dict(currentvalue={"prefix": "Time: "}, steps=steps, active=0, pad={"b": 10, "t": 50})
    fig.update_layout(sliders=[slider])


def plot_over_time(data, fig, row):
    fuzzers = sorted(data['fuzzer'].unique())
    color_map = dict(zip(fuzzers, px.colors.qualitative.Plotly))
    for i, y in enumerate(['coverage', 'executions']):
        for fuzzer in fuzzers:
            selected = data[data['fuzzer'] == fuzzer] \
                .groupby(by=['time'])[y] \
                .agg([min, max, 'median']) \
                .reset_index() \
                .sort_values('time')
            times = (selected['time'] / pd.to_timedelta(1, 'm')).tolist()
            r, g, b = plotly.colors.hex_to_rgb(color_map[fuzzer])
            x_area = times + times[::-1]
            y_area = selected['max'].tolist() + selected['min'].iloc[::-1].tolist()
            area_line = dict(color='rgba(255,255,255,0)')
            range_area = go.Scatter(x=x_area, y=y_area, fill='toself', fillcolor=f'rgba{(r, g, b, 0.2)}',
                                    line=area_line, hoverinfo="skip", showlegend=False)
            fig.add_trace(range_area, row, i + 1)
            line = dict(color=f'rgb{(r, g, b)}')
            median_line = go.Scatter(x=times, y=selected['median'], line=line, mode='lines', name=fuzzer,
                                     showlegend=i == 0)
            fig.add_trace(median_line, row, i + 1)
        fig.update_yaxes(title=y.title(), row=row, col=i + 1)
        fig.update_xaxes(title='Time (Minutes)', row=row, col=i + 1)
    fig.update_layout(legend=dict(orientation="h", yanchor="top", y=1.2, xanchor="center", x=0.5))


def create_subject_div(data, subject, slice_times, output_dir):
    data = data[data['subject'] == subject]
    fig = go.Figure().set_subplots(2, 2, vertical_spacing=0.25)
    plot_over_time(data, fig, 1)
    plot_time_slices(data, slice_times, fig, 2)
    fig.update_layout(template="none")
    fig.write_html(os.path.join(output_dir, f'{subject}.html'))
    return f'<div class="subject" id="{subject}"><h2>{subject.title()}</h2><iframe src="{f"{subject}.html"}">' \
           f'</iframe></div>'


def main(job_id, output_dir):
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)
    os.makedirs(output_dir, exist_ok=True)
    data = read_campaign_data(job_id)
    subjects = sorted(data['subject'].unique())
    slice_times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(15, 'm'), pd.to_timedelta(1, 'h')]
    content = ""
    nav = ""
    for subject in subjects:
        nav += f'<a href="#{subject}">{subject.title()}</a>'
        content += create_subject_div(data, subject, slice_times, output_dir)
    report = read_template() \
        .replace('$1', nav) \
        .replace('$2', content)
    with open(os.path.join(output_dir, 'report.html'), 'w') as f:
        f.write(report)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
