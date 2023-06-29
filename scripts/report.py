import os
import pathlib
import sys

import pandas as pd

import campaign
import coverage_section
import defects_section
import heritability_section
from report_util import compute_slice_times

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
        
        .data-table * {
            font-size: 12px;
            font-weight: normal;
            text-align: right;
            padding: 5px;
        }
        
        .data-table {
            border-bottom: black 1px solid;
            border-top: black 1px solid;
            border-collapse: collapse;
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


def find_heritability_results(input_dir):
    file = os.path.join(input_dir, 'heritability.csv')
    print(f'Checking for heritability results: {file}.')
    if os.path.isfile(file):
        print(f'\tHeritability results found.')
        return file
    else:
        print(f'\tHeritability results not found.')
        return None


def write_report(report_file, content):
    print(f'Writing report to {report_file}.')
    os.makedirs(pathlib.Path(report_file).parent, exist_ok=True)
    report = TEMPLATE.replace('$content', content)
    with open(report_file, 'w') as f:
        f.write(report)
    print(f'\tSuccessfully wrote report.')


def create_report(input_dir, report_file):
    campaigns = campaign.check_campaigns(campaign.find_campaigns(input_dir))
    heritability_csv = find_heritability_results(input_dir)
    times = compute_slice_times(pd.to_timedelta(min(c.duration for c in campaigns), 'ms'))
    content = coverage_section.create(campaigns, times)
    content += defects_section.create(campaigns, times)
    if heritability_csv is not None:
        content += heritability_section.create(heritability_csv)
    write_report(report_file, content)


def main():
    create_report(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
