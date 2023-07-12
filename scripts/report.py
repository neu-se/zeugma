import os
import pathlib
import sys

import pandas as pd

import campaign
import coverage_section
import extract
import heritability_section
import report_util
import tables

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


def find_dataset(input_dir, name):
    file = os.path.join(input_dir, f'{name}.csv')
    print(f'Checking for {name} data: {file}.')
    if os.path.isfile(file):
        print(f'\t{name.title()} data found.')
        return report_util.read_timedelta_csv(file)
    else:
        print(f'\t{name.title()} data not found.')
        return None


def create_defects_section(data, times):
    print('Creating defects section.')
    content = tables.create_defect_table(data, times) \
        .set_table_attributes('class="data-table"') \
        .to_html()
    print(f'\tCreated defects section.')
    return f'<div><h2>Defects</h2>{content}</div>'


def write_report(report_file, content):
    print(f'Writing report to {report_file}.')
    os.makedirs(pathlib.Path(report_file).parent, exist_ok=True)
    report = TEMPLATE.replace('$content', content)
    with open(report_file, 'w') as f:
        f.write(report)
    print(f'\tSuccessfully wrote report.')


def create_report(input_dir, output_file):
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(3, 'h')]
    coverage = find_dataset(input_dir, 'coverage')
    detections = find_dataset(input_dir, 'detections')
    if coverage is None or detections is None:
        campaigns = campaign.read_campaigns(input_dir)
        if coverage is None:
            coverage = extract.extract_coverage_data(campaigns, times, input_dir)
        if detections is None:
            detections = extract.extract_detections_data(campaigns, input_dir)
    content = coverage_section.create(coverage, times)
    content += create_defects_section(detections, times)
    heritability = find_dataset(input_dir, 'heritability')
    if heritability is not None:
        content += heritability_section.create(heritability)
    write_report(output_file, content)


def main():
    create_report(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
