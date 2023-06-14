import os
import pathlib
import sys

import pandas as pd

import coverage_section
import defects_section
import heritability_section
from campaign import Campaign

TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        * {
            font-family: Open Sans, sans-serif;
            color: black;
        }

        h1 {
            font-size: 28px;
            font-weight: bold;
            display: block;
        }

        h2 {
            font-size: 20px;
            font-weight: 550;
            display: block;
        }

        h3 {
            font-size: 18px;
            display: block;
            font-weight: 550;
        }

        img {
            max-width: 100%;
            max-height: calc((100vh - 100px) * 1 / 2);
            width: auto;
            height: auto;
            object-fit: contain;
        }
                .median {
            text-decoration: wavy overline;
        }

        .mean {
            text-decoration: overline;
        }

        .heritability_table .row_heading {
            text-align: left;
        }

        .heritability_table .col_heading.level0 {
            text-align: center;
            border-bottom: black 1px solid;
        }

        .heritability_table .data, .col_heading.level1 {
            text-align: right;
            padding: 0 0.5em;
        }

        .heritability_table thead {
            border-bottom: black solid 1px;
        }

        .heritability_table th {
            padding: 5px;
        }

        .heritability_table * {
            font-size: 12px;
            font-weight: normal;
        }

        .heritability_table caption {
            text-align: left;
        }

        .heritability_table {
            border-bottom: black 1px solid;
            border-top: black 1px solid;
            border-spacing: 10px 0;
            border-collapse: collapse;
        }

        .pairwise {
            display: flex;
        }

        .slices {
            display: flex;
        }

        .heatmap * {
            font-size: 10px;
            text-align: right;
        }

        .heatmap td {
            width: 1%;
            height: 1%;
        }

        .heatmap {
            border-collapse: separate;
            border-spacing: 5px;
            table-layout: fixed;
        }

        .heatmap .label {
            color: black;
            background-color: white;
        }

        .heatmap .p0 {
            background-color: rgb(0, 0, 128);
            color: white;
        }

        .heatmap .p1 {
            background-color: rgb(0, 0, 255);
            color: white;
        }

        .heatmap .p2 {
            background-color: rgb(128, 128, 255);
            color: white;
        }

        .heatmap .p3 {
            background-color: rgb(230, 230, 240);
            color: black;
        }

        .heatmap .e0 {
            background-color: rgb(240, 230, 230);
            color: black;
        }

        .heatmap .e1 {
            background-color: rgb(255, 128, 128);
            color: white;
        }

        .heatmap .e2 {
            background-color: rgb(255, 0, 0);
            color: white;
        }

        .heatmap .e3 {
            background-color: rgb(128, 0, 0);
            color: white;
        }
    </style>
    <title>Fuzzing Report</title>
</head>
<body>
<div>
    $content
</div>
</body>
</html>
"""


def find_campaigns(input_dir):
    print(f'Searching for campaigns in {input_dir}.')
    files = [os.path.join(input_dir, f) for f in os.listdir(input_dir)]
    campaigns = list(map(Campaign, filter(os.path.isdir, files)))
    print(f"\tFound {len(campaigns)} campaigns.")
    return campaigns


def check_campaigns(campaigns):
    print(f'Checking campaigns.')
    result = []
    for c in campaigns:
        if not c.valid:
            print(f"\tMissing required files for {c.id}.")
        else:
            result.append(c)
    print(f'\t{len(result)} campaigns were valid.')
    return result


def find_heritability_results(input_dir):
    file = os.path.join(input_dir, 'heritability.csv')
    print(f'Checking for heritability results: {file}.')
    if os.path.isfile(file):
        print(f'\tHeritability results found.')
        return file
    else:
        print(f'\tHeritability results not found.')
        return None


def compute_slice_times(duration):
    ideal = [pd.to_timedelta(5, 'm'), pd.to_timedelta(1, 'h'), pd.to_timedelta(3, 'h')]
    return [duration] if duration not in ideal else ideal[:ideal.index(duration) + 1]


def write_report(report_file, content):
    print(f'Writing report to {report_file}.')
    os.makedirs(pathlib.Path(report_file).parent, exist_ok=True)
    report = TEMPLATE.replace('$content', content)
    with open(report_file, 'w') as f:
        f.write(report)
    print(f'\tSuccessfully wrote report.')


def create_report(input_dir, report_file):
    campaigns = check_campaigns(find_campaigns(input_dir))
    heritability_csv = find_heritability_results(input_dir)
    times = compute_slice_times(pd.to_timedelta(min(c.duration for c in campaigns), 'ms'))
    content = coverage_section.create(campaigns, times)
    content += defects_section.create(campaigns)
    if heritability_csv is not None:
        content += heritability_section.create(heritability_csv)
    write_report(report_file, content)


def main():
    create_report(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
