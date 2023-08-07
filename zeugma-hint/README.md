# Zeugma-Hint

An extension to the Zeugma parametric fuzzer that employs string "hinting".

## Requirements

* OpenJDK 11
* [Apache Maven](https://maven.apache.org/) 3.6.0+

## Building

1. Ensure that some version of OpenJDK 11 is installed.
2. Set the JAVA_HOME environmental variable to the path of this OpenJDK 11 installation.
   For example, on Linux, run `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`.
3. Clone or download this repository.
4. In the root directory for this project, run `mvn -DskipTests install`.

## Running a Fuzzing Campaign

After this project has been built, you can run and analyze a fuzzing campaign.
In the root directory of this project, run the following command:

```
mvn -pl :zeugma-hint-evaluation
meringue:fuzz meringue:analyze 
-P<SUBJECT>
-Dmeringue.outputDirectory=<OUTPUT_DIRECTORY> 
-Dmeringue.duration=<DURATION>
```

Where:

* \<SUBJECT\> is the fuzzing target: ant, bcel, closure, maven, nashorn, rhino, tomcat.
* \<OUTPUT_DIRECTORY\> is the path of the directory to which campaign output files should be written.
  If a relative path is used, then the path will be resolved relative to the zeugma-evaluation/zeugma-evaluation-tools
  directory not the project root.
* \<DURATION\> is the maximum amount of time to execute the fuzzing campaign for specified in the ISO-8601 duration
  format (e.g., "P2DT3H4M" represents 2 days, 3 hours, and 4 minutes).

This command will first run a fuzzing campaign for the specified duration.
One this command has completed, the results of the campaign will be analyzed in order to collect coverage information
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
For example, if "coverage.csv" contained the row `100, 340` that would indicate that 340 branches were covered by inputs
saved in the first 100 milliseconds of the campaign.

The file "jacoco/jacoco.csv" is a JaCoCo-generated report detailing coverage in application classes after all the
inputs saved to the "corpus" and "failures" directories have been run.

The file "failures.json" contains a list of deduplicated failures that were induced by at least one input saved
to "corpus" or "failures" directories.
Failures are naively deduplicated using the top five elements of the failure's stack trace and the type of exception
thrown (e.g., java.lang.IndexOutOfBoundsException).
Each entry in "failures.json" will contain the top five elements (or fewer if the trace contains fewer than five
elements) of the stack trace for the failure, the type of exception thrown, the elapsed time in milliseconds since the
beginning of the campaign when the first input that induced the failure was saved, the message (as returned by
java.lang.Throwable#getMessage) for the first instance of the failure, and a list of saved inputs
that induced the failure.

The file "summary.json" contains information about the configuration used for the fuzzing campaign, such as the Java
options that were used and the fuzzing target.

## Replaying an Input

After this project has been built and a fuzzing campaign has been completed, you can rerun any input that was saved by
the fuzzer.
In the root directory of this project, run the following command:

```
mvn -pl :zeugma-hint-evaluation
meringue:replay
-P<SUBJECT>
-Dmeringue.input=<INPUT>
```

Where:

* \<SUBJECT\> is the fuzzing target for the input to be replayed: ant, bcel, closure, maven,
  nashorn, rhino, tomcat.
* \<INPUT\> is the path of a file containing an input that was saved during a fuzzing campaign performed by the
  specified fuzzer on the specified subject.

This command will start a Java process to rerun the input.
This process will stop and wait for a debugger to attach on port 5005.
If the input being rerun throws an exception, that exception's stack trace will be printed to standard err.

## Creating a Report

After this project has been built and one or more fuzzing campaigns have been completed, you can create a report
summarizing the results of those campaigns.
First, create an input directory containing the results of the campaigns to be included in the report.
This input directory should contain one subdirectory for each campaign to be included in the report.
Each of these subdirectories should contain the "coverage.csv", "failures.json", and "summary.json" output files for a
campaign (see ["Running a Fuzzing Campaign"](#Running-a-Fuzzing-Campaign) for information about these files).
For example, the following is a valid structure for the input directory:

```
├── campaign-0
│   ├── coverage.csv
│   ├── failures.json
│   └── summary.json
├── campaign-1
│   ├── coverage.csv
│   ├── failures.json
│   └── summary.json
└──  campaign-2
    ├── coverage.csv
    ├── failures.json
    └── summary.json
```

Next, ensure that you have Python 3.9+ installed.
In the root directory of this project, create and activate a virtual environment:

```shell
python3 -m venv venv
. venv/bin/activate
```

Now install the required libraries:

```shell
pip install -r ./resources/requirements.txt
```

Finally, create the report by running:

```
python3 scripts/report.py <INPUT_DIRECTORY> <OUTPUT_FILE>
```

Where:

* \<INPUT_DIRECTORY\> is the path of the input directory you created.
* \<OUTPUT_FILE\> is the path of the file to which the report should be written in HTML format.

## License

This software release is licensed under the BSD 3-Clause License.

Copyright (c) 2023, Katherine Hough and Jonathan Bell.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

## Acknowledgements

Zeugma-Hint makes use of the following libraries:

* [ASM](http://asm.ow2.org/license.html), (c) INRIA, France
  Telecom, [license](http://asm.ow2.org/license.html)