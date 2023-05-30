# Zeugma

A parametric fuzzer that uses call tree information to select crossover points.

## Requirements

* OpenJDK 11
* [Apache Maven](https://maven.apache.org/) 3.6.0+

## Building

1. Ensure that some version of OpenJDK 11 is installed.
2. Set the JAVA_HOME environmental variable to the path to this installation.
   For example, on Linux, run `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`.
3. Clone or download this repository.
4. In the root directory of this project, run `mvn -DskipTests install`.

## Evaluation

### Fuzzing Tools

Zeugma's evaluation features the following fuzzing tools:

* Zest
    * Citation: Rohan Padhye, Caroline Lemieux, and Koushik Sen. 2019. JQF: Coverage-Guided Property-Based Testing in
      Java. In Proceedings of the 28th ACM SIGSOFT International Symposium on Software Testing and Analysis (ISSTA ’19),
      July 15–19, 2019, Beijing, China. ACM, New York, NY, USA, 4 pages. https://doi.org/10.1145/3293882.3339002
    * URL: https://github.com/rohanpadhye/JQF
    * Version: 2.0
    * License: BSD 2-Clause License
* RLCheck
    * Citation: S. Reddy, C. Lemieux, R. Padhye and K. Sen, "Quickly Generating Diverse Valid Test Inputs with
      Reinforcement Learning," 2020 IEEE/ACM 42nd International Conference on Software Engineering (ICSE), Seoul,
      Korea (South), 2020, pp. 1410-1421.
    * URL: https://github.com/sameerreddy13/rlcheck
    * Commit: a01ba361a57d1ab5058f56d0b3b2736246f1596d
    * Licence: BSD 2-Clause License
* BeDivFuzz
    * Citation: H. L. Nguyen and L. Grunske, "BEDIVFUZZ: Integrating Behavioral Diversity into Generator-based Fuzzing,"
      2022 IEEE/ACM 44th International Conference on Software Engineering (ICSE), Pittsburgh, PA, USA, 2022, pp.
      249-261, doi: 10.1145/3510003.3510182.
    * URL: https://github.com/hub-se/BeDivFuzz
    * Commit: c06eaca5a9e7ef6123d3abb046d5ea3251db85b5
    * License: BSD 2-Clause License

### Fuzzing Experiment

#### Running a Fuzzing Campaign

After this project has been built, run:

```
mvn -pl :zeugma-evaluation-tools
meringue:fuzz meringue:analyze 
-P<SUBJECT>,<FUZZER> 
-Dmeringue.outputDirectory=<OUTPUT_DIRECTORY> 
-Dmeringue.duration=<DURATION>
```

Where:

* \<SUBJECT\> is the fuzzing target: ant, bcel, closure, maven, nashorn, rhino, tomcat.
* \<FUZZER\> is the fuzzer to be used: bedivfuzz-simple, bedivfuzz-structure, rlcheck, zest, zeugma-linked, zeugma-none,
  zeugma-one_point, or zeugma-two_point.
* \<OUTPUT_DIRECTORY\> is the path of the directory to which campaign output files should be written.
* \<DURATION\> is the maximum amount of time to execute the fuzzing campaign for specified in the ISO-8601 duration
  format (e.g., "P2DT3H4M" represents 2 days, 3 hours, and 4 minutes).

#### Replaying an Input

After this project has been built, run:

```
mvn -pl :zeugma-evaluation-tools
meringue:replay
-P<SUBJECT>,<FUZZER> 
-Dmeringue.input=<INPUT>
```

Where:

* \<SUBJECT\> is the fuzzing target for the input to be replayed: ant, bcel, closure, maven,
  nashorn, rhino, tomcat.
* \<FUZZER\> is the fuzzer that was used to produce the input to be replayed: bedivfuzz-simple, bedivfuzz-structure,
  rlcheck, zest, zeugma-linked, zeugma-none, zeugma-one_point, or zeugma-two_point.
* \<INPUT\> is the path of the file containing the input to be replayed.

#### Interpreting Fuzzing Results

TODO

### Heritability Experiment

#### Computing Heritability Metrics

After this project has been built, run:

```
mvn -pl :zeugma-evaluation-heritability 
dependency:properties exec:java@instrument exec:exec@compute
-Dheritability.corporaDir=<C>
-Dheritability.outputFile=<O>
```

Where:

* \<C\> is the path of the directory to scan for fuzzing campaign corpora.
* \<O\> path of file to which the results should be written in CSV format.

#### Interpreting Heritability Results

TODO

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

Zeugma makes use of the following libraries:

* [ASM](http://asm.ow2.org/license.html), (c) INRIA, France
  Telecom, [license](http://asm.ow2.org/license.html)