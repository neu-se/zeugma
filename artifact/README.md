# Artifact for "Crossover in Parametric Fuzzing"

## Purpose

This is the artifact for the paper "Crossover in Parametric Fuzzing" published at the International Conference on
Software Engineering (ICSE) 2024.
This artifact contains the materials needed to replicate the evaluation described in the paper,
the source code for the parametric fuzzer Zeugma including an implementation of linked crossover,
and the raw experiment data analyzed in the paper.
The purpose of this artifact is to facilitate future research into structured fuzzing and to allow researchers to
independently obtain the empirical results reported in the paper "Crossover in Parametric Fuzzing".

We are claiming the "Available" and "Reusable" badges.
This artifact is publicly accessible at https://figshare.com/s/eeec9ee6378739047953.
We have included source code, documentation, and detailed execution
instructions for the tools and experiments used the paper.
We feel that this artifact will allow the research community to reuse and extend the methods and experiments
presented in "Crossover in Parametric Fuzzing".

## Provenance

This artifact can be downloaded from https://figshare.com/s/eeec9ee6378739047953.

## Data

This artifact includes the raw experiment data for the results presented in "Crossover in Parametric Fuzzing".
These included data files are described below.

### heritability.csv

The file "heritability.csv" contains inheritance rates and hybrid data for the heritability experiment.
Each row reports results for a single child input and indicates the fuzzing subject that the input was created for, the
type of crossover used to produce the child, the inheritance rate of the child, and whether the child was hybrid.
Consider the following row:

```
subject,crossover_operator,inheritance_rate,hybrid
...
Rhino,Linked,0.817073,false
```

This row indicates that a child for "Rhino" created using "Linked" crossover had an inheritance rate of "0.817073" and
was not a hybrid.
There are 1,000 rows for each crossover operator for each subject.
This dataset was used to create "Table 2: Heritability Metrics" of the paper.

### saved.csv

The file "saved.csv" contains data about the number of inputs that were saved to the corpus of campaigns for
Zeugma-X.
Each row indicates the identifier of the campaign, the subject the campaign was performed on, the total number of inputs
saved over the full 3 hours of the campaign, and the number of inputs saved in the first 5 minutes of the campaign.
Consider the following row:

```
campaign_id,subject,total_saved,saved_5m
...
264,Rhino,530,332
```

This row indicates that the campaign with the unique identifier "264" which was performed on "Rhino" saved a total of
"530" inputs "332" of which were saved in the first 5 minutes of the campaign.
We used this data to determine that, on average, over half of the inputs saved to a corpus at the end of a three-hour
campaign were saved in the first five minutes.

### coverage.csv

The file "coverage.csv" contains JaCoCo branch coverage measurements for the fuzzing campaigns.
Each row indicates the branch coverage recorded for a particular campaign at a certain time.
Note that the measurement at 0 seconds is normally non-zero because times are computed relative to the first saved
input.
Times are in the format "D days HH:MM:SS[.UUUUUUUUU]", where D is days, HH is hours, MM is minutes, SS is seconds, and
UUUUUUUUU is nanoseconds.

Consider the following row:

```
campaign_id,fuzzer,subject,time,covered_branches
...
0,BeDiv-Simple,Ant,0 days 00:05:00,732
```

This row indicates that the campaign with the unique identifier "0" which was performed on "Ant" using the
fuzzer "BeDiv-Simple" covered "732" branches by time "0 days 00:05:00".

There are 1,001 measurements for each campaign: 1,000 measurements at equal time intervals starting at 0 seconds and
ending at 3 hours, and 1 measurement at 5 minutes.
This dataset was used to create "Figure 3: Branch Coverage Over Time" and "Table 3: Branch Coverage".

### defects.json

The file "defects.json" contains a JSON list of the unique defects detected in at least one
campaign during the evaluation.
This list was obtained by manually deduplicating detected failures.
Each entry in the list indicates the unique identifier of the defect, the subject in which the defect was
discovered, and a link to an issue in the subject's issue tracker for the defect.
Consider the following entry:

```JSON
{
  "subject": "Bcel",
  "identifier": "B0",
  "issue": "https://issues.apache.org/jira/browse/BCEL-367"
}
```

This entry describes defect "B0" which occurred in the subject "Bcel"
and described in the issue at "https://issues.apache.org/jira/browse/BCEL-367".

### failures.json

The file "failures.json" contains a JSON list of unique failures detected in at least one
campaign during the evaluation.
Each entry in the list indicates the subject in which the failure was detected, the Java type of the exception that was
thrown, the stack trace of the thrown exception, and a list of associated defects that cause the failure to occur.
Associated defects were determined by manually analyzing the failures.
If the list of associated defects for an entry is empty, then the failure is not indicative of a genuine defect and
the entry will contain a note explaining why this determination was made.

Consider the following entry:

```JSON
{
  "subject": "Rhino",
  "trace": [
    {
      "declaringClass": "org.mozilla.javascript.TokenStream",
      "fileName": "TokenStream.java",
      "lineNumber": 1991,
      "methodName": "ungetChar"
    },
    ...
  ],
  "type": "java.lang.ArrayIndexOutOfBoundsException",
  "associatedDefects": [
    "R0"
  ]
}
```

This entry describes a failure found in "Rhino" where the thrown exception is of
type `java.lang.ArrayIndexOutOfBoundsException` with the specified stack trace that is a manifestation of the
defect "R0".

### detections.csv

The file "detections.csv" contains defect discovery times for the fuzzing campaigns.

Each row indicates the time at which a campaign first discovered a defect.
If no time is listed for a campaign on a defect, then that campaign did not discover that defect.
Times are in the format "D days HH:MM:SS[.UUUUUUUUU]", where D is days, HH is hours, MM is minutes, SS is seconds, and
UUUUUUUUU is nanoseconds.
For each defect, there is one row for each campaign performed on the same subject as the defect.

Consider the following row:

```
campaign_id,fuzzer,subject,defect,time
...
1008,Zest,Closure,C1,0 days 01:00:34.474000
```

This row indicates that the campaign with the unique identifier "1008" which was performed on "Closure" using the
fuzzer "Zest" first discovered defect "C1" at time "0 days 01:00:34.474000".
This dataset was used to create "Table 4: Defect Detection Rates".

### campaigns.tgz

The file "campaigns.tgz" is a gzipped TAR archive containing output files for each of the fuzzing campaigns performed
for the evaluation.
Each directory within the archive contains the output for a single campaign.
This output consists of four files: "coverage.csv", "failures.json", "summary.json", and "meringue.tgz".
"coverage.csv" contains the branch coverage over time for the campaign.
"failures.json" contains a list of deduplicated failures that were induced by at least one input saved during
the campaign.
"summary.json" contains information about the configuration used for the fuzzing campaign, e.g., the fuzzing
target.
"meringue.tgz" is a gzipped TAR archive of the [Meringue](https://github.com/neu-se/meringue) output for the campaign.

## Setup

1. Install Docker Engine version 23.0.0+.
   Directions for installing Docker Engine are available on
   the [Docker website](https://docs.docker.com/engine/install/).
2. Download the archived Docker image "zeugma-artifact-image.tgz" from the artifact:
   https://figshare.com/s/eeec9ee6378739047953.
   This image can be recreated using the "Dockerfile" and source code archive "zeugma-main.zip" included with the
   artifact.
3. Load the Docker image by running: `docker load -i zeugma-artifact-image.tgz`.
4. Start an interactive Docker container: `docker run -it -p 8080:80 zeugma-artifact bash`

## Usage

The following directions assume that you have started a docker container according to the directions provided above.
All commands are run from the "home" directory of that docker container.

### Running a Fuzzing Campaign

To start a fuzzing campaign, run:

```
mvn -pl :zeugma-evaluation-tools \
  meringue:fuzz meringue:analyze \
  -P<SUBJECT>,<FUZZER> \
  -Dmeringue.outputDirectory=<OUTPUT_DIRECTORY> \
  -Dmeringue.duration=<DURATION>
```

Where:

* \<SUBJECT\> is the fuzzing target: ant, bcel, closure, maven, nashorn, rhino, tomcat.
* \<FUZZER\> is the fuzzer to be used: bedivfuzz-simple, bedivfuzz-structure, rlcheck, zest, zeugma-linked,
  zeugma-none, zeugma-one_point, or zeugma-two_point.
* \<OUTPUT_DIRECTORY\> is the absolute path of the directory to which campaign output files should be written.
* \<DURATION\> is the maximum amount of time to execute the fuzzing campaign for specified in the ISO-8601 duration
  format (e.g., "P2DT3H4M" represents 2 days, 3 hours, and 4 minutes).

This command will first run a fuzzing campaign for the specified duration.
Once this command has completed, the results of the campaign will be analyzed to collect coverage information
using the [JaCoCo Java Code Coverage Library](https://www.eclemma.org/jacoco/) and information about exposed failures.
After the analysis phase finishes, the output directory will contain the following files:

```
├── campaign
│   ├── corpus
│   │   └── *
│   ├── failures
│   │   └── *
│   └── *
├── jacoco
│   └── jacoco.csv
├── coverage.csv
├── failures.json
└── summary.json
```

The "campaign" directory stores data written by the fuzzer.
This data will include a "corpus" directory containing coverage-revealing inputs saved during the fuzzing campaign and
a "failures" directory containing failure-inducing inputs saved during the fuzzing campaign.

When the fuzzing campaign is analyzed, inputs saved to the "corpus" and "failures" directories are run in order by
timestamp to compute JaCoCo branch coverage over time, which is then saved to the file "coverage.csv".
There are two columns in "coverage.csv": "time" and "covered_branches".
The "time" column represents elapsed time in milliseconds since the beginning of the campaign, and the
"covered_branches" column represents the number of branches covered in application classes as determined by JaCoCo.
For example, if "coverage.csv" contained the row "100, 340" that would indicate that 340 branches were covered by inputs
saved in the first 100 milliseconds of the campaign.

The file "jacoco/jacoco.csv" is a JaCoCo-generated report detailing coverage in application classes after all the
inputs saved to the "corpus" and "failures" directories have been run.

The file "failures.json" contains a list of deduplicated failures that were induced by at least one input saved
to "corpus" or "failures" directories.
Failures are naively deduplicated using the top five elements of the failure's stack trace and the type of exception
thrown (e.g., `java.lang.IndexOutOfBoundsException`).
Each entry in "failures.json" will contain the top five elements (or fewer if the trace contains fewer than five
elements) of the stack trace for the failure, the type of exception thrown, the elapsed time in milliseconds since the
beginning of the campaign when the first input that induced the failure was saved, the message (as returned by
`java.lang.Throwable#getMessage`) for the first instance of the failure, and a list of saved inputs
that induced the failure.

The file "summary.json" contains information about the configuration used for the fuzzing campaign, such as the Java
options that were used and the fuzzing target.

#### Example

To test this command, run a one-minute fuzzing campaign on Mozilla Rhino using Zeugma-X:

```shell
mvn -pl :zeugma-evaluation-tools \
  meringue:fuzz meringue:analyze \
  -Prhino,zeugma-none \
  -Dmeringue.outputDirectory=$(pwd)/results/zeugma-x/ \
  -Dmeringue.duration=PT1M

```

Then, run a one-minute fuzzing campaign on Mozilla Rhino using Zeugma-Link (zeugma-linked):

```shell
mvn -pl :zeugma-evaluation-tools \
  meringue:fuzz meringue:analyze \
  -Prhino,zeugma-linked \
  -Dmeringue.outputDirectory=$(pwd)/results/zeugma-link/ \
  -Dmeringue.duration=PT1M

```

### Computing Heritability Metrics

After one or more fuzzing campaigns have been completed using Zeugma-X (zeugma-none),
you can compute the heritability of one-point, two-point, and linked crossover.
First, create an input directory containing the campaigns to be used to compute crossover heritability.
This input directory should contain one subdirectory for each campaign to be included in the report.
Each of these subdirectories should contain the "summary.json" output file and the "campaign" directory from the
fuzzing campaigns output (see ["Running a Fuzzing Campaign"](#Running-a-Fuzzing-Campaign) for information about these
files).
For example, the following is a valid structure for the input directory:

```
├── trial-0
│  ├── campaign
│  │   ├── corpus
│  │   │   └── *
│  │   ├── failures
│  │   │   └── *
│  │   └── *
│  ├── summary.json
│  └── *
├── trial-1
│  ├── campaign
│  │   ├── corpus
│  │   │   └── *
│  │   ├── failures
│  │   │   └── *
│  │   └── *
│  ├── summary.json
│  └── *
```

Once you have built the input directory, run the following command:

```
mvn -pl :zeugma-evaluation-heritability \
    -Pcompute install \
    -Dheritability.corpora=<INPUT_DIRECTORY> \
    -Dheritability.output=<OUTPUT_FILE>
```

Where:

* \<INPUT_DIRECTORY\> is the absolute path of the directory to be scanned for fuzzing campaign archives.
* \<OUTPUT_FILE\> is the absolute path of the file to which the results should be written in CSV format.

#### Example

To test this command, compute heritability metrics using the corpora from the one-minute Zeugma-X campaign run in the
directions above.

```shell
mvn -pl :zeugma-evaluation-heritability \
  -Pcompute install \
  -Dheritability.corpora=$(pwd)/results/ \
  -Dheritability.output=$(pwd)/results/heritability.csv

```

### Creating a Report

After one or more fuzzing campaigns have been completed, you can create a report
summarizing the results of those campaigns.
First, create an input directory containing the results of the campaigns to be included in the report.
This input directory should contain one subdirectory for each campaign to be included in the report.
Each of these subdirectories should contain the "coverage.csv", "failures.json", and "summary.json" output files for a
campaign (see ["Running a Fuzzing Campaign"](#Running-a-Fuzzing-Campaign) for information about these files).
If you wish to include heritability results in the report, add the heritability CSV (see the OUTPUT_FILE argument
described in ["Computing Heritability Metrics"](#Computing-Heritability-Metrics)) to the input directory with the
name "heritability.csv".
For example, the following is a valid structure for the input directory:

```
├── trial-0
│   ├── coverage.csv
│   ├── failures.json
│   ├── summary.json
│   └── *
├── trial-1
│   ├── coverage.csv
│   ├── failures.json
│   ├── summary.json
│   └── *
└── heritability.csv
```

To create the report run:

```
python3 scripts/report.py <INPUT_DIRECTORY> <OUTPUT_FILE>
```

Where:

* \<INPUT_DIRECTORY\> is the path of the input directory you created.
* \<OUTPUT_FILE\> is the path of the file to which the report should be written in HTML format.

#### Example

To test this command, create a report for the campaign runs in the directions above.

```shell
python3 scripts/report.py results/ report.html

```

The easiest way to view the created report from within the container is to run:

```shell
python -m http.server 80

```

Then, open the page "http://localhost:8080/report.html" in a browser.

To create a report using the raw experimental data analyzed in the paper run:

```shell
python3 scripts/report.py /home/data/ report.html

```

### Replicating the Paper Results

To replicate the results reported in the paper, you would need to run twenty campaigns per fuzzer (bedivfuzz-simple,
bedivfuzz-structure, rlcheck, zest, zeugma-linked,
zeugma-none, zeugma-one_point, or zeugma-two_point) each lasting three hours on each subject (ant, bcel, closure, maven,
nashorn, rhino, and tomcat).
These campaigns can be run using the directions given in ["Running a Fuzzing Campaign"](#running-a-fuzzing-campaign).
The output from these campaigns should be collected in a single directory.
Then, heritability metric should be computed following the directions given in
["Computing Heritability Metrics"](#computing-heritability-metrics).
Finally, a report can be created using the directions given in ["Creating a Report"](#creating-a-report).

